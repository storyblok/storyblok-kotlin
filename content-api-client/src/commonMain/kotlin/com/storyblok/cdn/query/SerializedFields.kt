package com.storyblok.cdn.query

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.reflect.KProperty1
import kotlin.time.Instant

/**
 * Maps a property reference to the name its value carries in the API's JSON.
 *
 * A field whose Storyblok name differs from the Kotlin property is named with `@JsonNames`, which — unlike
 * `@SerialName` — reaches the [SerialDescriptor], and which also lets the field decode under that name:
 *
 * ```
 * @JsonNames("release_date") val releaseDate: Instant
 * ```
 *
 * A field renamed with `@SerialName` is not resolvable, because the annotation is invisible here,
 * and is reported as a field the component does not have — except in one case: if its serial name
 * happens to be another property's name, a reference to that property passes the check and
 * addresses the wrong field.
 *
 * ```
 * @SerialName("body") val richBody: String   // serialized as "body"
 * @SerialName("body_html") val body: String  // Foo::body passes the check and filters richBody
 * ```
 *
 * Naming fields with `@JsonNames` avoids this entirely; a field that must keep `@SerialName` is reachable by writing
 * the parameter out with [Query.parameter], e.g. `parameter("filter_query[body_html][like]", "*space*")`.
 */
internal class SerializedFields(descriptor: SerialDescriptor) {

    /**
     * The descriptor of the queried component, or `null` when there is not exactly one: a story typed as the abstract
     * [Component] describes the polymorphic envelope rather than any single component.
     */
    private val component: SerialDescriptor? = descriptor.takeIf { it.kind !is PolymorphicKind }

    /**
     * The technical name of the queried component — its `@SerialName` — or `null` when the query is not typed to one,
     * since an abstract component describes several content types rather than naming one.
     */
    val technicalName: String? get() = component?.serialName

    /**
     * The serialized kind of each field of the queried component, or `null` when they cannot be enumerated — a
     * component serialized by hand need not describe its fields, and there is then nothing to check a reference
     * against.
     */
    private val fields: Map<String, SerialDescriptor>? =
        component
            ?.takeIf { it.elementsCount > 0 }
            ?.let { descriptor ->
                (0 until descriptor.elementsCount).associate {
                    descriptor.getElementName(it) to descriptor.getElementDescriptor(it)
                }
            }

    /**
     * The sole `@JsonNames` alias of each field, by property name. A field declaring several names gets none of
     * them, and one declaring none is named by the property itself.
     */
    private val soleAlias: Map<String, String> = buildMap {
        val descriptor = component ?: return@buildMap
        for (i in 0 until descriptor.elementsCount) {
            descriptor.getElementAnnotations(i)
                .filterIsInstance<JsonNames>()
                .flatMap { it.names.asList() }
                .singleOrNull()
                ?.let { alias -> put(descriptor.getElementName(i), alias) }
        }
    }

    /**
     * The name Storyblok knows [property] by: its sole `@JsonNames` alias, or the property's own name. Fields are
     * expected to be named with `@JsonNames`, which reaches the descriptor; a field renamed with `@SerialName`
     * instead is not a name the component has and is reported as such.
     *
     * @throws IllegalArgumentException if the component has no such field, or, when an [operand] is given, if the
     * field cannot be compared with it.
     */
    fun nameOf(property: KProperty1<*, *>, operand: Operand? = null): String {
        val fields = fields ?: return property.name
        val field = fields[property.name]
        require(field != null) {
            "'${property.name}' is not a serialized field of the queried component. Name it with " +
                "@JsonNames(\"<its Storyblok name>\") if it differs from the property. " +
                "Known fields: ${fields.keys.sorted().joinToString()}."
        }
        require(operand == null || operand.accepts(field)) {
            "'${property.name}' serializes as ${field.serialName}, which this operation does not apply to; " +
                "Storyblok applies it to ${operand?.fields}."
        }
        return soleAlias[property.name] ?: property.name
    }

    /**
     * The `sort_by` type of [property], or `null` when the API's default text ordering applies — which is also the
     * right ordering for a date, since it serializes as an ISO-8601 string.
     */
    fun sortTypeOf(property: KProperty1<*, *>): String? = when (fields?.get(property.name)?.kind) {
        in INTEGRAL -> "int"
        in DECIMAL -> "float"
        else -> null
    }
}

/** The kind of value a comparison operation compares against, and the fields it applies to. */
internal enum class Operand(val fields: String) {
    WholeNumber("number fields and text fields holding an integer value"),
    DecimalNumber("number fields and text fields holding a float value"),
    Timestamp("date fields, which serialize as text"),
    Multiple("multi-value fields, such as Option or References fields"),
    ;

    fun accepts(field: SerialDescriptor): Boolean = when (this) {
        // A text field qualifies for either numeric operation: Storyblok serializes a number field's value as text,
        // so the field these target is as often modelled as a String as it is as a Long or a Double.
        WholeNumber -> field.isText || field.kind in INTEGRAL
        DecimalNumber -> field.isText || field.kind in DECIMAL
        // A date serializes as text whichever serializer models it, so its own kind is all there is to go on.
        Timestamp -> field.kind == PrimitiveKind.STRING
        // A multi-value field always arrives as a JSON array, so unlike a number its Kotlin type is decisive.
        Multiple -> field.kind == StructureKind.LIST
    }

}

private val INTEGRAL: Set<SerialKind> =
    setOf(PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG)

private val DECIMAL: Set<SerialKind> = setOf(PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE)

/** Tells a plain text field from a type that merely serializes as text, such as an `Instant`. */
private val SerialDescriptor.isText: Boolean
    get() = kind == PrimitiveKind.STRING && serialName.removeSuffix("?") == "kotlin.String"
