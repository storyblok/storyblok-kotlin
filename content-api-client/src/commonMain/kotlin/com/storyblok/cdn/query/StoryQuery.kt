package com.storyblok.cdn.query

import com.storyblok.cdn.schema.Component

/**
 * Builder for the query parameters of the
 * [retrieve a single story](https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-a-single-story)
 * endpoint.
 *
 * @param T The [Component] type of the story's content.
 */
@QueryDsl
public open class StoryQuery<T : Component> internal constructor() : Query() {

    /** Omit these content fields from the response to reduce its size. Maps to `excluding_fields`. */
    public var excludingFields: List<String> = emptyList()

    /** Retrieve the story as it appears in the [release](https://www.storyblok.com/docs/guide/essentials/releases) with this id. Maps to `from_release`. */
    public var fromRelease: String? = null

    override fun generate(): Map<String, String> = buildMap {
        putJoined("excluding_fields", excludingFields)
        fromRelease?.let { put("from_release", it) }
    }
}
