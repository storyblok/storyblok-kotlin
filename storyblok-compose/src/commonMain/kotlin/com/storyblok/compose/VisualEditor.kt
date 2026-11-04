@file:OptIn(InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
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
import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.schema.Component
import kotlinx.coroutines.flow.filter

/** The outline the preview bridge package draws around the selected block. */
private val OUTLINE = Color(0xFF05807F)

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
    val selected: State<String?>,
) {
    /** The innermost block under the pointer that last went down: the one a tap there opens. */
    var pressed: Component? = null

    /**
     * The block last brought into view, or opened by a tap and so on screen already. A selection
     * scrolls to its block once, so that a lazy list scrolling a selected block out and back in does
     * not then yank it back into view.
     */
    var shown: String? = null
}

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
        derivedStateOf { block.uid.isNotEmpty() && editor.selected.value == block.uid }
    }
    val requester = remember { BringIntoViewRequester() }

    // Read from snapshots rather than composition, so that a change of selection recomposes nothing:
    // only the block that gains it and the one that loses it redraw.
    LaunchedEffect(selected, requester) {
        snapshotFlow { selected.value }
            .filter { it }
            .collect {
                if (editor.shown == block.uid) return@collect
                editor.shown = block.uid
                requester.bringIntoView()
            }
    }

    return this
        .pointerInput(block, editor) { openOnTap(block, editor) }
        .bringIntoViewRequester(requester)
        .drawWithContent {
            drawContent()
            if (!selected.value) return@drawWithContent
            // Inset by half the stroke, which is drawn centred on the edge, so none of it falls outside the block.
            val width = 1.dp.toPx()
            drawRect(
                OUTLINE,
                topLeft = Offset(width / 2, width / 2),
                size = Size(size.width - width, size.height - width),
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

    val wasSelected = editor.bridge.selectedBlock.value == block.uid
    editor.bridge.openBlock(block)
    // Held back only if this tap is what selected the block. One the editor cannot open — it has no
    // `_editable` — is never selected, and must not swallow every tap it is given.
    if (wasSelected || editor.bridge.selectedBlock.value != block.uid) return@awaitEachGesture
    editor.shown = block.uid
    up.consume()
}
