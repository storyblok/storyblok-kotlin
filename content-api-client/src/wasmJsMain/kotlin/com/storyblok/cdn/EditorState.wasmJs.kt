@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import js.objects.unsafeJso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.js

/**
 * Everything the Visual Editor's messages say other than what the story holds: which block is
 * selected, the menu the preview draws on it, and the blocks the editor points the author at.
 *
 * Apart from the window it is posted to, so that what each message does can be tested without one.
 * Which block is selected, and when it is to be brought into view, is [selection]'s to say; this adds
 * its menu, which leaves its [reveal][SelectedBlock.reveal] as it was.
 */
internal class EditorState(private val selection: EditorSelection = EditorSelection()) {

    /** What the editor has said about itself so far, replaced as each message adds to it. */
    private var context = EditorContext()

    val selectedBlock: MutableStateFlow<SelectedBlock?> get() = selection.selected

    val hints = MutableStateFlow(BlockHints())

    /** The content of the story last edited, which the next edit to it is compared with, and that story's id. */
    private var content: Pair<String?, JsonElement>? = null

    private var edits = 0

    /** Takes in a message from the editor, of any action. */
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun receive(data: JsAny?) {
        val event = data as? SelectionEvent ?: return
        val action = (event.action as? JsString)?.toString() ?: return
        if (action == "input") {
            (event as StoryEvent).story?.let(::edited)
            selection.handle(event)
            return
        }
        val message = data.toJsonElement(resolveLevel = 1) as? JsonObject ?: return
        context = context.after(message)
        selection.handle(event)
        selectedBlock.value = selectedBlock.value?.let { selected ->
            context.menuOf(message, selected.uid)?.let { selected.copy(menu = it) } ?: selected
        }
        hints.value = hintsAfter(message, hints.value)
    }

    /** Selects [block], which a tap has just asked the editor to open, keeping its menu if it is selected already. */
    fun open(block: EditableBlock) {
        context = context.copy(storyId = block.id)
        selection.opened(block.uid)
    }

    /**
     * The message asking the editor to do [action] to the selected block, or `null` when its menu does
     * not offer that action.
     */
    fun action(action: BlockAction): BlockActionMessage? {
        val selected = selectedBlock.value ?: return null
        if (action !in selected.menu?.actions.orEmpty()) return null
        return blockAction(action, selected.menu?.target ?: return null)
    }

    /**
     * The message opening [breadcrumb]'s form, selecting it as it is sent — or `null` when no story is
     * known for it to name.
     */
    fun open(breadcrumb: BlockBreadcrumb, resolveRelations: String, fallbackLanguage: String?): EditAction? {
        val storyId = context.storyId ?: return null
        val path = selectedBlock.value?.menu?.breadcrumbs.orEmpty()
        val ancestors = path.takeWhile { it.uid != breadcrumb.uid }.map { it.uid }
        selection.opened(breadcrumb.uid, ancestors)
        return edit(EditableBlock(name = breadcrumb.component, uid = breadcrumb.uid, id = storyId), resolveRelations, fallbackLanguage)
    }

    fun dismissHover() = hints.update { it.copy(hovered = null) }

    /** The edit [content], the first the editor sent, makes: in the selected block, or else the content itself. */
    private fun firstEdit(content: JsonElement): BlockEdit? {
        val selected = selectedBlock.value
        if (selected != null) return BlockEdit(selected.uid, selected.ancestors, compared = false)
        val root = ((content as? JsonObject)?.get("_uid") as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() } ?: return null
        return BlockEdit(root, compared = false)
    }

    /**
     * Takes in the story the editor sent with an `input`, finding where the author changed it.
     *
     * That converts the whole content on every keystroke, as decoding it for the preview does too. It
     * is compared as converted, rather than through the JavaScript values, because the same comparison
     * is then plain, testable code.
     *
     * The first story the editor sends, or one with another id, has nothing to be compared with. Its
     * edit is put in the selected block, as the author can only type in a form they have opened, or in
     * the story's content when none is selected. Where in rich text it is, is left to the document.
     */
    private fun edited(story: EditedStory) {
        val id = story.id?.toLong()?.toString()
        id?.let { context = context.copy(storyId = it) }
        val previous = content?.takeIf { (storyId, _) -> id != null && storyId == id }?.second
        val next = story.content.toJsonElement(resolveLevel = 1)
        content = id to next
        val edit = when (previous) {
            // Nothing to place it in. The last story's edit is cleared, so that nothing follows it here.
            null -> firstEdit(next) ?: return hints.update { it.copy(edited = null) }
            else -> editBetween(previous, next) ?: return
        }
        hints.update { it.copy(edited = edit.copy(serial = ++edits)) }
    }
}

/**
 * The message asking the editor to do [action] to the block [blok] names: the last of the breadcrumbs
 * the editor sent with it, sent back as it came.
 */
internal fun blockAction(action: BlockAction, blok: JsonObject): BlockActionMessage = unsafeJso {
    this.action = action.message
    this.blok = parseJson(blok.toString())
}

internal external interface BlockActionMessage : JsAny {
    var action: String
    var blok: JsAny
}

/** The `input` message, read for the story it carries. */
private external interface StoryEvent : JsAny {
    val story: EditedStory?
}

private external interface EditedStory : JsAny {
    val id: Double?
    val content: JsAny?
}

private fun parseJson(text: String): JsAny = js("JSON.parse(text)")
