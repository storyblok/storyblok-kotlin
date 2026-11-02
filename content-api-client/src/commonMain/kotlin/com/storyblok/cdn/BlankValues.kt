package com.storyblok.cdn

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.capturedKClass
import kotlinx.serialization.descriptors.getContextualDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Decodes [element] as [deserializer], reading a blank value of a field that cannot be blank as a value that is not
 * set, and reporting a value that does not parse as a [SerializationException].
 *
 * Storyblok sends a Date/Time, Number or Single-Option field left blank as `""`, rather than leaving it out or sending
 * `null`. Where a property's type cannot read `""` — a number, a date or time, a `Uuid` or a `Story` — that value is
 * left out of an object when the property has a default, which then applies, and is `null` for a nullable property or
 * list element. A required property still fails, and any other type, such as a `String`, an enum or a custom
 * serializer, gets `""` as it arrived.
 *
 * A serializer that parses text, such as `Instant`'s or `Uuid`'s, rejects a value it cannot parse with an
 * [IllegalArgumentException] rather than a [SerializationException], as does a component whose `init` block calls
 * `require`. Both are a mismatch between the content and the types decoding it, so both are reported as one, rather
 * than as the failed request a [StoryblokClientException] is.
 */
internal fun <T> Json.decodeContent(deserializer: DeserializationStrategy<T>, element: JsonElement): T {
    val withoutBlanks = Blanks(this).removeFrom(element, deserializer.descriptor)
    return try {
        decodeFromJsonElement(deserializer, withoutBlanks)
    } catch (e: SerializationException) {
        throw e
    } catch (e: IllegalArgumentException) {
        throw SerializationException(e.message, e)
    }
}

/** Takes the blank values [decodeContent] describes out of one JSON tree, reading each class's keys once. */
private class Blanks(private val json: Json) {

    private val indices = mutableMapOf<SerialDescriptor, Map<String, Int>>()

    /** [element] without the blank values [descriptor] cannot read, or [element] itself when it holds none. */
    fun removeFrom(element: JsonElement, descriptor: SerialDescriptor): JsonElement {
        // The serializer a contextual descriptor stands for, or the value an inline class wraps, is what reads the
        // JSON. Either is described as not nullable, so the nullability of the property it fills is carried over.
        val reader = when {
            descriptor.kind == SerialKind.CONTEXTUAL -> json.serializersModule.getContextualDescriptor(descriptor) ?: return element
            descriptor.isInline -> descriptor.getElementDescriptor(0)
            else -> null
        }
        if (reader != null) return removeFrom(element, if (descriptor.isNullable) reader.nullable else reader)
        return when (element) {
            is JsonPrimitive -> if (descriptor.isNullable && isUnreadableBlank(element, descriptor)) JsonNull else element
            is JsonArray -> when (descriptor.kind) {
                StructureKind.LIST -> element.mapUnlessSame { removeFrom(it, descriptor.getElementDescriptor(0)) }
                else -> element
            }
            is JsonObject -> when (descriptor.kind) {
                StructureKind.CLASS, StructureKind.OBJECT -> objectWithoutBlanks(element, descriptor)
                StructureKind.MAP -> element.mapValuesUnlessSame { removeFrom(it, descriptor.getElementDescriptor(1)) }
                is PolymorphicKind -> subclassDescriptor(element, descriptor)?.let { objectWithoutBlanks(element, it) } ?: element
                else -> element
            }
        }
    }

    private fun objectWithoutBlanks(element: JsonObject, descriptor: SerialDescriptor): JsonObject {
        val indices = indices.getOrPut(descriptor) { elementIndices(descriptor) }
        var changed = false
        val entries = buildMap {
            for ((key, value) in element) {
                val index = indices[key]
                if (index != null) {
                    val elementDescriptor = descriptor.getElementDescriptor(index)
                    // Left out, so the property's default applies.
                    if (value is JsonPrimitive && isUnreadableBlank(value, elementDescriptor) && descriptor.isElementOptional(index)) {
                        changed = true
                        continue
                    }
                    // `null` for a nullable property, and otherwise left for decoding to report, a required property
                    // having no value to fall back on.
                    val withoutBlanks = removeFrom(value, elementDescriptor)
                    if (withoutBlanks !== value) changed = true
                    put(key, withoutBlanks)
                } else {
                    put(key, value)
                }
            }
        }
        return if (changed) JsonObject(entries) else element
    }

