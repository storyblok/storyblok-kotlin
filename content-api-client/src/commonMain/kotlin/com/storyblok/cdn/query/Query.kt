package com.storyblok.cdn.query

/**
 * DSL marker for the [StoriesQuery] and [FilterQueryBuilder] builders, preventing accidental access to an outer
 * builder's receiver from within a nested builder block.
 */
@DslMarker
public annotation class QueryDsl

/**
 * Base of the query builders, carrying what every Content Delivery API endpoint has in common: nothing beyond a way
 * to set a parameter directly. `token`, `version` and `cv` are the other parameters every endpoint takes, and they
 * belong to the Ktor plugin's configuration rather than to a single query.
 */
@QueryDsl
public abstract class Query internal constructor() {

    private val overrides: MutableMap<String, String?> = mutableMapOf()

    /**
     * Set a query parameter directly, for forward compatibility with parameters this SDK version does not know about
     * yet, and to override any the SDK sends on your behalf. A `null` [value] removes the parameter instead, and
     * naming one that would not be sent anyway does nothing.
     *
     * A parameter set here replaces the one this query would otherwise generate under the same name, including those
     * the SDK manages itself. Overriding those is at your own risk and can break the features that rely on them:
     *
     * - `page` and `per_page` come from the `PagingConfig` passed to [com.storyblok.cdn.StoryblokClient.stories];
     *   changing `per_page` desynchronises the page numbers the Paging library requests from the ones the HTTP
     *   cache holds.
     * - `resolve_relations` and `resolve_level` come from the client's registered relations and its `resolveLevel`;
     *   narrowing them leaves relation fields unresolved, which fails deserialization for non-nullable ones.
     * - `content_type` is derived from the component the query is typed to; overriding it returns stories that need
     *   not deserialize into that type.
     *
     * `token`, `version`, `cv`, `language` and `fallback_lang` are a separate case: this query never generates them,
     * the Ktor plugin applies them as request defaults instead. A value set here still wins, for this query alone,
     * since a default only fills in a parameter the request does not already carry — but `null` cannot reach them.
     * Removing a parameter the query never generated changes nothing, and the plugin's default applies as usual.
     */
    public fun parameter(name: String, value: Any?) {
        overrides[name] = value?.toString()
    }

    /** The parameters this query states, before [parameter] overrides are applied. */
    internal abstract fun generate(): Map<String, String>

    /**
     * The parameters to send. A name set through [parameter] replaces the generated one, which is also what the API
     * does with a repeated query parameter — it answers to the last. A name set to `null` drops out entirely.
     */
    internal fun build(): Map<String, String> = buildMap {
        putAll(generate())
        overrides.forEach { (name, value) -> if (value != null) put(name, value) else remove(name) }
    }

    internal fun MutableMap<String, String>.putJoined(name: String, values: Collection<Any>) {
        if (values.isNotEmpty()) put(name, values.joinToString(","))
    }
}
