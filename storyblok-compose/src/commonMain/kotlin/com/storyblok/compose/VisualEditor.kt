@file:OptIn(InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.storyblok.InternalAPI
import com.storyblok.cdn.BlockEdit
import com.storyblok.cdn.BlockHints
import com.storyblok.cdn.SelectedBlock
import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/** The colour of the outline the preview bridge package draws around the selected block. */
private val OUTLINE = Color(0xFF05807F)

/** The colour of the line the package outlines every block with while the pointer moves over the page. */
private val BLOCK_OUTLINE = Color(182, 186, 191, 128)

/** The colour the package fills a highlighted block with. */
private val HIGHLIGHT = Color(89, 197, 198, 51)

/** How long the pointer has to rest before the outlines around every block go, as in the package. */
private const val OUTLINE_LINGER_MILLIS = 800L

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
    /** [bridge]'s hints, collected once for every block in the tree. */
    val hints: State<BlockHints> = mutableStateOf(BlockHints()),
) {
    /** The innermost block under the pointer that last went down: the one a tap there opens. */
    var pressed: Component? = null

    /**
     * The block last brought into view, or opened by a tap and so on screen already. A selection
     * scrolls to its block once, so that a lazy list scrolling a selected block out and back in does
     * not then yank it back into view.
     */
    var shown: String? = null

    /**
     * The selection each lazy list of [blocks][BlockScope.blocks] last followed. A list follows each
     * selection once, for the same reason a block is only [shown] once: so that the user can scroll
     * away from it.
     */
    val followed: MutableMap<LazyListState, SelectedBlock> = mutableMapOf()

    /**
     * The [serial][BlockEdit.serial] of the last edit a block was brought into view for. Each edit is
     * shown once, so that the author can scroll away from the block they are typing in.
     */
    var shownEdit: Int = 0

    /** The [serial][BlockEdit.serial] of the edit each lazy list last followed, for the same reason. */
    val followedEdits: MutableMap<LazyListState, Int> = mutableMapOf()

    /**
     * Where the selected block is in the window, clipped to what of it is on screen, for its
     * [menu][EditorMenu] to be drawn against. Paired with the block's `_uid`, as it lingers after the
     * selection moves on until the next selected block reports in.
     */
    val selectedBounds: MutableState<Pair<String, Rect>?> = mutableStateOf(null)

    /**
     * The block asked to open its form and not yet described by the editor in answer. A selection the
     * editor gives no menu for is opened, so that the editor describes it, unless it is this one.
     */
    var awaitingMenu: String? = null

    /** Whether every block is outlined, as they are while the pointer moves over the page. */
    val outlined: MutableState<Boolean> = mutableStateOf(false)

    /** When the pointer last moved, as its events time it, which every block under it hears of. */
    private var lastMove = -1L

    private var outlineTimer: Job? = null

    /**
     * Outlines every block until the pointer has rested for a while, then hides them and the hovered
     * block's hint, as the package does on a web page.
     */
    fun pointerMoved(uptimeMillis: Long) {
        if (uptimeMillis == lastMove) return
        lastMove = uptimeMillis
        outlined.value = true
        outlineTimer?.cancel()
        outlineTimer = scope.launch {
            delay(OUTLINE_LINGER_MILLIS)
            outlined.value = false
            bridge.dismissHover()
        }
    }
}

/** The Visual Editor the blocks below are previewed in, or `null` outside one. */
internal val LocalVisualEditor = staticCompositionLocalOf<VisualEditor?> { null }