    /**
     * The index of each of [descriptor]'s elements by every key decoding accepts for it. An element's own name takes
     * precedence over another's alternative name, as it does in decoding.
     */
    private fun elementIndices(descriptor: SerialDescriptor): Map<String, Int> = buildMap {
        for (index in 0 until descriptor.elementsCount) {
            val name = descriptor.getElementName(index)
            put(json.configuration.namingStrategy?.serialNameForJson(descriptor, index, name) ?: name, index)
        }
        if (!json.configuration.useAlternativeNames) return@buildMap
        for (index in 0 until descriptor.elementsCount) {
            descriptor.getElementAnnotations(index)
                .filterIsInstance<JsonNames>()
                .forEach { annotation -> annotation.names.forEach { getOrPut(it) { index } } }
        }
    }

    /** The descriptor of the subclass [element] names in its class discriminator, among those [descriptor] decodes. */
    private fun subclassDescriptor(element: JsonObject, descriptor: SerialDescriptor): SerialDescriptor? {
        val discriminator = descriptor.annotations.filterIsInstance<JsonClassDiscriminator>().firstOrNull()?.discriminator
            ?: json.configuration.classDiscriminator
        val name = (element[discriminator] as? JsonPrimitive)?.contentOrNull ?: return null
        return when (descriptor.kind) {
            // A plugin-generated sealed descriptor lists its subclasses as the elements of its value element, named by
            // their serial names. Others, such as a JsonContentPolymorphicSerializer's, have no elements to look in.
            PolymorphicKind.SEALED -> {
                if (descriptor.elementsCount < 2) return null
                val subclasses = descriptor.getElementDescriptor(1)
                (0 until subclasses.elementsCount)
                    .firstOrNull { subclasses.getElementName(it) == name }
                    ?.let(subclasses::getElementDescriptor)
            }
            else -> descriptor.capturedKClass?.let { json.serializersModule.getPolymorphic(it, name)?.descriptor }
        }
    }

    /** Whether [value] is blank where [descriptor] cannot read `""`. */
    private fun isUnreadableBlank(value: JsonPrimitive, descriptor: SerialDescriptor): Boolean =
        value.isString && value.content.isEmpty() && cannotReadBlank(descriptor)

    /**
     * Whether [descriptor] describes a type that cannot read `""`: a number, a date or time, a `Uuid` or a [Story].
     * Every other type gets `""` as it arrived, as one may read it on purpose: an enum may name a value `""`, and a
     * custom serializer may give it a meaning.
     */
    private fun cannotReadBlank(descriptor: SerialDescriptor): Boolean = when {
        descriptor.kind == SerialKind.CONTEXTUAL ->
            json.serializersModule.getContextualDescriptor(descriptor)?.let { cannotReadBlank(it) } ?: false
        descriptor.isInline -> cannotReadBlank(descriptor.getElementDescriptor(0))
        descriptor.kind is PrimitiveKind && descriptor.kind != PrimitiveKind.STRING && descriptor.kind != PrimitiveKind.BOOLEAN -> true
        else -> descriptor.serialName.removeSuffix("?").let { name ->
            name in UNREADABLE_BLANK_NAMES || name.startsWith("kotlinx.datetime.")
        }
    }

    private companion object {
        /** Serial names, besides kotlinx-datetime's, of types that read a string but not a blank one. */
        val UNREADABLE_BLANK_NAMES = setOf("kotlin.time.Instant", "kotlin.uuid.Uuid", "com.storyblok.cdn.schema.Story")
    }
}

private inline fun JsonArray.mapUnlessSame(transform: (JsonElement) -> JsonElement): JsonArray {
    val mapped = map(transform)
    return if (mapped.indices.all { mapped[it] === this[it] }) this else JsonArray(mapped)
}

private inline fun JsonObject.mapValuesUnlessSame(transform: (JsonElement) -> JsonElement): JsonObject {
    val mapped = mapValues { (_, value) -> transform(value) }
    return if (mapped.all { (key, value) -> value === this[key] }) this else JsonObject(mapped)
}
