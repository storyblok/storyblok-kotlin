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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import com.storyblok.InternalAPI
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
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
        override val selectedBlock = MutableStateFlow<SelectedBlock?>(null)

        override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
            flowOf(story)

        override fun openBlock(block: Component) {
            opened += block.uid
            // As the real bridge does: a tap selects without asking to be brought into view.
            val current = selectedBlock.value
            if (block.uid in uneditable || current?.uid == block.uid) return
            selectedBlock.value = SelectedBlock(block.uid, reveal = current?.reveal ?: 0)
        }

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
        val scope = rememberCoroutineScope()
        CompositionLocalProvider(LocalVisualEditor provides remember { VisualEditor(editor, selected, scope) }, content)
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


    @Test
    fun `a block selected before it arrives is scrolled to when it does`() = runComposeUiTest {
        // The editor can send addedBlock before the content holding the new block.
        val editor = Editor()
        val state = LazyListState()
        var cards by mutableStateOf(cards(20))
        setContent { Previewed(editor) { List(state, cards) } }

        editor.selectedBlock.value = SelectedBlock("c21", reveal = 1)
        waitForIdle()
        cards = cards + card("c21", clickable = false)
        waitForIdle()

        onNodeWithTag("c21").assertIsDisplayed()
    }

    @Test
    fun `a moved block is followed to where it lands`() = runComposeUiTest {
        // The move is announced before the content that moves the block, which first finds it on screen.
        val editor = Editor()
        val state = LazyListState()
        var cards by mutableStateOf(cards(20))
        setContent { Previewed(editor) { List(state, cards) } }

        editor.selectedBlock.value = SelectedBlock("c2", reveal = 1)
        waitForIdle()
        cards = cards.filterNot { it.uid == "c2" } + cards.first { it.uid == "c2" }
        waitForIdle()

        onNodeWithTag("c2").assertIsDisplayed()
    }

    @Test
    fun `moving the same block again follows it again`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, cards(20)) } }

        editor.selectedBlock.value = SelectedBlock("c15", reveal = 1)
        waitForIdle()
        onNodeWithTag("list").performScrollToIndex(0)
        waitForIdle()
        editor.selectedBlock.value = SelectedBlock("c15", reveal = 2)
        waitForIdle()

        onNodeWithTag("c15").assertIsDisplayed()
    }

    @Test
    fun `a block selected again after deselection is scrolled to again`() = runComposeUiTest {
        val editor = Editor()
        val state = LazyListState()
        setContent { Previewed(editor) { List(state, cards(20)) } }

        editor.selectedBlock.value = SelectedBlock("c15", reveal = 1)
        waitForIdle()
        onNodeWithTag("list").performScrollToIndex(0)
        editor.selectedBlock.value = null
        waitForIdle()
        editor.selectedBlock.value = SelectedBlock("c15", reveal = 2)
        waitForIdle()

        onNodeWithTag("c15").assertIsDisplayed()
    }

    @Test
    fun `a composed block selected again after deselection is brought into view again`() = runComposeUiTest {
        val editor = Editor()
        val scroll = ScrollState(0)
        setContent {
            Previewed(editor) {
                Column(Modifier.size(width = 200.dp, height = 250.dp).verticalScroll(scroll)) {
                    (1..10).forEach { blocks.Block(card("c$it", clickable = false)) }
                }
            }
        }

        editor.selectedBlock.value = SelectedBlock("c8", reveal = 1)
        waitForIdle()
        runOnIdle { kotlinx.coroutines.runBlocking { scroll.scrollTo(0) } }
        editor.selectedBlock.value = null
        waitForIdle()
        editor.selectedBlock.value = SelectedBlock("c8", reveal = 2)
        waitForIdle()

        onNodeWithTag("c8").assertIsDisplayed()
    }

    @Test
    fun `two groups of blocks in one list each follow their own`() = runComposeUiTest {
        // The first group's item is on screen too, and must not take the selection for its own.
        val editor = Editor()
        val state = LazyListState()
        val first = listOf(card("a1", clickable = false))
        val second = cards(20)
        setContent {
            Previewed(editor) {
                LazyColumn(Modifier.size(width = 200.dp, height = 250.dp), state = state) {
                    with(blocks) {
                        blocks(first, state)
                        blocks(second, state)
                    }
                }
            }
        }

        editor.selectedBlock.value = SelectedBlock("c15", reveal = 1)
        waitForIdle()

        onNodeWithTag("c15").assertIsDisplayed()
    }

}
