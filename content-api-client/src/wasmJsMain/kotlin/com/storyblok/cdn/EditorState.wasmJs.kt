@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import js.objects.unsafeJso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsString
import kotlin.js.js

/**
 * Everything the Visual Editor's messages say other than what the story holds: which block is
 * selected, the menu the preview draws on it, and the blocks the editor points the author at.
 *
 * Apart from the window it is posted to, so that what each message does can be tested without one.
 */
internal class EditorState {

    val context = EditorContext()

    val selectedBlock = MutableStateFlow<SelectedBlock?>(null)

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
            edited((event as StoryEvent).story ?: return)
            return
        }
        val message = data.toJsonElement(resolveLevel = 1) as? JsonObject ?: return
        context.update(message)
        selectedBlock.value = selectionAfter(event, selectedBlock.value)?.let { selected ->
            context.menuOf(message, selected.uid)?.let { selected.copy(menu = it) } ?: selected
        }
        hints.value = hintsAfter(message, hints.value)
    }

    /** Selects [block], which a tap has just asked the editor to open, keeping its menu if it is selected already. */
    fun open(block: EditableBlock) {
        context.storyId = block.id
        if (selectedBlock.value?.uid != block.uid) selectedBlock.value = SelectedBlock(block.uid)
    }

    /**
     * The message asking the editor to do [action] to the selected block, or `null` when its menu does
     * not offer that action.
     */
    fun action(action: BlockAction): BlockActionMessage? {
        val selected = selectedBlock.value ?: return null
        if (action !in selected.menu?.actions.orEmpty()) return null
        return blockAction(action, context.actionTarget(selected.uid) ?: return null)
    }

    /**
     * The message opening [breadcrumb]'s form, selecting it as it is sent — or `null` when no story is
     * known for it to name.
     */
    fun open(breadcrumb: BlockBreadcrumb, resolveRelations: String): EditAction? {
        val storyId = context.storyId ?: return null
        val path = selectedBlock.value?.menu?.breadcrumbs.orEmpty()
        val ancestors = path.takeWhile { it.uid != breadcrumb.uid }.map { it.uid }
        selectedBlock.value = SelectedBlock(breadcrumb.uid, ancestors)
        return edit(EditableBlock(name = breadcrumb.component, uid = breadcrumb.uid, id = storyId), resolveRelations)
    }

    fun dismissHover() = hints.update { it.copy(hovered = null) }

    /**
     * Takes in the story the editor sent with an `input`, finding where the author changed it.
     *
     * That converts the whole content on every keystroke, as decoding it for the preview does too. It
     * is compared as converted, rather than through the JavaScript values, because the same comparison
     * is then plain, testable code. The first story the editor sends, or one with another id, is only
     * kept to compare the next with: there is nothing yet to compare it with.
     */
    private fun edited(story: EditedStory) {
        val id = story.id?.toLong()?.toString()
        id?.let { context.storyId = it }
        val previous = content?.takeIf { (storyId, _) -> id != null && storyId == id }?.second
        val next = story.content.toJsonElement(resolveLevel = 1)
        content = id to next
        val edit = editBetween(previous ?: return, next) ?: return
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