/**
 * [block] as the editor previews it: a tap opens its form, and while it is selected it is outlined and
 * brought into view, with its [menu][EditorMenu] above it.
 *
 * It is also outlined, more faintly, while the pointer moves over the page; outlined while the author
 * hovers over it in the editor's form; and highlighted, and brought into view, when the editor
 * highlights it.
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
    val hovered = remember(editor, block.uid) {
        derivedStateOf { block.uid.isNotEmpty() && editor.hints.value.hovered == block.uid }
    }
    val highlighted = remember(editor, block.uid) {
        derivedStateOf { block.uid.isNotEmpty() && block.uid in editor.hints.value.highlighted }
    }
    // Where the block was last placed, for its menu to be put against the moment it is selected.
    val placed = remember { Placement() }

    // Read from snapshots rather than composition, so that a change of selection recomposes nothing:
    // only the block that gains it and the one that loses it redraw.
    LaunchedEffect(selected, requester) {
        snapshotFlow { selected.value }
            .filter { it }
            .collect {
                placed.coordinates?.let { editor.selectedBounds.value = block.uid to it.boundsInWindow() }
                if (editor.shown == block.uid) return@collect
                editor.shown = block.uid
                requester.bringIntoView()
            }
    }

    // The editor describes the block it selects through the form, and the one a tap or a breadcrumb
    // asks it to open. A block it selects by adding, moving or duplicating it is asked for, as the
    // package asks for it, so that its menu comes back.
    LaunchedEffect(editor, block) {
        snapshotFlow { editor.selected.value?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { selection ->
                if (selection.menu != null) {
                    if (editor.awaitingMenu == block.uid) editor.awaitingMenu = null
                } else if (editor.awaitingMenu != block.uid) {
                    editor.awaitingMenu = block.uid
                    editor.bridge.openBlock(block)
                }
            }
    }

    LaunchedEffect(editor, requester, block.uid) {
        snapshotFlow { block.uid.isNotEmpty() && editor.hints.value.highlightedFocus == block.uid }
            .filter { it }
            .collect { requester.bringIntoView() }
    }

    // The block the author changes, followed as they type. Started as the block enters composition,
    // too, so that one the edit adds, or a lazy list scrolls to for it, is brought into view as well.
    LaunchedEffect(editor, requester, block.uid) {
        snapshotFlow { editor.hints.value.edited?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { edit ->
                if (editor.shownEdit == edit.serial) return@collect
                editor.shownEdit = edit.serial
                requester.bringIntoView()
            }
    }

    DisposableEffect(editor, block.uid) {
        onDispose {
            if (editor.selectedBounds.value?.first == block.uid) editor.selectedBounds.value = null
        }
    }

    return this
        .pointerInput(block, editor) { openOnTap(block, editor) }
        .pointerInput(editor) { outlineOnMove(editor) }
        .bringIntoViewRequester(requester)
        .onGloballyPositioned {
            placed.coordinates = it
            if (selected.value) editor.selectedBounds.value = block.uid to it.boundsInWindow()
        }
        .drawWithContent {
            drawContent()
            if (highlighted.value) {
                drawRect(HIGHLIGHT)
                drawOutline(OUTLINE, 1.dp.toPx())
            }
            when {
                selected.value -> drawOutline(OUTLINE, OUTLINE_WIDTH.toPx())
                hovered.value -> drawOutline(OUTLINE, 1.dp.toPx())
                editor.outlined.value -> drawOutline(BLOCK_OUTLINE, 1.dp.toPx(), dashed = true)
            }
        }
}

private class Placement {
    var coordinates: LayoutCoordinates? = null
}

/**
 * A line around the block, 1dp in from its edges rather than on them. A block as wide as the preview
 * has its sides under the editor's frame, which hides a line drawn on the outermost pixels.
 */
private fun DrawScope.drawOutline(color: Color, width: Float, dashed: Boolean = false) {
    // The stroke is centred on its path, hence the half.
    val inset = 1.dp.toPx() + width / 2
    drawRect(
        color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - 2 * inset, size.height - 2 * inset),
        style = Stroke(width, pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())) else null),
    )
}

/**
 * Tells [editor] whenever a mouse moves over the block. Hover events, which only a mouse makes, reach
 * every block under the pointer rather than the page as a whole, so this costs nothing while the
 * pointer is still or outside the preview.
 */
private suspend fun PointerInputScope.outlineOnMove(editor: VisualEditor) = awaitPointerEventScope {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        if (event.type != PointerEventType.Move && event.type != PointerEventType.Enter) continue
        val change = event.changes.firstOrNull { it.type == PointerType.Mouse } ?: continue
        editor.pointerMoved(change.uptimeMillis)
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
    editor.awaitingMenu = block.uid
    editor.bridge.openBlock(block)
    // Held back only if this tap is what selected the block. One the editor cannot open — it has no
    // `_editable` — is never selected, and must not swallow every tap it is given.
    if (wasSelected || editor.bridge.selectedBlock.value?.uid != block.uid) return@awaitEachGesture
    editor.shown = block.uid
    up.consume()
}

