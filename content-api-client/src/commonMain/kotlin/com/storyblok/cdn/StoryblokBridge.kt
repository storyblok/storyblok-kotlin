package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
     * The story as it is edited, pushed to the app rather than fetched from the Content Delivery
     * API, decoded as the [Story] type [typeInfo] describes.
     *
     * The editor sends its relations already resolved — it is given the same `resolve_relations` the
     * request is — so there is nothing left to resolve against the response this replaces.
     *
     * A payload that cannot be decoded is dropped and the flow stays open, leaving the collector on
     * the last story that decoded. This is the opposite of the fetch path, which fails on one, and
     * deliberately so: the editor pushes a story on every keystroke, so it also pushes the states
     * between two valid ones — a number field cleared before it is retyped, a relation unlinked
     * before the next is picked. Failing there would end live preview for the rest of the session
     * over an edit the author was midway through. Nothing is hidden by this, because a story the
     * client genuinely cannot model fails the fetch that opens the flow, which does throw.
     *
     * @param storyId The story to receive updates for. Updates for any other story are dropped.
     * @param resolveLevel How deep to follow a value that is already being read, as it is for
     * relations.
     */
    fun <T : Component> story(storyId: Long, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>>

    /**
     * Removes whatever the connection holds — on the web target the bridge's event listeners,
     * timers and injected DOM.
     *
     * The editor is told nothing: a page may be torn down without the preview ending.
     */
    fun destroy()
}

/** No editor on the other side: what every target but the web one has, and the web one outside a preview. */
internal object NoVisualEditor : StoryblokBridge {
    override fun <T : Component> story(storyId: Long, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> = emptyFlow()
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
