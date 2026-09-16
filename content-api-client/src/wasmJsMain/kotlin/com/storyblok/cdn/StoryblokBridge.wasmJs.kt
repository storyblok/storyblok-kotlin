@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import js.globals.globalThis
import js.objects.Object
import js.objects.unsafeJso
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsString
import kotlin.js.toJsArray
import kotlin.js.toJsString
import web.events.Event
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.messaging.MessageEvent
import web.window.window

/*
 * The Visual Editor talks to an embedded preview over `Window.postMessage`, and that is all this
 * needs. `@storyblok/preview-bridge` wraps the same two messages, but everything else it does —
 * outlining blocks, hover hints, the selection overlay and its menus — reads and writes per-block
 * DOM that a Compose canvas does not have, and it does some of that work on every mouse movement.
 */

/**
 * The editor's origin, which is both where the handshake is sent and the only origin a story is
 * accepted from.
 *
 * Always `https`: the editor is served over it, and `postMessage` drops a message whose target
 * origin does not match the parent exactly.
 */
private const val EDITOR_ORIGIN = "https://app.storyblok.com"

/** Storyblok's staging editor, which a preview opts into through the URL the editor gave it. */
private const val STAGE_EDITOR_ORIGIN = "https://app-beta.storyblok.com"

/** The `initialized` message, which tells the editor what to resolve before it sends anything. */
private fun initialized(resolveRelations: String): JsAny = unsafeJso<InitializedAction> {
    action = "initialized"
    config = unsafeJso {
        this.resolveRelations = resolveRelations
            .takeIf { it.isNotEmpty() }
            ?.split(",")
            ?.map { it.toJsString() }
            ?.toJsArray()
    }
}

private external interface InitializedAction : JsAny {
    var action: String
    var config: BridgeParams
}

/**
 * What the editor is asked to do before it sends anything back. Only the relations are set; the
 * preview bridge's other options all concern DOM this preview does not have.
 */
private external interface BridgeParams : JsAny {
    var resolveRelations: JsArray<JsString>?
}

/** The `input` message, carrying the story as the editor currently holds it. */
private external interface InputBridgeEvent : JsAny {
    val action: String?
    val story: StoryMetadata?
}

/** A partial description of a story. Only its id is read here; the rest is converted whole. */
private external interface StoryMetadata : JsAny {
    val id: Double
}

internal actual fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge = when {
    // Node and any other host without a DOM, where there is no editor to be previewed by.
    !Object.hasOwn(globalThis, "window") -> NoVisualEditor
    // The preview token is what the Storyblok SDKs take as "this page is being previewed", rather
    // than `_storyblok` — which names the story being edited and so goes stale as soon as the app
    // navigates within the preview.
    "_storyblok_tk" !in window.location.search -> NoVisualEditor
    // A top-level window is its own parent, and there is no editor on the other side of one.
    window.parent == window -> NoVisualEditor
    else -> VisualEditor(json, resolveRelations)
}

/**
 * The page's connection to the Visual Editor, over `Window.postMessage`.
 *
 * Nothing is injected into the page and nothing is held beyond the listener each subscriber adds,
 * so there is nothing to tear down that outlives a collection.
 */
private class VisualEditor(private val json: Json, resolveRelations: String) : StoryblokBridge {

    private val editorOrigin =
        if ("_storyblok_env=stage" in window.location.search) STAGE_EDITOR_ORIGIN else EDITOR_ORIGIN

    init {
        window.parent.postMessage(initialized(resolveRelations), editorOrigin)
    }

    @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    override fun <T : Component> story(storyId: Long, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
        callbackFlow {
            val listener: (MessageEvent<JsAny?>) -> Unit = listener@{ message ->
                // The editor is the only thing allowed to drive the preview: any page may frame this
                // one and post to it, and `data` is decoded and rendered unchecked.
                if (message.origin != editorOrigin) return@listener

                val input = message.data as? InputBridgeEvent ?: return@listener
                if (input.action != "input") return@listener

                // The editor edits one story while the app may have navigated to another.
                val edited = input.story?.takeIf { it.id == storyId.toDouble() } ?: return@listener

                // Read straight from the JavaScript object: no JSON string is created on the way.
                trySend(
                    json.decodeFromJsonElement(
                        @OptIn(io.ktor.utils.io.InternalAPI::class) typeInfo.serializer() as KSerializer<Story<T>>,
                        edited.toJsonElement(resolveLevel),
                    )
                )
            }

            window.addEventListener(EventType<MessageEvent<JsAny?>>("message"), listener)
            awaitClose { window.removeEventListener(EventType<MessageEvent<JsAny?>>("message"), listener) }
        }

    override fun destroy(): Unit = Unit
}
