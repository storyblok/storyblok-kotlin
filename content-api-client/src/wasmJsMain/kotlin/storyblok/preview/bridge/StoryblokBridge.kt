@file:OptIn(ExperimentalWasmJsInterop::class)

package storyblok.preview.bridge

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsString

/*
 * External declarations for the `@storyblok/preview-bridge` npm package, carrying only the members
 * this client reads.
 *
 * The package sets the package name, as it does for the declarations JetBrains generates for
 * kotlin-wrappers and for Karakum's own output: `@storyblok/preview-bridge` becomes
 * `storyblok.preview.bridge`. Names match the package's `.d.ts` files, so a reader can check these
 * against the source they describe — and so could a generator, were these ever generated.
 */

/**
 * Storyblok's preview bridge, which talks to the Visual Editor that embeds the page.
 *
 * `@JsModule` on the declaration binds to the module's ES default export, which is the only form in
 * which the package exports this class. `on` is generic in TypeScript too, over the `action`
 * discriminator, from which it picks the payload out of the union of the fourteen events the bridge
 * reports; here the caller names the payload type instead.
 */
@JsModule("@storyblok/preview-bridge")
internal external class StoryblokBridge(props: BridgeParams?) : JsAny {
    fun <T : JsAny> on(event: String, callback: (T) -> Unit)
    fun destroy()
}

/**
 * What the bridge is built with. Only the relations are set by this client; the rest keep their
 * defaults. The bridge forwards them to the editor as it initializes.
 */
internal external interface BridgeParams : JsAny {
    var resolveRelations: JsArray<JsString>?
}

/**
 * The `input` event, carrying the story as the editor currently holds it.
 *
 * `BridgeEvent.d.ts` declares `story` as always present, but nothing checks an external interface
 * against the object that arrives, so nothing else would catch an event without one.
 */
internal external interface InputBridgeEvent : JsAny {
    val story: StoryMetadata?
}

/** A partial description of a story. Only its id is read here; the rest is converted whole. */
internal external interface StoryMetadata : JsAny {
    val id: Double
    val content: JsAny
}
