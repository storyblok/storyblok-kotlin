@file:OptIn(ExperimentalTestApi::class, InternalAPI::class)

package com.storyblok.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import com.storyblok.InternalAPI
import com.storyblok.cdn.BlockAction
import com.storyblok.cdn.BlockBreadcrumb
import com.storyblok.cdn.BlockHints
import com.storyblok.cdn.BlockMenu
import com.storyblok.cdn.SelectedBlock
import com.storyblok.cdn.StoryblokBridge
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.compose.provider.blockProviderWithoutRichText
import com.storyblok.ktor.Api.Config.Version
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Blocks as the Visual Editor previews them: opened by a tap, outlined and scrolled to while selected. */
class VisualEditorTest {

    @Serializable @SerialName("card")
    class Card(val clickable: Boolean = true) : Component()

    @Serializable @SerialName("section")
    class Section(val cards: List<Card>) : Component()

    private fun card(uid: String, clickable: Boolean = true): Card =
        Json.decodeFromString("""{"component": "card", "_uid": "$uid", "clickable": $clickable}""")

    private fun section(uid: String, vararg cards: String): Section = Json.decodeFromString(
        """{"component": "section", "_uid": "$uid", "cards": [${cards.joinToString { """{"component": "card", "_uid": "$it"}""" }}]}"""
    )

    /**
     * An editor that opens whatever it is handed, as the real one does with a block that has an
     * `_editable` — except [uneditable] ones, which it ignores as it would a block without.
     */
    private class Editor(private val uneditable: Set<String> = emptySet()) : StoryblokBridge {
        val opened = mutableListOf<String>()
        val performed = mutableListOf<BlockAction>()
        val openedBreadcrumbs = mutableListOf<String>()
        override val selectedBlock = MutableStateFlow<SelectedBlock?>(null)
        override val hints = MutableStateFlow(BlockHints())

        override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
            flowOf(story)

        // Keeping the selection of a block that is selected already, menu and all, as the real one does.
        override fun openBlock(block: Component) {
            opened += block.uid
            if (block.uid !in uneditable && selectedBlock.value?.uid != block.uid) selectedBlock.value = SelectedBlock(block.uid)
        }

        override fun perform(action: BlockAction) {
            performed += action
        }

        override fun openBreadcrumb(breadcrumb: BlockBreadcrumb) {
            openedBreadcrumbs += breadcrumb.uid
            selectedBlock.value = SelectedBlock(breadcrumb.uid)
        }

        override fun dismissHover() = hints.update { it.copy(hovered = null) }

        override fun destroy() = Unit
    }

    /** Taps that got past the editor to the cards' own click handlers. */
    private val clicks = mutableListOf<String>()

