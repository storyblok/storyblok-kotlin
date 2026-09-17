@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import js.globals.globalThis
import js.objects.Object
import js.objects.unsafeJso
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import storyblok.preview.bridge.InputBridgeEvent
import storyblok.preview.bridge.StoryMetadata
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsArray
import kotlin.js.toJsString
import web.window.window
import storyblok.preview.bridge.StoryblokBridge as PreviewBridge

internal actual fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge = when {
    // Node and any other host without a DOM, where there is no editor to be previewed by.
    !Object.hasOwn(globalThis, "window") -> NoVisualEditor
    // The preview token is what the Storyblok SDKs take as "this page is being previewed", rather
    // than `_storyblok` — which names the story being edited and so goes stale as soon as the app
    // navigates within the preview.
    "_storyblok_tk" !in window.location.search -> NoVisualEditor
    else -> VisualEditor(json, resolveRelations)
}

/**
 * The page's connection to the Visual Editor, over `@storyblok/preview-bridge`.
 *
 * Constructing one injects styles and four elements into the page, a `window` message listener and a
 * `document` mousemove listener that runs on every movement — but only inside an iframe, which the
 * preview bridge checks for itself. Outside one it allocates and does nothing, so the preview token
 * is the only gate worth applying before building it.
 */
private class VisualEditor(private val json: Json, resolveRelations: String) : StoryblokBridge {

    /**
     * `initOnlyOnce` and `preventClicks` are left at the bridge's own defaults. `resolveRelations`
     * asks the editor to resolve the same relations the Content Delivery API request asks it to.
     */
    private val bridge = PreviewBridge(unsafeJso {
        this.resolveRelations = resolveRelations
            .takeIf { it.isNotEmpty() }
            ?.split(",")
            ?.map { it.toJsString() }
            ?.toJsArray()
    })

    /**
     * The story as the editor currently holds it, still a JavaScript value, for every subscriber.
     *
     * The preview bridge has no way to remove a single listener — `destroy` is all there is — so
     * registering one per subscription would leak a handler on every screen the app navigates
     * through. It registers once below instead, and subscribers come and go around this flow.
     *
     * Stories are dropped rather than suspending a JavaScript callback that cannot wait: a
     * subscriber that falls behind wants the newest story anyway, which is what
     * [conflate][kotlinx.coroutines.flow.conflate] downstream also asks for.
     */
    private val edits = MutableSharedFlow<StoryMetadata>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        // `input` is the only event worth subscribing to. A save leaves the draft holding exactly
        // what the editor has already pushed here, and publishing or unpublishing does not touch the
        // draft a preview reads — so nothing the other events report needs fetching or reloading.
        //
        // The payload is handed on as it arrived, so that every subscriber reads it at its own
        // resolve level rather than at whichever one subscribed first.
        bridge.on<InputBridgeEvent>("input") { event ->
            event.story?.let { edits.tryEmit(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Component> story(storyId: Long, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> = edits
        .filter { it.id == storyId.toDouble() }
        .mapNotNull { edited ->
            // Dropped rather than thrown: the editor pushes the states between two valid ones as
            // well, and failing here would end live preview for the rest of the session. See the
            // contract on StoryblokBridge.story.
            try {
                json.decodeFromJsonElement(
                    @OptIn(io.ktor.utils.io.InternalAPI::class) typeInfo.serializer() as KSerializer<Story<T>>,
                    edited.toJsonElement(resolveLevel),
                )
            } catch (_: SerializationException) {
                null
            }
        }

    override fun destroy(): Unit = bridge.destroy()
}
