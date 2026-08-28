package com.storyblok.cdn.query

import com.storyblok.cdn.schema.Component
import kotlin.reflect.KProperty1
import kotlin.time.Instant

/**
 * Formats a `Double` operand identically on every platform: Kotlin/JS prints a whole number as `100`, while
 * Kotlin/JVM and Kotlin/Native print `100.0`. The API accepts either, but the query — and so the HTTP cache key it
 * produces — should not depend on the target.
 */
internal fun Double.filterOperand(): String {
    val whole = toLong()
    return if (whole.toDouble() == this) "$whole.0" else toString()
}

/**
 * Infix DSL for building [filter queries](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries).
 *
 * Each operation appends a `filter_query[<field>][<operation>]=<value>` parameter, naming the field by a property
 * reference of [T] resolved to its serialized name. Collection values are joined with commas, as the API expects.
 *
 * The operations do not constrain the property's Kotlin type: Storyblok serializes numbers as JSON strings, so a
 * numeric field is as likely to be modelled as a `String` as a `Double`, and requiring one would rule out valid
 * schemas. The comparison operations are the exception — they check the field can hold the operand they are given.
 *
 * A property reference addresses one of [T]'s own fields, so
 * [nested blocks and fields](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/nested-blocks-and-fields)
 * — which the API addresses with a dotted path, `seo.description` or `body.0.name` — have no entry point here, and a
 * reference to the containing field alone filters on the whole object rather than on a property inside it. Write
 * those out with [Query.parameter]:
 *
 * ```
 * parameter("filter_query[seo.description][is]", "not_empty")
 * ```
 *
 * @param T The [Component] type the enclosing query is built for.
 */
@QueryDsl
public class FilterQueryBuilder<T : Component> internal constructor(private val fields: SerializedFields) {

    internal val parameters: MutableMap<String, String> = mutableMapOf()

    private fun KProperty1<in T, *>.filter(operation: String, value: String, operand: Operand? = null) {
        this@FilterQueryBuilder.parameters["filter_query[${fields.nameOf(this, operand)}][$operation]"] =
            value
    }

    /** [like](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-like): the field value matches the given pattern (supports `*` wildcards). */
    public infix fun KProperty1<in T, *>.like(pattern: String): Unit = filter("like", pattern)

    /** [not_like](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-like): the field value does not match the given pattern. */
    public infix fun KProperty1<in T, *>.notLike(pattern: String): Unit = filter("not_like", pattern)

    /** [is](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-is): the field value is of the given [value type][Is]. */
    public infix fun KProperty1<in T, *>.`is`(value: Is): Unit = filter("is", value.value, value.operand)

    /** [in](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-in): the field value matches any of the given values. */
    public infix fun KProperty1<in T, *>.isIn(values: Collection<String>): Unit =
        filter("in", values.joinToString(","))

    /** [not_in](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-in): the field value matches none of the given values. */
    public infix fun KProperty1<in T, *>.notIn(values: Collection<String>): Unit =
        filter("not_in", values.joinToString(","))

    /** [all_in_array](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-all-in-array): the array field contains all of the given values. */
    public infix fun KProperty1<in T, *>.allIn(values: Collection<String>): Unit =
        filter("all_in_array", values.joinToString(","), Operand.Multiple)

    /** [any_in_array](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-any-in-array): the array field contains any of the given values. */
    public infix fun KProperty1<in T, *>.anyIn(values: Collection<String>): Unit =
        filter("any_in_array", values.joinToString(","), Operand.Multiple)

    /**
     * [gt_int](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-int): the integer
     * field value is greater than the given value. Applies to values written without a decimal point, whether the
     * field is a number field or a text field.
     */
    public infix fun KProperty1<in T, *>.greaterThan(value: Long): Unit =
        filter("gt_int", value.toString(), Operand.WholeNumber)

    /**
     * [gt_float](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-float): the float
     * field value is greater than the given value. Applies to values written with a decimal point, whether the field
     * is a number field or a text field.
     */
    public infix fun KProperty1<in T, *>.greaterThan(value: Double): Unit =
        filter("gt_float", value.filterOperand(), Operand.DecimalNumber)

    /** [gt_date](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-date): the date field value is after the given instant. */
    public infix fun KProperty1<in T, *>.greaterThan(value: Instant): Unit =
        filter("gt_date", value.toString(), Operand.Timestamp)

    /**
     * [lt_int](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-int): the integer
     * field value is less than the given value. Applies to values written without a decimal point, whether the field
     * is a number field or a text field.
     */
    public infix fun KProperty1<in T, *>.lessThan(value: Long): Unit =
        filter("lt_int", value.toString(), Operand.WholeNumber)

    /**
     * [lt_float](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-float): the float
     * field value is less than the given value. Applies to values written with a decimal point, whether the field is
     * a number field or a text field.
     */
    public infix fun KProperty1<in T, *>.lessThan(value: Double): Unit =
        filter("lt_float", value.filterOperand(), Operand.DecimalNumber)

    /** [lt_date](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-date): the date field value is before the given instant. */
    public infix fun KProperty1<in T, *>.lessThan(value: Instant): Unit =
        filter("lt_date", value.toString(), Operand.Timestamp)
}

/**
 * The value types the [is][FilterQueryBuilder.is] operation matches, which are the eight the API documents. A value
 * type it may add later is reachable by writing the parameter out with [Query.parameter], e.g.
 * `parameter("filter_query[categories][is]", "<its name>")`.
 */
public enum class Is(internal val value: String, internal val operand: Operand?) {

    /** The field has no value. */
    Empty("empty", null),

    /** The field has a value. */
    NotEmpty("not_empty", null),

    /** The boolean field is `true`. */
    True("true", null),

    /** The boolean field is `false`. */
    False("false", null),

    /** The multi-value field holds no entries. */
    EmptyArray("empty_array", Operand.Multiple),

    /** The multi-value field holds at least one entry. */
    NotEmptyArray("not_empty_array", Operand.Multiple),

    /** The field is absent from the content. */
    Null("null", null),

    /** The field is present in the content. */
    NotNull("not_null", null),
}
