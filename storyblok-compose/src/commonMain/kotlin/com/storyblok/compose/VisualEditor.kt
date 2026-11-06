@file:OptIn(InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.storyblok.InternalAPI
import com.storyblok.cdn.SelectedBlock
import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.schema.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/** The colour of the outline the preview bridge package draws around the selected block. */
private val OUTLINE = Color(0xFF05807F)

/**
 * Twice the package's 1px. That was drawn for light web pages, and against a dark Compose theme a line
 * of that teal so thin is barely visible.
 */
private val OUTLINE_WIDTH = 2.dp

/**
 * The Visual Editor a [Storyblok] tree is previewed in.
 *
 * Every block reaches it through [LocalVisualEditor]. Nested blocks are drawn through the scope their
 * [BlockProvider][com.storyblok.compose.provider.BlockProvider] was built with, before any client
 * existed, so there is no other way to hand it to them.
 */
internal class VisualEditor(
    val bridge: StoryblokBridge,
    /** [bridge]'s selected block, collected once for every block in the tree. */
    val selected: State<SelectedBlock?>,
    /**
     * Where lazy lists scroll to a selected block from. It is the [Storyblok] call's, because the item
     * that starts a scroll is soon scrolled out of composition, and would cancel the scroll with it.
     */
    val scope: CoroutineScope,
) {
    /** The innermost block under the pointer that last went down: the one a tap there opens. */
    var pressed: Component? = null

    /**
     * The [SelectedBlock.reveal] last brought into view, or answered by a tap, whose block is on screen
     * already. Each is scrolled to once, so that a lazy list scrolling a selected block out and back in
     * does not then yank it back into view, while a new request for the same block still scrolls.
     */
    var shown: Int? = null

    /**
     * The request each lazy list of [blocks][BlockScope.blocks] last followed, and where it found the
     * selected block's item. A list follows each request once, for the same reason a block is only
     * [shown] once: so that the user can scroll away. It follows it again only if that item moves.
     */
    val followed: MutableMap<LazyListState, Followed> = mutableMapOf()
}

/** A [SelectedBlock.reveal] a lazy list followed, to the item at [index] in its blocks. */
internal data class Followed(val reveal: Int, val index: Int)

/** The Visual Editor the blocks below are previewed in, or `null` outside one. */
internal val LocalVisualEditor = staticCompositionLocalOf<VisualEditor?> { null }

/**
 * [block] as the editor previews it: a tap opens its form, and while it is selected it is outlined and
 * brought into view.
 *
 * The first tap on a block only selects it, as on a web page in the editor, so that tapping a block
 * that is itself clickable — a card that navigates, say — opens it instead of following it. A second
 * tap does what the block does.
 */
@Composable
internal fun Modifier.editable(block: Component, editor: VisualEditor): Modifier {
    val selected = remember(editor, block.uid) {
        derivedStateOf { block.uid.isNotEmpty() && editor.selected.value?.uid == block.uid }
    }
    val requester = remember { BringIntoViewRequester() }

    // Read from snapshots rather than composition, so that a change of selection recomposes nothing:
    // only the block that gains it and the one that loses it redraw.
    LaunchedEffect(editor, block.uid, requester) {
        snapshotFlow { editor.selected.value?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { selection ->
                if (editor.shown == selection.reveal) return@collect
                editor.shown = selection.reveal
                requester.bringIntoView()
            }
    }

    return this
        .pointerInput(block, editor) { openOnTap(block, editor) }
        .bringIntoViewRequester(requester)
        .drawWithContent {
            drawContent()
            if (!selected.value) return@drawWithContent
            // 1dp in from the edge rather than on it. A block as wide as the preview has its sides under
            // the editor's frame, which hides a line drawn on the outermost pixels. The stroke is centred
            // on its path, hence the half.
            val width = OUTLINE_WIDTH.toPx()
            val inset = 1.dp.toPx() + width / 2
            drawRect(
                OUTLINE,
                topLeft = Offset(inset, inset),
                size = Size(size.width - 2 * inset, size.height - 2 * inset),
                style = Stroke(width),
            )
        }
}

/**
 * Opens [block] in the editor when it is tapped, holding the tap back from whatever is inside it if
 * this selects it.
 *
 * Everything happens on the [Initial][PointerEventPass.Initial] pass, which runs from the outermost
 * block inwards before anything inside handles the event in the Main pass. So the innermost block
 * under the pointer is the last to record itself as [pressed][VisualEditor.pressed], and the only one
 * that acts — and a tap it holds back is consumed before a clickable inside can see it.
 */
private suspend fun PointerInputScope.openOnTap(block: Component, editor: VisualEditor) = awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
    editor.pressed = block

    var moved = false
    val up: PointerInputChange
    while (true) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
        if (!change.pressed) {
            up = change
            break
        }
        // A drag scrolls in the Main pass, after this one, so it is told apart by distance rather than by consumption.
        if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
    }
    if (moved || editor.pressed !== block) return@awaitEachGesture

    val wasSelected = editor.bridge.selectedBlock.value?.uid == block.uid
    editor.bridge.openBlock(block)
    // Held back only if this tap is what selected the block. One the editor cannot open — it has no
    // `_editable` — is never selected, and must not swallow every tap it is given.
    val selection = editor.bridge.selectedBlock.value
    if (wasSelected || selection?.uid != block.uid) return@awaitEachGesture
    editor.shown = selection.reveal
    up.consume()
}

/**
 * Scrolls [state]'s list to the item of [blocks] holding the block the editor selects, when that item
 * is off screen and so has no block to bring itself into view.
 *
 * Every item of [blocks] that is composed runs this, and the first of them on screen acts. None of
 * them may be the list's first item, and the list never says which index a key is at unless that
 * item is on screen, so the index of [blocks]' first item is worked out from the one that acts.
 */
@Composable
internal fun FollowSelection(item: Component, blocks: List<Component>, state: LazyListState) {
    val editor = LocalVisualEditor.current ?: return
    val current by rememberUpdatedState(blocks)

    // The blocks are read as well as the selection, because the two arrive separately: a block can be
    // selected before the content holding it does, and a moved block before the content that moves it.
    LaunchedEffect(editor, state, item.uid) {
        snapshotFlow { editor.selected.value?.let { it to current.map(Component::uid) } }
            .filterNotNull()
            .collect { (selected, uids) ->
                val visible = state.layoutInfo.visibleItemsInfo
                val actor = visible.firstOrNull { it.key in uids } ?: return@collect
                if (actor.key != item.uid) return@collect

                // Not found is not followed: the block may yet arrive, or be another blocks()'s in this list.
                val target = uids.indexOfFirst { it == selected.uid || it in selected.ancestors }
                if (target < 0) return@collect
                val followed = Followed(selected.reveal, target)
                if (editor.followed[state] == followed) return@collect
                editor.followed[state] = followed

                val index = actor.index - uids.indexOf(actor.key) + target
                if (visible.any { it.index == index }) return@collect
                editor.scope.launch { state.animateScrollToItem(index) }
            }
    }
}
