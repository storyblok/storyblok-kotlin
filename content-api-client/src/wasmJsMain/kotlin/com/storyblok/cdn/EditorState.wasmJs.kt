@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import js.objects.unsafeJso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
 * Which block is selected, and when it is to be brought into view, is [selection]'s to say; this adds
 * its menu, which leaves its [reveal][SelectedBlock.reveal] as it was.
 */
internal class EditorState(private val selection: EditorSelection = EditorSelection()) {

    val context = EditorContext()

    val selectedBlock: MutableStateFlow<SelectedBlock?> get() = selection.selected

    val hints = MutableStateFlow(BlockHints())

    /** Takes in a message from the editor, of any action. */
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    fun receive(data: JsAny?) {
        val event = data as? SelectionEvent ?: return
        val action = (event.action as? JsString)?.toString() ?: return
        if (action == "input") {
            // Read for the story's id alone. Converting the story here as well would double the work
            // every keystroke costs, for content that is converted where it is decoded.
            (event as StoryIdEvent).story?.id?.let { context.storyId = it.toLong().toString() }
            selection.handle(event)
            return
        }
        val message = data.toJsonElement(resolveLevel = 1) as? JsonObject ?: return
        context.update(message)
        selection.handle(event)
        selectedBlock.value = selectedBlock.value?.let { selected ->
            context.menuOf(message, selected.uid)?.let { selected.copy(menu = it) } ?: selected
        }
        hints.value = hintsAfter(message, hints.value)
    }

    /** Selects [block], which a tap has just asked the editor to open, keeping its menu if it is selected already. */
    fun open(block: EditableBlock) {
        context.storyId = block.id
        selection.opened(block.uid)
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
    fun open(breadcrumb: BlockBreadcrumb, resolveRelations: String, fallbackLanguage: String?): EditAction? {
        val storyId = context.storyId ?: return null
        val path = selectedBlock.value?.menu?.breadcrumbs.orEmpty()
        val ancestors = path.takeWhile { it.uid != breadcrumb.uid }.map { it.uid }
        selection.opened(breadcrumb.uid, ancestors)
        return edit(EditableBlock(name = breadcrumb.component, uid = breadcrumb.uid, id = storyId), resolveRelations, fallbackLanguage)
    }

    fun dismissHover() = hints.update { it.copy(hovered = null) }
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

/** The `input` message, read only for the id of the story it carries. */
private external interface StoryIdEvent : JsAny {
    val story: StoryId?
}

private external interface StoryId : JsAny {
    val id: Double?
}

private fun parseJson(text: String): JsAny = js("JSON.parse(text)")
