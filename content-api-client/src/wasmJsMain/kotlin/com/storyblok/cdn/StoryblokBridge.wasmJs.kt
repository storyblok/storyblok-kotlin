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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsString
import kotlin.js.length
import kotlin.js.get
import kotlin.js.toJsArray
import kotlin.js.toJsString
import web.events.EventType
import web.events.addEventListener
import web.events.removeEventListener
import web.messaging.MessageEvent
import web.window.window

private val LOGGER = KtorSimpleLogger("com.storyblok.cdn.VisualEditor")

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

private val MESSAGE = EventType<MessageEvent<JsAny?>>("message")

/**
 * The `initialized` message, which tells the editor to resolve the relations the Content Delivery
 * API request asks for before it sends anything.
 */
internal fun initialized(resolveRelations: String): InitializedAction = unsafeJso {
    action = "initialized"
    config = bridgeParams(resolveRelations)
}

internal external interface InitializedAction : JsAny {
    var action: String
    var config: BridgeParams
}

/**
 * The `edit` message, which opens [block]'s form in the editor.
 *
 * It repeats the handshake's [config][BridgeParams], as the preview bridge does: the editor reads it from both.
 */
internal fun edit(block: EditableBlock, resolveRelations: String): EditAction = unsafeJso {
    action = "edit"
    dataC = unsafeJso {
        name = block.name
        // Left off rather than sent as null, as a page's `_editable` that has no space leaves it off.
        block.space?.let { space = it }
        uid = block.uid
        id = block.id
    }
    config = bridgeParams(resolveRelations)
}

internal external interface EditAction : JsAny {
    var action: String
    var dataC: EditableBlockData
    var config: BridgeParams
}

/** An [EditableBlock], as the `edit` message carries it. */
internal external interface EditableBlockData : JsAny {
    var name: String
    var space: String?
    var uid: String
    var id: String
}

private fun bridgeParams(resolveRelations: String): BridgeParams = unsafeJso {
    this.resolveRelations = resolveRelations
        .takeIf { it.isNotEmpty() }
        ?.split(",")
        ?.map { it.toJsString() }
        ?.toJsArray()
}

/**
 * What the editor is asked to do before it sends anything back. Only the relations are set; the
 * preview bridge's other options all concern DOM this preview does not have.
 */
internal external interface BridgeParams : JsAny {
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
    val content: JsAny
}

/**
 * Any message from the editor, read for the fields that say which block is selected. Which of them is
 * set depends on [action].
 */
internal external interface SelectionEvent : JsAny {
    val action: JsAny?
    /** The block clicked in the editor's form, on `enterComponent`. */
    val componentId: JsAny?
    /** The block added, moved or duplicated, on `addedBlock`, `movedBlock` and `duplicatedBlock`. */
    val blockId: JsAny?
    /** The block whose form the editor opened, on `editedBlok`: its answer to an `edit` message. */
    val blok: EditedBlock?
    /**
     * The blocks from the story's content down to the selected one, on `enterComponent` and
     * `editedBlok` — the path the preview bridge package builds its parent menu from.
     */
    val breadcrumbs: JsArray<Breadcrumb>?
}

internal external interface EditedBlock : JsAny {
    val uid: JsAny?
}

internal external interface Breadcrumb : JsAny {
    @Suppress("PropertyName")
    val _uid: JsAny?
}

/**
 * The block selected once the editor's [event] is handled, given the one [selected] before it: the
 * block the event names, `null` for one that deselects, or [selected] for one that says nothing about
 * selection.
 *
 * These are the events the preview bridge package outlines a block for. It also draws hover hints and
 * highlights, which are not selections and are not read here.
 */
internal fun selectionAfter(event: SelectionEvent, selected: SelectedBlock?): SelectedBlock? =
    when ((event.action as? JsString)?.toString()) {
        "enterComponent" -> event.componentId.uid()?.let { SelectedBlock(it, event.ancestorsOf(it)) } ?: selected
        "editedBlok" -> event.blok?.uid.uid()?.let { SelectedBlock(it, event.ancestorsOf(it)) } ?: selected
        "addedBlock", "movedBlock", "duplicatedBlock" -> event.blockId.uid()?.let { SelectedBlock(it) } ?: selected
        "deselectBlok" -> null
        else -> selected
    }

