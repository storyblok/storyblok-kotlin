package com.storyblok.cdn.query

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.reflect.KProperty1
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Builder for the query parameters of the
 * [retrieve multiple stories](https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-multiple-stories)
 * endpoint.
 *
 * Every documented parameter has a dedicated, type-safe entry point. Fields of your own content types are addressed
 * by a property reference of [T], [Story] attributes are addressed by a property reference of [Story]:
 *
 * ```
 * client.stories<Article> {
 *     sortBy(Article::headline)                    // sort_by=content.headline:asc
 *     sortByDescending(Story<*>::publishedAt)      // sort_by=published_at:desc
 *     filter { Article::price greaterThan 100.0 }  // filter_query[price][gt_float]=100.0
 * }
 * ```
 *
 * @param T The [Component] type the query is built for, whose properties the property-reference overloads accept.
 */
@QueryDsl
public class StoriesQuery<T : Component> internal constructor(typeInfo: TypeInfo) : StoryQuery<T>() {

    /** The queried component, which is the `content` of the `Story` [typeInfo] describes. */
    private val fields: SerializedFields = SerializedFields(
        @OptIn(io.ktor.utils.io.InternalAPI::class) typeInfo.serializer().descriptor
            .let { story -> story.getElementDescriptor(story.getElementIndex("content")) },
    )

    /**
     * Filter by the [full slug][Story.fullSlug] prefix, e.g. `blog/posts`, to retrieve the stories of a specific
     * folder. Maps to the `starts_with` parameter.
     */
    public var startsWith: String? = null

    /** Search across all content fields, e.g. `spaceship`. Maps to `search_term`. */
    public var searchTerm: String? = null

    /** Retrieve only start pages (`true`) or only non-start pages (`false`). Maps to `is_startpage`. */
    public var isStartpage: Boolean? = null

    /** Retrieve the stories matching these [full slugs][Story.fullSlug]; supports `*` wildcards. Maps to `by_slugs`. */
    public var bySlugs: List<String> = emptyList()

    /** Exclude the stories matching these [full slugs][Story.fullSlug]; supports `*` wildcards. Maps to `excluding_slugs`. */
    public var excludingSlugs: List<String> = emptyList()

    /** Retrieve the stories with these [uuids][Story.uuid], in the API's own order. Maps to `by_uuids`. */
    public var byUuids: List<Uuid> = emptyList()

    /** Retrieve the stories with these [uuids][Story.uuid], preserving the order given here. Maps to `by_uuids_ordered`. */
    public var byUuidsOrdered: List<Uuid> = emptyList()

    /** Exclude the stories with these [ids][Story.id]. Maps to `excluding_ids`. */
    public var excludingIds: List<Long> = emptyList()

    /** Retrieve only stories carrying *all* of these tags. Maps to `with_tag`. */
    public var withTag: List<String> = emptyList()

    /** Retrieve stories [first published][Story.firstPublishedAt] after this instant. Maps to `first_published_at_gt`. */
    public var firstPublishedAtGreaterThan: Instant? = null

    /** Retrieve stories [first published][Story.firstPublishedAt] before this instant. Maps to `first_published_at_lt`. */
    public var firstPublishedAtLessThan: Instant? = null

    /** Retrieve stories [published][Story.publishedAt] after this instant. Maps to `published_at_gt`. */
    public var publishedAtGreaterThan: Instant? = null

    /** Retrieve stories [published][Story.publishedAt] before this instant. Maps to `published_at_lt`. */
    public var publishedAtLessThan: Instant? = null

    /** Retrieve stories [published][Story.publishedAt] at or after this instant. Maps to `published_at_gte`. */
    public var publishedAtGreaterThanOrEqual: Instant? = null

    /** Retrieve stories [published][Story.publishedAt] at or before this instant. Maps to `published_at_lte`. */
    public var publishedAtLessThanOrEqual: Instant? = null

    /** Retrieve stories [updated][Story.updatedAt] after this instant. Maps to `updated_at_gt`. */
    public var updatedAtGreaterThan: Instant? = null

    /** Retrieve stories [updated][Story.updatedAt] before this instant. Maps to `updated_at_lt`. */
    public var updatedAtLessThan: Instant? = null

    private val filters: FilterQueryBuilder<T> = FilterQueryBuilder(fields)
    private val sorts: MutableList<String> = mutableListOf()

    /**
     * Sort the stories by the content field [field] ascending, e.g. `Article::price`. Call repeatedly, or alongside
     * [sortByDescending], to sort by several fields; they are applied in call order. Maps to `sort_by`, prefixed with
     * `content.` as the API requires for content fields.
     *
     * A numeric field is sorted numerically, so that `100` sorts after `99` rather than before it; every other field
     * is sorted as text, which orders an ISO-8601 date field chronologically. A number held in a text field is
     * therefore sorted as text, since the property is a `String` and its type says nothing more — state the ordering
     * with the [SortAs] overload to sort it numerically.
     *
     * @param field A property of the queried component; it resolves to the serialized field name.
     */
    public fun sortBy(field: KProperty1<in T, *>): Unit =
        sort("content.${fields.nameOf(field)}", ASCENDING, fields.sortTypeOf(field))