/**
 * Scrolls [state]'s list to the item of [blocks] holding the block the editor selects, or the one the
 * author changes, when that item is off screen and so has no block to bring itself into view.
 *
 * Every item of [blocks] that is composed runs this, and the first of them on screen acts. None of
 * them may be the list's first item, and the list never says which index a key is at unless that
 * item is on screen, so the index of [blocks]' first item is worked out from the one that acts.
 */
@Composable
internal fun FollowSelection(item: Component, blocks: List<Component>, state: LazyListState) {
    val editor = LocalVisualEditor.current ?: return
    val current by rememberUpdatedState(blocks)

    LaunchedEffect(editor, state, item.uid) {
        snapshotFlow { editor.selected.value }
            .filterNotNull()
            .collect { selected ->
                val actor = state.actor(item, current) ?: return@collect
                if (editor.followed[state] == selected) return@collect
                editor.followed[state] = selected
                val index = state.offScreenIndex(actor, current, selected.uid, selected.ancestors) ?: return@collect
                editor.scope.launch { state.animateScrollToItem(index) }
            }
    }

    // The blocks are read here as well as the edit, as the edit can arrive before the blocks it adds.
    LaunchedEffect(editor, state, item.uid) {
        snapshotFlow { editor.hints.value.edited?.takeIf { edit -> current.any { it.uid == edit.uid || it.uid in edit.ancestors } } }
            .filterNotNull()
            .collect { edit ->
                val actor = state.actor(item, current) ?: return@collect
                if (editor.followedEdits[state] == edit.serial) return@collect
                editor.followedEdits[state] = edit.serial
                val index = state.offScreenIndex(actor, current, edit.uid, edit.ancestors) ?: return@collect
                editor.scope.launch { state.animateScrollToItem(index) }
            }
    }
}

/** The first item of [blocks] on screen, if it is [item]'s: the one item that acts for them all. */
private fun LazyListState.actor(item: Component, blocks: List<Component>): LazyListItemInfo? = layoutInfo.visibleItemsInfo
    .firstOrNull { info -> blocks.any { it.uid == info.key } }
    ?.takeIf { it.key == item.uid }

/**
 * The index in the list of the item of [blocks] holding block [uid], nested in [ancestors], worked out
 * from [actor]'s — or `null` when no item holds it, or that item is on screen.
 */
private fun LazyListState.offScreenIndex(actor: LazyListItemInfo, blocks: List<Component>, uid: String, ancestors: List<String>): Int? {
    val target = blocks.indexOfFirst { it.uid == uid || it.uid in ancestors }
    if (target < 0) return null
    val index = actor.index - blocks.indexOfFirst { it.uid == actor.key } + target
    return index.takeUnless { layoutInfo.visibleItemsInfo.any { it.index == index } }
}

/**
 * Scrolls [state], a list of [document]'s nodes one item each, to the node the author changes in the
 * Visual Editor, when it is not wholly on screen. Outside the editor it does nothing.
 *
 * A lazy list needs this to follow an edit, since a node off screen is not composed and so cannot
 * bring itself into view.
 *
 * @suppress
 */
@InternalAPI
@Composable
public fun FollowEdits(document: RichText.Document, state: LazyListState) {
    val editor = LocalVisualEditor.current ?: return
    val current by rememberUpdatedState(document)

    // The document is read here as well as the edit, as the edit arrives before the document it edits.
    LaunchedEffect(editor, state) {
        snapshotFlow { editor.hints.value.edited?.let { edit -> edit.nodeIn(current)?.let { edit.serial to it } } }
            .filterNotNull()
            .collect { (serial, index) ->
                if (editor.followedEdits[state] == serial) return@collect
                editor.followedEdits[state] = serial
                state.reveal(index)
            }
    }
}

/**
 * Scrolls the item at [index] into view, as little as it takes: all of it if it fits, its start if it
 * does not, and nothing if it fills the list already.
 */
private suspend fun LazyListState.reveal(index: Int) {
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return animateScrollToItem(index)
    val hiddenBefore = layoutInfo.viewportStartOffset - item.offset
    val hiddenAfter = item.offset + item.size - layoutInfo.viewportEndOffset
    when {
        hiddenBefore > 0 && hiddenAfter > 0 -> Unit
        hiddenBefore > 0 -> animateScrollBy(-hiddenBefore.toFloat())
        hiddenAfter > 0 -> animateScrollBy(minOf(hiddenAfter, -hiddenBefore).toFloat())
    }
}
