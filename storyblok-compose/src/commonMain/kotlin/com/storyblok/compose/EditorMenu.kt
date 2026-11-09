@file:OptIn(InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.storyblok.InternalAPI
import com.storyblok.cdn.BlockAction
import com.storyblok.cdn.BlockBreadcrumb
import com.storyblok.cdn.BlockMenu
import com.storyblok.cdn.SelectedBlock

/*
 * The menu `@storyblok/preview-bridge` draws on the selected block of a web page, drawn here with
 * Compose Foundation alone: its label, its parents and the editor's actions on it. Colours, sizes and
 * wording follow the package's stylesheet.
 */

private val MENU = Color(0xFF05807F)
private val MENU_HOVER = Color(0xFF0D5454)
private val ITEM_TEXT = Color(0xFF1B243F)
private val ITEM_SELECTED = MENU
private val ITEM_DELETE = Color(0xFFC11C14)
private val ITEM_HOVER = Color(0xFFEFF1F3)
private val DROPDOWN_BORDER = Color(0xFFDFE3E8)
private val CORNERS = RoundedCornerShape(5.dp)

/** Between the menu and the block it is on, as the package spaces them. */
private val MENU_GAP = 10.dp

/** What the parent button reads as to assistive technology and tests. */
internal const val PARENTS_DESCRIPTION = "Parent blocks"

/** What the actions button reads as to assistive technology and tests. */
internal const val ACTIONS_DESCRIPTION = "Block actions"

/** Which of the menu's two lists is open. */
private enum class Dropdown { Breadcrumbs, Actions }

/**
 * The selected block's menu, over the block, while the editor has described one and the block is on
 * screen.
 *
 * It is one popup for the whole tree, drawn against where the selected block last said it was, rather
 * than one per block: a popup lays out an empty node in its parent, and in a block's parent that
 * would move the blocks after it.
 */
@Composable
internal fun EditorMenu(editor: VisualEditor) {
    val selected = editor.selected.value ?: return
    val menu = selected.menu ?: return
    val bounds = editor.selectedBounds.value?.takeIf { it.first == selected.uid }?.second ?: return
    if (bounds.isEmpty) return

    // Reset by every selection, as the package hides its lists whenever the editor describes a block.
    var dropdown by remember(selected) { mutableStateOf<Dropdown?>(null) }
    // Moving, duplicating or deleting the block hides its menu until the editor selects it again.
    var hidden by remember(selected) { mutableStateOf(false) }
    if (hidden) return

    val gap = with(LocalDensity.current) { MENU_GAP.roundToPx() }
    Popup(popupPositionProvider = MenuPosition(bounds.roundToIntRect(), gap), properties = PopupProperties(focusable = false)) {
        MenuBar(
            selected = selected,
            menu = menu,
            dropdown = dropdown,
            toggle = { dropdown = if (dropdown == it) null else it },
            open = { breadcrumb ->
                dropdown = null
                editor.awaitingMenu = breadcrumb.uid
                editor.bridge.openBreadcrumb(breadcrumb)
            },
            perform = { action ->
                dropdown = null
                if (action !in KEEPS_MENU) hidden = true
                editor.bridge.perform(action)
            },
        )
    }
}

/** The actions after which the package leaves the menu up, as the block stays where it is. */
private val KEEPS_MENU = setOf(BlockAction.AddBlockBefore, BlockAction.AddBlockAfter, BlockAction.Copy)

@Composable
private fun MenuBar(
    selected: SelectedBlock,
    menu: BlockMenu,
    dropdown: Dropdown?,
    toggle: (Dropdown) -> Unit,
    open: (BlockBreadcrumb) -> Unit,
    perform: (BlockAction) -> Unit,
) {
    Row(Modifier.height(30.dp).clip(CORNERS).background(MENU), verticalAlignment = Alignment.CenterVertically) {
        if (menu.breadcrumbs.size < 2) {
            Label(menu.label, Modifier.padding(horizontal = 20.dp))
            return@Row
        }
        Box {
            MenuButton(PARENTS_DESCRIPTION, CHEVRON, label = menu.label, onClick = { toggle(Dropdown.Breadcrumbs) })
            if (dropdown == Dropdown.Breadcrumbs) Dropdown(offsetX = (-10).dp) {
                menu.breadcrumbs.forEach { breadcrumb ->
                    val current = breadcrumb.uid == selected.uid
                    Item(breadcrumb.label, if (current) ITEM_SELECTED else ITEM_TEXT, enabled = !current) { open(breadcrumb) }
                }
            }
        }
        if (menu.actions.isNotEmpty()) Box {
            MenuButton(ACTIONS_DESCRIPTION, DOTS, startLine = true, onClick = { toggle(Dropdown.Actions) })
            if (dropdown == Dropdown.Actions) Dropdown(offsetX = 10.dp) {
                menu.actions.forEachIndexed { index, action ->
                    if (index > 0 && action.group != menu.actions[index - 1].group) Separator()
                    Item(action.label, if (action == BlockAction.Delete) ITEM_DELETE else ITEM_TEXT) { perform(action) }
                }
            }
        }
    }
}

@Composable
private fun MenuButton(description: String, icon: ImageVector, label: String? = null, startLine: Boolean = false, onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    Row(
        Modifier
            .fillMaxHeight()
            .background(if (hovered) MENU_HOVER else Color.Transparent)
            // A white line down its start, as the package separates its actions button.
            .drawBehind { if (startLine) drawRect(Color.White, size = Size(1.dp.toPx(), size.height)) }
            .hoverable(interactions)
            .clickable(interactions, indication = null, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon)
        if (label != null) Label(label, Modifier.padding(end = 20.dp))
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) = BasicText(
    text,
    modifier.widthIn(max = 200.dp),
    style = TextStyle(color = Color.White, fontSize = 14.sp),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)

@Composable
private fun Icon(icon: ImageVector) =
    Image(rememberVectorPainter(icon), contentDescription = null, Modifier.padding(horizontal = 3.dp).size(24.dp))

/**
 * One of the menu's lists, below the button it belongs to unless there is no room there. It scrolls when
 * it is taller than the window, as a list of actions can be in a short preview, so that every row can
 * be reached.
 */
@Composable
private fun Dropdown(offsetX: Dp, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val position = remember(density, offsetX) {
        DropdownPosition(with(density) { offsetX.roundToPx() }, with(density) { 25.dp.roundToPx() })
    }
    Popup(popupPositionProvider = position, properties = PopupProperties(focusable = false)) {
        Column(
            Modifier
                .shadow(8.dp, CORNERS)
                .border(1.dp, DROPDOWN_BORDER, CORNERS)
                .background(Color.White, CORNERS)
                .clip(CORNERS)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 11.dp)
                .width(IntrinsicSize.Max)
                .widthIn(min = 156.dp),
        ) { content() }
    }
}

@Composable
private fun Item(text: String, color: Color, enabled: Boolean = true, onClick: () -> Unit) {
    val interactions = remember { MutableInteractionSource() }
    val hovered by interactions.collectIsHoveredAsState()
    BasicText(
        text,
        Modifier
            .fillMaxWidth()
            .background(if (hovered && enabled) ITEM_HOVER else Color.White)
            .hoverable(interactions)
            .clickable(interactions, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        style = TextStyle(color = color, fontSize = 14.sp),
        maxLines = 1,
    )
}

@Composable
private fun Separator() = Box(
    Modifier.padding(start = 20.dp, top = 11.dp, bottom = 11.dp).fillMaxWidth().height(1.dp).background(DROPDOWN_BORDER)
)

/** What the package's menu calls each action. */
internal val BlockAction.label: String
    get() = when (this) {
        BlockAction.AddBlockBefore -> "Add Block Before"
        BlockAction.AddBlockAfter -> "Add Block After"
        BlockAction.Duplicate -> "Duplicate"
        BlockAction.Copy -> "Copy"
        BlockAction.MoveForward -> "Move Forward"
        BlockAction.MoveBackward -> "Move Backward"
        BlockAction.Delete -> "Delete"
    }

/** Which run of the actions list an action is in. A line separates one run from the next. */
private val BlockAction.group: Int
    get() = when (this) {
        BlockAction.AddBlockBefore, BlockAction.AddBlockAfter, BlockAction.Duplicate, BlockAction.Copy -> 0
        BlockAction.MoveForward, BlockAction.MoveBackward -> 1
        BlockAction.Delete -> 2
    }

/**
 * Places the menu against [block], the selected block's bounds in the window: above it, as the package
 * places it, or below it when there is no room above, or inside its top when there is no room either
 * side, as for a block taller than the window. Its start is the block's, kept within the window.
 */
private data class MenuPosition(val block: IntRect, val gap: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val height = popupContentSize.height
        val y = when {
            block.top - gap - height >= 0 -> block.top - gap - height
            block.bottom + gap + height <= windowSize.height -> block.bottom + gap
            else -> block.top.coerceAtLeast(0) + gap
        }
        val x = block.left.coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

/** Places a list [top] below the top of its button and [offsetX] along from its start, or above it when there is no room below. */
private data class DropdownPosition(val offsetX: Int, val top: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val below = anchorBounds.top + top
        val y = if (below + popupContentSize.height <= windowSize.height) below
        else (anchorBounds.bottom - top - popupContentSize.height).coerceAtLeast(0)
        val x = (anchorBounds.left + offsetX).coerceAtMost(windowSize.width - popupContentSize.width).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

/** The package's parent icon: a chevron pointing back. */
private val CHEVRON: ImageVector = icon(
    "M13.73 14.284l-2.197-2.216 2.197-2.217a1.051 1.051 0 000-1.477 1.03 1.03 0 00-1.465 0l-2.93 2.955a1.043 1.043 0 00-.287.554l-.014.123v.123c.014.247.115.489.301.677l2.93 2.956a1.03 1.03 0 001.465 0 1.051 1.051 0 000-1.478z"
)

/** The package's actions icon: three dots. */
private val DOTS: ImageVector = icon(
    "M7.5 11a1.5 1.5 0 110 3 1.5 1.5 0 010-3zm10 0a1.5 1.5 0 110 3 1.5 1.5 0 010-3zm-5 0a1.5 1.5 0 110 3 1.5 1.5 0 010-3z"
)

private fun icon(path: String): ImageVector =
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(addPathNodes(path), fill = SolidColor(Color.White))
        .build()