/** The `_uid`s in this event's breadcrumbs other than [uid]'s own, or none when it carries none. */
private fun SelectionEvent.ancestorsOf(uid: String): List<String> {
    val path = breadcrumbs ?: return emptyList()
    return (0 until path.length).mapNotNull { path[it]?._uid.uid() }.filter { it != uid }
}

private fun JsAny?.uid(): String? = (this as? JsString)?.toString()?.takeIf { it.isNotEmpty() }

/** Whether this page is being previewed in the Visual Editor. */
private fun inVisualEditor(): Boolean =
    // Node and any other host without a DOM, where there is no editor to be previewed by.
    Object.hasOwn(globalThis, "window") &&
        // A top-level window is its own parent.
        inVisualEditor(window.location.search, framed = window.parent != window)

internal actual fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge =
    if (inVisualEditor()) VisualEditor(json, resolveRelations) else NoVisualEditor

// Read once, when the client is created: the editor reloads the preview to switch language, so the
// URL's language cannot change while the page lives. The same checks as the bridge's keep a page
// outside the editor on the app's own language, whatever its URL says.
internal actual fun editorLanguage(): String? =
    if (inVisualEditor()) editorLanguage(window.location.search) else null

/**
 * The page's connection to the Visual Editor, over `Window.postMessage`.
 *
 * Nothing is injected into the page. All it holds is one `window` message listener, which
 * [destroy] removes. Clicks are not listened for here: a Compose canvas is one element, so which
 * block was clicked is for the UI to say, through [openBlock].
 */
private class VisualEditor(private val json: Json, private val resolveRelations: String) : StoryblokBridge {

    private val editorOrigin =
        if ("_storyblok_env=stage" in window.location.search) STAGE_EDITOR_ORIGIN else EDITOR_ORIGIN

    /**
     * The story as the editor currently holds it, still a JavaScript value, for every subscriber —
     * `null` until the author touches something.
     *
     * One listener feeds it rather than one per subscription, so that the newest edit outlives the
     * restart every completed fetch causes.
     */
    private val edits = MutableStateFlow<StoryMetadata?>(null)

    override val selectedBlock = MutableStateFlow<SelectedBlock?>(null)

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    private val listener: (MessageEvent<JsAny?>) -> Unit = listener@{ message ->
        // The editor is the only thing allowed to drive the preview: any page may frame this one
        // and post to it, and `data` is decoded and rendered unchecked.
        if (message.origin != editorOrigin) return@listener

        (message.data as? SelectionEvent)?.let { selectedBlock.value = selectionAfter(it, selectedBlock.value) }

        // `input` is the only event worth reading for content. A save leaves the draft holding exactly what the
        // editor has already pushed here, and publishing or unpublishing does not touch the draft a
        // preview reads — so nothing the other events report needs fetching or reloading.
        //
        // The payload is handed on as it arrived, so that every subscriber reads it at its own
        // resolve level rather than at whichever one subscribed first.
        val input = message.data as? InputBridgeEvent ?: return@listener
        if (input.action != "input") return@listener
        input.story?.let { edits.value = it }
    }

    init {
        // Listening first, so that nothing the editor sends in answer to the handshake is missed.
        window.addEventListener(MESSAGE, listener)
        window.parent.postMessage(initialized(resolveRelations), editorOrigin)
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
                else -> json.decodeEdit(typeInfo.contentSerializer<T>(), edited.content.toJsonElement(resolveLevel))
                    ?.let { story.copy(content = it) }
            }
        }

    override fun openBlock(block: Component) {
        val editable = block.editableBlock() ?: return
        window.parent.postMessage(edit(editable, resolveRelations), editorOrigin)
        // Selected now rather than when the editor answers, as the preview bridge package does, so the
        // tap shows at once.
        selectedBlock.value = SelectedBlock(editable.uid)
    }

    override fun destroy(): Unit = window.removeEventListener(MESSAGE, listener)
}

/**
 * The content of an edit the Visual Editor pushed, decoded as [decodeContent] decodes fetched content, or `null` when it
 * does not decode, so that the preview stays on the last story that did.
 */
internal fun <T> Json.decodeEdit(deserializer: DeserializationStrategy<T>, content: JsonElement): T? = try {
    decodeContent(deserializer, content)
} catch (e: SerializationException) {
    LOGGER.warn("Visual Editor update dropped, preview left on the last story that decoded", e)
    null
}
