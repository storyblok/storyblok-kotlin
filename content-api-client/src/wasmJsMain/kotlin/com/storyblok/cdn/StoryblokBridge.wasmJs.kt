@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.reflect.TypeInfo
import js.globals.globalThis
import js.objects.Object
import js.objects.unsafeJso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import storyblok.preview.bridge.BridgeParams
import storyblok.preview.bridge.InputBridgeEvent
import storyblok.preview.bridge.StoryMetadata
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.toJsArray
import kotlin.js.toJsString
import web.window.window
import storyblok.preview.bridge.StoryblokBridge as PreviewBridge

private val LOGGER = KtorSimpleLogger("com.storyblok.cdn.VisualEditor")

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
 * What to build the bridge with: the relations the Content Delivery API request asks the editor to
 * resolve, and `initOnlyOnce` turned off.
 */
internal fun bridgeParams(resolveRelations: String): BridgeParams = unsafeJso {
    this.resolveRelations = resolveRelations
        .takeIf { it.isNotEmpty() }
        ?.split(",")
        ?.map { it.toJsString() }
        ?.toJsArray()
    this.initOnlyOnce = false
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

    private val bridge = PreviewBridge(bridgeParams(resolveRelations))

    /**
     * The story as the editor currently holds it, still a JavaScript value, for every subscriber —
     * `null` until the author touches something.
     *
     * The preview bridge has no way to remove a single listener — `destroy` is all there is — so
     * registering one per subscription would leak a handler on every screen the app navigates
     * through. It registers once below instead, and subscribers come and go around this flow.
     */
    private val edits = MutableStateFlow<StoryMetadata?>(null)

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

    override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> = edits
        .mapNotNull { edited ->
            // Only the content is taken from the editor. The envelope is the one that was fetched,
            // which is fresher, this flow being restarted by every fetch. It is not a way of
            // avoiding the editor's own fields: the relations it resolves are inlined into the
            // content as story envelopes carrying those same fields, so Story meets them here
            // whatever this one is built from.
            when {
                edited == null || edited.id != story.id.toDouble() -> story
                else -> try {
                    story.copy(
                        content = json.decodeFromJsonElement(
                            typeInfo.contentSerializer<T>(),
                            edited.content.toJsonElement(resolveLevel),
                        ),
                    )
                } catch (e: SerializationException) {
                    LOGGER.warn("Visual Editor update dropped, preview left on the last story that decoded", e)
                    null
                }
            }
        }

    override fun destroy(): Unit = bridge.destroy()
}