    /** Cards are clickable boxes; a section is a padded column of them, so it can be tapped between its cards. */
    private val blocks: BlockScope = blockProviderWithoutRichText {
        block<Card> { card, modifier ->
            Box(
                modifier
                    .testTag(card.uid)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White)
                    .then(if (card.clickable) Modifier.clickable { clicks += card.uid } else Modifier)
            )
        }
        block<Section> { section, modifier ->
            Column(modifier.testTag(section.uid).padding(20.dp)) {
                section.cards.forEach { Block(it) }
            }
        }
    }.blockScope

    @Composable
    private fun Previewed(editor: Editor, content: @Composable () -> Unit) {
        val selected = editor.selectedBlock.collectAsState()
        val hints = editor.hints.collectAsState()
        val scope = rememberCoroutineScope()
        // As Storyblok provides it, menu and all.
        val visualEditor = remember { VisualEditor(editor, selected, scope, hints) }
        CompositionLocalProvider(LocalVisualEditor provides visualEditor, content)
        EditorMenu(visualEditor)
    }

    @Test
    fun `a block outside the editor takes its first tap`() = runComposeUiTest {
        setContent { blocks.Block(card("c1")) }

        onNodeWithTag("c1").performClick()

        assertEquals(listOf("c1"), clicks)
    }

    @Test
    fun `the first tap on a block opens it in the editor rather than clicking it`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performClick()

        assertEquals(listOf("c1"), editor.opened)
        assertEquals("c1", editor.selectedBlock.value?.uid)
        assertEquals(emptyList(), clicks)
    }

    @Test
    fun `a tap on the selected block clicks it`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performClick()
        onNodeWithTag("c1").performClick()

        assertEquals(listOf("c1"), clicks)
        // Still sent, as the preview bridge package sends it on every click: the author may have
        // navigated the form away since.
        assertEquals(listOf("c1", "c1"), editor.opened)
    }

    @Test
    fun `a tap opens the innermost block under it`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }

        onNodeWithTag("c2").performClick()

        assertEquals(listOf("c2"), editor.opened)
    }

    @Test
    fun `a tap between a section's cards opens the section`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }

        onNodeWithTag("s1").performTouchInput { click(Offset(5f, 5f)) }

        assertEquals(listOf("s1"), editor.opened)
    }

    @Test
    fun `a block the editor cannot open takes its first tap`() = runComposeUiTest {
        val editor = Editor(uneditable = setOf("c1"))
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performClick()

        assertEquals(listOf("c1"), clicks)
        assertNull(editor.selectedBlock.value)
    }

    @Test
    fun `a drag is not a tap`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performTouchInput { swipeDown() }

        assertEquals(emptyList(), editor.opened)
    }

    @Test
    fun `the selected block is outlined and no other`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }

        editor.selectedBlock.value = SelectedBlock("c1")
        waitForIdle()

        assertEquals(Color(0xFF05807F), onNodeWithTag("c1").captureToImage().toPixelMap()[1, 1])
        assertNotEquals(Color(0xFF05807F), onNodeWithTag("c2").captureToImage().toPixelMap()[1, 1])
    }

    @Test
    fun `the outline's sides are drawn inside the block's edges`() = runComposeUiTest {
        // A block as wide as the preview has its outermost pixels under the editor's frame, which hid
        // the sides of an outline drawn on them while its top and bottom showed.
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        editor.selectedBlock.value = SelectedBlock("c1")
        waitForIdle()

        val pixels = onNodeWithTag("c1").captureToImage().toPixelMap()
        val middle = pixels.height / 2
        assertEquals(Color(0xFF05807F), pixels[1, middle], "left side")
        assertEquals(Color(0xFF05807F), pixels[pixels.width - 2, middle], "right side")
        assertNotEquals(Color(0xFF05807F), pixels[0, middle], "left edge")
        assertNotEquals(Color(0xFF05807F), pixels[pixels.width - 1, middle], "right edge")
    }

    @Test
    fun `a block selected in the editor is brought into view`() = runComposeUiTest {
        val editor = Editor()
        setContent {
            Previewed(editor) {
                Column(Modifier.size(width = 200.dp, height = 250.dp).verticalScroll(rememberScrollState())) {
                    (1..10).forEach { blocks.Block(card("c$it")) }
                }
            }
        }

        editor.selectedBlock.value = SelectedBlock("c8")
        waitForIdle()

        onNodeWithTag("c8").assertIsDisplayed()
    }

    @Test
    fun `a tapped block is not scrolled to`() = runComposeUiTest {
        val editor = Editor()
        val scroll = ScrollState(0)
        setContent {
            Previewed(editor) {
                Column(Modifier.size(width = 200.dp, height = 250.dp).verticalScroll(scroll)) {
                    (1..10).forEach { blocks.Block(card("c$it", clickable = false)) }
                }
            }
        }

        // The third card is half on screen: selecting it from the editor would scroll the rest of it in.
        // Not clickable, because a clickable takes focus on desktop and focus scrolls it in regardless.
        onNodeWithTag("c3").performTouchInput { click(Offset(centerX, 10f)) }
        waitForIdle()

        assertEquals("c3", editor.selectedBlock.value?.uid)
        assertEquals(0, scroll.value)
    }

    @Test
    fun `a page outside the editor gives its blocks no editor`() = runComposeUiTest {
        var composed = false
        var seen: VisualEditor? = null
        setContent {
            Storyblok(accessToken = "token", version = Version.Draft, blockProvider = blockProviderWithoutRichText {}) {
                composed = true
                seen = LocalVisualEditor.current
            }
        }

        assertTrue(composed)
        assertNull(seen)
    }

    /** A list 250dp tall, of [blocks] after [before] fixed items 100dp tall, as a page's body might follow a header. */
    @Composable
    private fun List(state: LazyListState, blocks: List<Component>, before: Int = 0) {
        LazyColumn(Modifier.testTag("list").size(width = 200.dp, height = 250.dp), state = state) {
            repeat(before) { item { Box(Modifier.testTag("header$it").height(100.dp)) } }
            with(this@VisualEditorTest.blocks) { blocks(blocks, state) }
        }
    }

    private fun cards(count: Int) = (1..count).map { card("c$it", clickable = false) }

    @Test
    fun `blocks fill a lazy list in order`() = runComposeUiTest {
        setContent { List(LazyListState(), cards(2)) }

        onNodeWithTag("c1").assertIsDisplayed()
        onNodeWithTag("c2").assertIsDisplayed()
    }

    @Test
    fun `selecting a block off screen scrolls the list to it`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, cards(20)) } }

        editor.selectedBlock.value = SelectedBlock("c15")
        waitForIdle()

        onNodeWithTag("c15").assertIsDisplayed()
    }

    @Test
    fun `items before the blocks are allowed for`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, cards(20), before = 2) } }

        editor.selectedBlock.value = SelectedBlock("c15")
        waitForIdle()

        // Two headers, then c1 at index 2: c15 is item 16, scrolled to the top.
        assertEquals(16, state.firstVisibleItemIndex)
    }

    @Test
    fun `selecting a block nested in an item off screen scrolls to that item`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        val sections = (1..10).map { section("s$it", "s$it-card") }
        setContent { Previewed(editor) { List(state, sections) } }

        editor.selectedBlock.value = SelectedBlock("s8-card", ancestors = listOf("page", "s8"))
        waitForIdle()

        onNodeWithTag("s8-card").assertIsDisplayed()
    }

    @Test
    fun `a nested block the editor gives no ancestors for is not followed`() = runComposeUiTest {
        // As when a block is added inside another: the list has nothing to find its item by.
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, (1..10).map { section("s$it", "s$it-card") }) } }

        editor.selectedBlock.value = SelectedBlock("s8-card")
        waitForIdle()

        assertEquals(0, state.firstVisibleItemIndex)
    }

    @Test
    fun `the list lets the author scroll away from a selection`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, cards(20)) } }

        editor.selectedBlock.value = SelectedBlock("c15")
        waitForIdle()
        onNodeWithTag("list").performScrollToIndex(0)
        waitForIdle()

        // The items scrolled back in start following afresh; they must not take the list back to c15.
        assertEquals(0, state.firstVisibleItemIndex)
    }

    @Test
    fun `blocks that share a uid are still listed`() = runComposeUiTest {
        // Lazy lists reject duplicate keys, so these are keyed by position.
        setContent { List(LazyListState(), listOf(card("same", clickable = false), card("same", clickable = false))) }

        onAllNodesWithTag("same").assertCountEquals(2)
    }

    /** The menu the editor describes for card [uid] in section `s1` on page `page`, offering [actions]. */
    private fun cardMenu(uid: String = "c1", actions: List<BlockAction> = BlockAction.entries) = BlockMenu(
        label = "Teaser Card",
        breadcrumbs = listOf(BlockBreadcrumb("page", "page", "Page"), BlockBreadcrumb("s1", "section", "Section"), BlockBreadcrumb(uid, "card", "Teaser Card")),
        actions = actions,
    )

    @Test
    fun `the selected block's menu shows its label`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("page")) } }

        editor.selectedBlock.value = SelectedBlock("page", menu = BlockMenu("Page", listOf(BlockBreadcrumb("page", "page", "Page"))))
        waitForIdle()

        onNodeWithText("Page").assertIsDisplayed()
        // The story's content has no parents and no actions.
        onNodeWithContentDescription(PARENTS_DESCRIPTION).assertDoesNotExist()
        onNodeWithContentDescription(ACTIONS_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `a block has no menu until the editor describes it`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performClick()
        waitForIdle()

        onNodeWithContentDescription(PARENTS_DESCRIPTION).assertDoesNotExist()
        onNodeWithText("Teaser Card").assertDoesNotExist()
    }

    @Test
    fun `no menu is shown for a selected block that is not drawn`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        editor.selectedBlock.value = SelectedBlock("c9", menu = cardMenu("c9"))
        waitForIdle()

        onNodeWithText("Teaser Card").assertDoesNotExist()
    }

    @Test
    fun `a nested block's menu lists the blocks it is nested in`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithText("Teaser Card").assertIsDisplayed()
        onNodeWithContentDescription(PARENTS_DESCRIPTION).performClick()

        onNodeWithText("Page").assertIsDisplayed()
        onNodeWithText("Section").assertIsDisplayed()
        onAllNodesWithText("Teaser Card").assertCountEquals(2)
    }

    @Test
    fun `choosing a parent opens it`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithContentDescription(PARENTS_DESCRIPTION).performClick()
        onNodeWithText("Section").performClick()
        waitForIdle()

        assertEquals(listOf("s1"), editor.openedBreadcrumbs)
        // The editor describes the parent in answer to that, so it is not asked a second time.
        assertEquals(emptyList<String>(), editor.opened)
        onNodeWithText("Page").assertDoesNotExist()
    }

    @Test
    fun `the selected block cannot be chosen from its own breadcrumbs`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithContentDescription(PARENTS_DESCRIPTION).performClick()
        onAllNodesWithText("Teaser Card")[1].performClick()

        assertEquals(emptyList<String>(), editor.openedBreadcrumbs)
    }

    @Test
    fun `the actions menu shows only the actions the editor allows`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu(actions = listOf(BlockAction.AddBlockBefore, BlockAction.MoveForward)))
        waitForIdle()

        onNodeWithContentDescription(ACTIONS_DESCRIPTION).performClick()

        onNodeWithText("Add Block Before").assertIsDisplayed()
        onNodeWithText("Move Forward").assertIsDisplayed()
        for (absent in listOf("Add Block After", "Duplicate", "Copy", "Move Backward", "Delete")) {
            onNodeWithText(absent).assertDoesNotExist()
        }
    }

    @Test
    fun `a block the editor allows nothing has no actions button`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu(actions = emptyList()))
        waitForIdle()

        onNodeWithContentDescription(PARENTS_DESCRIPTION).assertIsDisplayed()
        onNodeWithContentDescription(ACTIONS_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `each action asks the editor for itself`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }

        for (action in BlockAction.entries) {
            // A fresh selection each time, as an action that moves or deletes the block hides its menu.
            editor.selectedBlock.value = null
            waitForIdle()
            editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
            waitForIdle()
            onNodeWithContentDescription(ACTIONS_DESCRIPTION).performClick()
            onNodeWithText(action.label).performClick()
            waitForIdle()
        }

        assertEquals(BlockAction.entries.toList(), editor.performed)
    }

    @Test
    fun `adding a block leaves the menu up`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithContentDescription(ACTIONS_DESCRIPTION).performClick()
        onNodeWithText("Add Block After").performClick()
        waitForIdle()

        onNodeWithText("Add Block After").assertDoesNotExist()
        onNodeWithText("Teaser Card").assertIsDisplayed()
    }

    @Test
    fun `moving a block hides its menu until the editor selects it again`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithContentDescription(ACTIONS_DESCRIPTION).performClick()
        onNodeWithText("Move Forward").performClick()
        waitForIdle()
        onNodeWithText("Teaser Card").assertDoesNotExist()

        // As the editor reports the move, and then describes the block it was asked for.
        editor.selectedBlock.value = SelectedBlock("c1")
        waitForIdle()
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()
        onNodeWithText("Teaser Card").assertIsDisplayed()
    }

    @Test
    fun `a block the editor selects without describing it is opened for its menu`() = runComposeUiTest {
        // As when the author adds, moves or duplicates one.
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }

        editor.selectedBlock.value = SelectedBlock("c2")
        waitForIdle()

        assertEquals(listOf("c2"), editor.opened)
    }

    @Test
    fun `a tapped block is opened once`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(card("c1")) } }

        onNodeWithTag("c1").performClick()
        waitForIdle()

        assertEquals(listOf("c1"), editor.opened)
    }

    @Test
    fun `another block can be tapped while the menu is up`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }
        editor.selectedBlock.value = SelectedBlock("c1", menu = cardMenu())
        waitForIdle()

        onNodeWithTag("c2").performClick()

        assertEquals("c2", editor.selectedBlock.value?.uid)
    }

    @Test
    fun `a block hovered in the editor's form is outlined`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }

        editor.hints.value = BlockHints(hovered = "c2")
        waitForIdle()

        val middle = onNodeWithTag("c2").captureToImage().toPixelMap().let { it[1, it.height / 2] }
        assertEquals(Color(0xFF05807F), middle)
        assertNotEquals(Color(0xFF05807F), onNodeWithTag("c1").captureToImage().toPixelMap().let { it[1, it.height / 2] })
    }

    @Test
    fun `the hover hint goes once the mouse rests on the page`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }
        editor.hints.value = BlockHints(hovered = "c2")
        waitForIdle()

        onNodeWithTag("c1").performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(500)
        assertEquals("c2", editor.hints.value.hovered)
        mainClock.advanceTimeBy(500)

        assertNull(editor.hints.value.hovered)
    }

    @Test
    fun `highlighted blocks are tinted`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }

        editor.hints.value = BlockHints(highlighted = listOf("c1"))
        waitForIdle()

        val pixels = onNodeWithTag("c1").captureToImage().toPixelMap()
        assertNotEquals(Color.White, pixels[pixels.width / 2, pixels.height / 2])
        assertEquals(Color(0xFF05807F), pixels[1, pixels.height / 2])
        val other = onNodeWithTag("c2").captureToImage().toPixelMap()
        assertEquals(Color.White, other[other.width / 2, other.height / 2])
    }

    @Test
    fun `the block the editor highlights for attention is brought into view`() = runComposeUiTest {
        val editor = Editor()
        setContent {
            Previewed(editor) {
                Column(Modifier.size(width = 200.dp, height = 250.dp).verticalScroll(rememberScrollState())) {
                    (1..10).forEach { blocks.Block(card("c$it")) }
                }
            }
        }

        editor.hints.value = BlockHints(highlighted = listOf("c2", "c8"), highlightedFocus = "c8")
        waitForIdle()

        onNodeWithTag("c8").assertIsDisplayed()
    }

    @Test
    fun `every block is outlined while the mouse moves over the page`() = runComposeUiTest {
        val editor = Editor()
        setContent { Previewed(editor) { blocks.Block(section("s1", "c1", "c2")) } }
        fun outlined(tag: String) = onNodeWithTag(tag).captureToImage().toPixelMap().let { pixels ->
            (5 until pixels.height - 5).any { pixels[1, it] != Color.White }
        }
        assertFalse(outlined("c2"))

        onNodeWithTag("c1").performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(100)
        assertTrue(outlined("c2"))

        mainClock.advanceTimeBy(1000)
        assertFalse(outlined("c2"))
    }
}
