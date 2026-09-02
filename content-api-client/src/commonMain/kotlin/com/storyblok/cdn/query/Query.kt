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

    private val overrides: MutableMap<String, String> = mutableMapOf()

    /**
     * Set a query parameter directly, for forward compatibility with parameters this SDK version does not know about
     * yet.
     *
     * Note you can also override any parameter the SDK sends on your behalf - do this at your own risk as they can
     * break the features that rely on them.
     */
    public fun parameter(name: String, value: Any) {
        overrides[name] = value.toString()
    }

    /** The parameters this query states, before [parameter] overrides are applied. */
    internal abstract fun generate(): Map<String, String>

    internal fun build(): Map<String, String> = generate() + overrides

    internal fun MutableMap<String, String>.putJoined(name: String, values: Collection<Any>) {
        if (values.isNotEmpty()) put(name, values.joinToString(","))
    }
}