    /** Sort the stories by the content field [field] descending, see [sortBy]. */
    public fun sortByDescending(field: KProperty1<in T, *>): Unit =
        sort("content.${fields.nameOf(field)}", DESCENDING, fields.sortTypeOf(field))

    /**
     * Sort the stories by the content field [field] ascending, ordering it as [sortAs] states rather than as the
     * property's type implies — for a number Storyblok holds in a text field, which is otherwise sorted as text.
     *
     * @param field A property of the queried component; it resolves to the serialized field name.
     * @param sortAs The ordering to apply, which the field has to be able to hold.
     * @throws IllegalArgumentException if the field cannot be ordered that way, such as a multi-value field
     * numerically.
     */
    public fun sortBy(field: KProperty1<in T, *>, sortAs: SortAs): Unit =
        sort("content.${fields.nameOf(field, sortAs.operand)}", ASCENDING, sortAs.type)

    /** Sort the stories by the content field [field] descending, ordering it as [sortAs] states, see [sortBy]. */
    public fun sortByDescending(field: KProperty1<in T, *>, sortAs: SortAs): Unit =
        sort("content.${fields.nameOf(field, sortAs.operand)}", DESCENDING, sortAs.type)

    /**
     * Sort the stories ascending by the [Story] attribute [field], e.g. `Story<*>::publishedAt`, rather than by a
     * field of their content. Only some attributes are sortable; the API answers the rest with
     * `Not sortable by this column` rather than the SDK second-guessing which those are.
     */
    @JvmName("sortByAttribute")
    public fun sortBy(field: KProperty1<Story<*>, *>): Unit = sort(STORY_FIELDS.nameOf(field), ASCENDING, type = null)

    /** Sort the stories descending by the [Story] attribute [field], see [sortBy]. */
    @JvmName("sortByAttributeDescending")
    public fun sortByDescending(field: KProperty1<Story<*>, *>): Unit =
        sort(STORY_FIELDS.nameOf(field), DESCENDING, type = null)

    private fun sort(path: String, direction: String, type: String?) {
        sorts += buildString {
            append(path)
            append(':')
            append(direction)
            if (type != null) {
                append(':')
                append(type)
            }
        }
    }

    /**
     * Configure advanced [filter queries](https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries)
     * targeting the fields of your content types, e.g.:
     *
     * ```
     * filter {
     *     Article::headline like "*space*"
     *     Article::price greaterThan 100.0
     *     Article::categories anyIn listOf("solar-system", "space-exploration")
     * }
     * ```
     */
    public fun filter(block: FilterQueryBuilder<T>.() -> Unit) {
        filters.block()
    }

    override fun generate(): Map<String, String> = buildMap {
        startsWith?.let { put("starts_with", it) }
        searchTerm?.let { put("search_term", it) }
        fields.technicalName?.let { put("content_type", it) }
        isStartpage?.let { put("is_startpage", it.toString()) }
        putJoined("by_slugs", bySlugs)
        putJoined("excluding_slugs", excludingSlugs)
        putJoined("by_uuids", byUuids)
        putJoined("by_uuids_ordered", byUuidsOrdered)
        putJoined("excluding_ids", excludingIds)
        putJoined("with_tag", withTag)
        putAll(super.generate())
        firstPublishedAtGreaterThan?.let { put("first_published_at_gt", it.toString()) }
        firstPublishedAtLessThan?.let { put("first_published_at_lt", it.toString()) }
        publishedAtGreaterThan?.let { put("published_at_gt", it.toString()) }
        publishedAtLessThan?.let { put("published_at_lt", it.toString()) }
        publishedAtGreaterThanOrEqual?.let { put("published_at_gte", it.toString()) }
        publishedAtLessThanOrEqual?.let { put("published_at_lte", it.toString()) }
        updatedAtGreaterThan?.let { put("updated_at_gt", it.toString()) }
        updatedAtLessThan?.let { put("updated_at_lt", it.toString()) }
        if (sorts.isNotEmpty()) put("sort_by", sorts.joinToString(","))
        putAll(filters.parameters)
    }

}

/**
 * The ordering to apply to a `sort_by` field, for the [StoriesQuery.sortBy] overload that states it rather than
 * deriving it from the property's type. Storyblok serializes a number field as text, so a field holding one is as
 * likely to be modelled as a `String` as a `Long` or a `Double`, and its type cannot settle the ordering.
 */
public enum class SortAs(internal val type: String?, internal val operand: Operand?) {

    /** Order numerically, without a decimal point, so that `100` sorts after `99`. Appends `:int`. */
    WholeNumber("int", Operand.WholeNumber),

    /** Order numerically, with a decimal point. Appends `:float`. */
    DecimalNumber("float", Operand.DecimalNumber),

    /** Order as text, which is the API's default and orders an ISO-8601 date chronologically. Appends nothing. */
    Text(null, null),
}

/** Resolves a [Story] attribute to the name the API knows it by, the same way a component's fields are resolved. */
private val STORY_FIELDS = SerializedFields(serializer<Story<Component>>().descriptor)

private const val ASCENDING: String = "asc"

private const val DESCENDING: String = "desc"
