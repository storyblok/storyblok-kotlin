package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

/**
 * Storyblok's preview bridge: a page's connection to the Visual Editor that embeds it.
 *
 * Only the web target can be embedded in one, so every other target gets [NoVisualEditor] — which is
 * what leaves [StoryblokClient.story] completing after its fetch exactly as it did before.
 *
 * A [StoryblokClient] holds one for its lifetime and [destroys][destroy] it with itself. That
 * outlives any single [story][StoryblokClient.story] collection, which come and go as the app
 * navigates, and the connection has to survive that.
 */
internal interface StoryblokBridge {

    /**
     * [story] as the app should show it: what was fetched, then that story carrying the content the
     * author is editing.
     *
     * Only the content comes from the editor, so slug and name edits do not show here; the editor
     * reloads the preview for those. Relations arrive already resolved. The newest edit is retained,
     * so the restart that every completed fetch causes re-emits it rather than waiting for the next
     * keystroke. An update that cannot be decoded is dropped and the flow stays open, because the
     * editor also pushes the half-finished states between two valid ones.
     *
     * @param story The story as fetched: the first thing emitted and the envelope under every later
     * one. Updates for any other story are ignored.
     * @param resolveLevel How deep to follow a value that is already being read.
     */
    fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>>

    /**
     * Removes whatever the connection holds — on the web target the bridge's event listeners,
     * timers and injected DOM.
     *
     * The editor is told nothing: a page may be torn down without the preview ending.
     */
    fun destroy()
}

internal object NoVisualEditor : StoryblokBridge {
    override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> = flowOf(story)
    override fun destroy(): Unit = Unit
}

/**
 * Opens the page's bridge, asking the editor to resolve [resolveRelations] — formatted as the
 * `resolve_relations` request parameter formats them — in everything it sends back, and decoding
 * what it sends with [json].
 *
 * The relations are fixed here rather than per subscription because the bridge posts them to the
 * editor as it initializes. A page shows one story fetched with one set of relations, so there is
 * nothing to vary them by.
 *
 * At most one bridge should be open per page: the preview bridge's own `initOnlyOnce` makes a second
 * one either silently do nothing or reset the first one's listeners.
 */
internal expect fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge

@Suppress("UNCHECKED_CAST")
internal fun <T : Component> TypeInfo.contentSerializer(): KSerializer<T> =
    serializer(kotlinType!!.arguments.first().type!!) as KSerializer<T>
