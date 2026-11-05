@file:OptIn(InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.runtime.withFrameNanos
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
import kotlinx.coroutines.flow.collectLatest
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
    val followed: FollowedLists = FollowedLists()

    /**
     * The [serial][BlockEdit.serial] of the last edit a block was brought into view for. Each edit is
     * shown once, so that the author can scroll away from the block they are typing in.
     */
    var shownEdit: Int = 0

    /** The edit each lazy list last followed, and where, for the same reason as [followed]. */
    val followedEdits: FollowedLists = FollowedLists()

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

/**
 * A request a lazy list followed, to the item at [index] in the whole list: a [SelectedBlock.reveal], or
 * a [BlockEdit.serial].
 */
internal data class Followed(val reveal: Int, val index: Int)

/**
 * What the most recent [capacity] lazy lists followed, the least recently followed forgotten first.
 *
 * Bounded because the editor outlives the screens it previews, and every list the app navigates
 * through would otherwise stay here. Not dropped when a list's items leave composition, as they do
 * whenever it scrolls past them, because that would forget the guard that lets the author scroll away
 * from a selection. A list forgotten while still on screen follows its current request once more.
 */
internal class FollowedLists(private val capacity: Int = 16) {
    private val lists = LinkedHashMap<LazyListState, Followed>()

    operator fun get(state: LazyListState): Followed? = lists[state]

    operator fun set(state: LazyListState, followed: Followed) {
        lists.remove(state)
        lists[state] = followed
        if (lists.size > capacity) lists.remove(lists.keys.first())
    }

    val size: Int get() = lists.size
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
    // Read through state rather than keyed on, so that every edit's new instance of the block neither
    // restarts the tap detector, losing a press it spans, nor compares the whole block to the last one.
    val current by rememberUpdatedState(block)
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
    LaunchedEffect(editor, block.uid, requester) {
        snapshotFlow { editor.selected.value?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { selection ->
                placed.coordinates?.let { editor.selectedBounds.value = block.uid to it.boundsInWindow() }
                // Content that arrives with the request recomposes where blocks are, which cancels this
                // effect if its place now shows another block, so that the block is brought into view
                // where it lands rather than where it was. Two frames, because a frame resumes its waiters
                // before it recomposes: one would act first.
                repeat(2) { withFrameNanos {} }
                if (editor.shown == selection.reveal) return@collect
                editor.shown = selection.reveal
                requester.bringIntoView()
            }
    }

    // The editor describes the block it selects through the form, and the one a tap or a breadcrumb
    // asks it to open. A block it selects by adding, moving or duplicating it is asked for, as the
    // package asks for it, so that its menu comes back. Keyed on the uid, as the tap detector is, so that
    // an edit's new instance of the block does not restart it.
    LaunchedEffect(editor, block.uid) {
        snapshotFlow { editor.selected.value?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { selection ->
                if (selection.menu != null) {
                    if (editor.awaitingMenu == block.uid) editor.awaitingMenu = null
                } else if (editor.awaitingMenu != block.uid) {
                    editor.awaitingMenu = block.uid
                    editor.bridge.openBlock(current)
                }
            }
    }

    LaunchedEffect(editor, requester, block.uid) {
        snapshotFlow { block.uid.isNotEmpty() && editor.hints.value.highlightedFocus == block.uid }
            .filter { it }
            // Launched apart, so that a scroll the author interrupts cancels only itself, not this collector.
            .collect { launch { requester.bringIntoView() } }
    }

    // The block the author changes, followed as they type. Started as the block enters composition,
    // too, so that one the edit adds, or a lazy list scrolls to for it, is brought into view as well.
    LaunchedEffect(editor, requester, block.uid) {
        snapshotFlow { editor.hints.value.edited?.takeIf { block.uid.isNotEmpty() && it.uid == block.uid } }
            .filterNotNull()
            .collect { edit ->
                // After the content that comes with the edit has been laid out, as for a selection.
                repeat(2) { withFrameNanos {} }
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
        .pointerInput(block.uid, editor) { openOnTap({ current }, editor) }
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
private suspend fun PointerInputScope.openOnTap(block: () -> Component, editor: VisualEditor) = awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
    val pressed = block()
    editor.pressed = pressed

    var moved = false
    val up: PointerInputChange
    while (true) {
        val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
        // A drag scrolls in the Main pass, after this one, so it is told apart by distance rather than by
        // consumption — the release's included, since a pointer can move only on that last event.
        if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
        if (!change.pressed) {
            up = change
            break
        }
    }
    if (moved || editor.pressed !== pressed) return@awaitEachGesture

    // The block as it is now, which an edit during the press may have replaced.
    val tapped = block()
    val before = editor.bridge.selectedBlock.value
    val wasSelected = before?.uid == tapped.uid
    // Only a block without a menu waits for one. A block that has one keeps its selection, and the
    // editor's answer repeats the same menu, so nothing would ever stop it waiting: a later move would
    // then be taken for the menu still on its way, and the block not asked for it again.
    if (!wasSelected || before.menu == null) editor.awaitingMenu = tapped.uid
    editor.bridge.openBlock(tapped)
    // Held back only if this tap is what selected the block. One the editor did not select must not
    // swallow every tap it is given.
    val selection = editor.bridge.selectedBlock.value
    if (wasSelected || selection?.uid != tapped.uid) return@awaitEachGesture
    editor.shown = selection.reveal
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

    // The blocks are read as well as the selection, because the two arrive separately: a block can be
    // selected before the content holding it does, and a moved block before the content that moves it.
    // And which items are on screen, so that a request none of these could act on yet is acted on as
    // soon as one of them is: an item composed ahead of the scroll is not on screen until it arrives.
    LaunchedEffect(editor, state, item.uid) {
        snapshotFlow {
            editor.selected.value?.let { selected ->
                Triple(selected, current.map(Component::uid), state.layoutInfo.visibleItemsInfo.map { it.index to it.key })
            }
        }
            .filterNotNull()
            .collectLatest {
                // What changed is read again a frame later rather than taken from the snapshot, which can
                // see the new blocks with the layout from before they were measured: a block inserted
                // above the item acting would put every index one out. A frame resumes its waiters before
                // it recomposes, so the blocks and the layout read then are from the same frame.
                withFrameNanos {}
                follow(editor, item, current, state)
            }
    }

    // The author's edits, followed as selections are: each once, read with the blocks and what is on
    // screen, as an edit too can arrive before the content holding the block it changes.
    LaunchedEffect(editor, state, item.uid) {
        snapshotFlow {
            editor.hints.value.edited?.let { edit ->
                Triple(edit, current.map(Component::uid), state.layoutInfo.visibleItemsInfo.map { it.index to it.key })
            }
        }
            .filterNotNull()
            .collectLatest {
                // A frame later, for the same reason as a selection.
                withFrameNanos {}
                followEdit(editor, item, current, state)
            }
    }
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
                if (editor.followedEdits[state]?.reveal == serial) return@collect
                editor.followedEdits[state] = Followed(serial, index)
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

private fun follow(editor: VisualEditor, item: Component, blocks: List<Component>, state: LazyListState) {
    val selected = editor.selected.value ?: return
    follow(editor, item, blocks, state, selected.uid, selected.ancestors, selected.reveal, editor.followed) {
        editor.shown = selected.reveal
    }
}

private fun followEdit(editor: VisualEditor, item: Component, blocks: List<Component>, state: LazyListState) {
    val edit = editor.hints.value.edited ?: return
    follow(editor, item, blocks, state, edit.uid, edit.ancestors, edit.serial, editor.followedEdits) {
        editor.shownEdit = edit.serial
    }
}

/**
 * Scrolls [state] to the item of [blocks] holding the block [uid], or the one of its [ancestors] that
 * [blocks] has, once for each [request], if [item] is the first of [blocks] on screen and that item is
 * not. [shown] is told when the scroll shows the block itself, which then need not bring itself into view.
 */
private fun follow(
    editor: VisualEditor,
    item: Component,
    blocks: List<Component>,
    state: LazyListState,
    uid: String,
    ancestors: List<String>,
    request: Int,
    followedLists: FollowedLists,
    shown: () -> Unit,
) {
    val keys = blocks.map { blockKey(it.uid) }
    val visible = state.layoutInfo.visibleItemsInfo.map { it.index to it.key }
    val actor = visible.firstOrNull { (_, key) -> key in keys } ?: return
    if (actor.second != blockKey(item.uid)) return

    // Not found is not followed: the block may yet arrive, or be another blocks()'s in this list.
    val target = blocks.indexOfFirst { it.uid == uid || it.uid in ancestors }
    if (target < 0) return
    // Where it is in the whole list, so that a block moved to another blocks() in it is followed even
    // when it keeps its place within its group.
    val index = actor.first - keys.indexOf(actor.second) + target
    if (index < 0) return
    val followed = Followed(request, index)
    if (followedLists[state] == followed) return
    followedLists[state] = followed

    if (visible.any { (visibleIndex, _) -> visibleIndex == index }) return
    // The block's own item is shown by this scroll, which its block bringing itself into view as it
    // arrives would only cut short. A block nested in the item still does, to find its place in it.
    if (blocks[target].uid == uid) shown()
    editor.scope.launch { state.animateScrollToItem(index) }
}

/**
 * The lazy list key of the block with [uid], set apart from the keys of the list's other items.
 *
 * A string, as a key has to be for Android to save the item's state.
 */
internal fun blockKey(uid: String): String = "storyblok:block:$uid"
