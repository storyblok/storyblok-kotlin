package com.storyblok.cdn

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
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
 * Decodes [element] as [deserializer], reading a blank value of a field that does not hold text as a value that is not
 * set, and reporting a value that does not parse as a [SerializationException].
 *
 * Storyblok sends a date, number or story option left blank as `""`, rather than leaving it out or sending `null`, and
 * `""` is neither a date, a number nor a uuid. Such a value is left out of an object when its property has a default,
 * which then applies, and is otherwise `null` for a nullable property. A required property still fails, and a `String`
 * keeps its `""`, which is a text field's blank value.
 *
 * A serializer that parses text, such as `Instant`'s or `Uuid`'s, rejects a value it cannot parse with an
 * [IllegalArgumentException] rather than a [SerializationException]. Both are a mismatch between the content and the
 * types decoding it, so both are reported as one, rather than as the failed request a [StoryblokClientException] is.
 */
internal fun <T> Json.decodeContent(deserializer: DeserializationStrategy<T>, element: JsonElement): T =
    try {
        decodeFromJsonElement(deserializer, withoutBlanks(element, deserializer.descriptor))
    } catch (e: SerializationException) {
        throw e
    } catch (e: IllegalArgumentException) {
        throw SerializationException(e.message, e)
    }

/** [element] with the blank values the [descriptor] it decodes as cannot read taken out, as [decodeContent] describes. */
internal fun Json.withoutBlanks(element: JsonElement, descriptor: SerialDescriptor): JsonElement {
    // The serializer a contextual descriptor stands for, or the value an inline class wraps, is what reads the JSON.
    // Either is described as not nullable, so the nullability of the property it fills is carried over.
    val reader = when {
        descriptor.kind == SerialKind.CONTEXTUAL -> serializersModule.getContextualDescriptor(descriptor) ?: return element
        descriptor.isInline -> descriptor.getElementDescriptor(0)
        else -> null
    }
    if (reader != null) return withoutBlanks(element, if (descriptor.isNullable) reader.nullable else reader)
    return when (element) {
        is JsonPrimitive -> if (descriptor.isNullable && isUnreadableBlank(element, descriptor)) JsonNull else element
        is JsonArray -> when (descriptor.kind) {
            StructureKind.LIST -> JsonArray(element.map { withoutBlanks(it, descriptor.getElementDescriptor(0)) })
            else -> element
        }
        is JsonObject -> when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> objectWithoutBlanks(element, descriptor)
            StructureKind.MAP -> JsonObject(element.mapValues { (_, value) -> withoutBlanks(value, descriptor.getElementDescriptor(1)) })
            is PolymorphicKind -> subclassDescriptor(element, descriptor)?.let { objectWithoutBlanks(element, it) } ?: element
            else -> element
        }
    }
}

private fun Json.objectWithoutBlanks(element: JsonObject, descriptor: SerialDescriptor): JsonObject {
    val indices = elementIndices(descriptor)
    return JsonObject(buildMap {
        for ((key, value) in element) {
            val index = indices[key]
            if (index == null) {
                put(key, value)
                continue
            }
            val elementDescriptor = descriptor.getElementDescriptor(index)
            when {
                value !is JsonPrimitive || !isUnreadableBlank(value, elementDescriptor) -> put(key, withoutBlanks(value, elementDescriptor))
                // Left out, so the property's default applies.
                descriptor.isElementOptional(index) -> Unit
                // `null` for a nullable property, and otherwise left for decoding to report, a required property having
                // no value to fall back on.
                else -> put(key, withoutBlanks(value, elementDescriptor))
            }
        }
    })
}

/**
 * The index of each of [descriptor]'s elements by every key decoding accepts for it. An element's own name takes
 * precedence over another's alternative name, as it does in decoding.
 */
private fun Json.elementIndices(descriptor: SerialDescriptor): Map<String, Int> = buildMap {
    for (index in 0 until descriptor.elementsCount) {
        val name = descriptor.getElementName(index)
        put(configuration.namingStrategy?.serialNameForJson(descriptor, index, name) ?: name, index)
    }
    if (!configuration.useAlternativeNames) return@buildMap
    for (index in 0 until descriptor.elementsCount) {
        descriptor.getElementAnnotations(index)
            .filterIsInstance<JsonNames>()
            .forEach { annotation -> annotation.names.forEach { getOrPut(it) { index } } }
    }
}

/** The descriptor of the subclass [element] names in its class discriminator, among those [descriptor] decodes. */
private fun Json.subclassDescriptor(element: JsonObject, descriptor: SerialDescriptor): SerialDescriptor? {
    val discriminator = descriptor.annotations.filterIsInstance<JsonClassDiscriminator>().firstOrNull()?.discriminator
        ?: configuration.classDiscriminator
    val name = (element[discriminator] as? JsonPrimitive)?.contentOrNull ?: return null
    return when (descriptor.kind) {
        // A sealed class lists its subclasses as the elements of its value element, named by their serial names.
        PolymorphicKind.SEALED -> descriptor.getElementDescriptor(1).let { subclasses ->
            (0 until subclasses.elementsCount)
                .firstOrNull { subclasses.getElementName(it) == name }
                ?.let(subclasses::getElementDescriptor)
        }
        else -> descriptor.capturedKClass?.let { serializersModule.getPolymorphic(it, name)?.descriptor }
    }
}

/**
 * Whether [value] is blank where [descriptor] does not read text: a `String` property reads `""` as its value, and a
 * `JsonElement` one keeps it as it arrived.
 */
private fun Json.isUnreadableBlank(value: JsonPrimitive, descriptor: SerialDescriptor): Boolean =
    value.isString && value.content.isEmpty() && !readsText(descriptor)

private fun Json.readsText(descriptor: SerialDescriptor): Boolean = when {
    descriptor.kind == SerialKind.CONTEXTUAL ->
        serializersModule.getContextualDescriptor(descriptor)?.let { readsText(it) } ?: true
    descriptor.isInline -> readsText(descriptor.getElementDescriptor(0))
    else -> descriptor.serialName.removeSuffix("?").let { it == "kotlin.String" || it.startsWith("kotlinx.serialization.json.") }
}
