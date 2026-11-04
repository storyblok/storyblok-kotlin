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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import com.storyblok.InternalAPI
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
        override val selectedBlock = MutableStateFlow<String?>(null)

        override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
            flowOf(story)

        override fun openBlock(block: Component) {
            opened += block.uid
            if (block.uid !in uneditable) selectedBlock.value = block.uid
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
        CompositionLocalProvider(LocalVisualEditor provides remember { VisualEditor(editor, selected) }, content)
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
        assertEquals("c1", editor.selectedBlock.value)
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

        editor.selectedBlock.value = "c1"
        waitForIdle()

        assertEquals(Color(0xFF05807F), onNodeWithTag("c1").captureToImage().toPixelMap()[0, 0])
        assertNotEquals(Color(0xFF05807F), onNodeWithTag("c2").captureToImage().toPixelMap()[0, 0])
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

        editor.selectedBlock.value = "c8"
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

        assertEquals("c3", editor.selectedBlock.value)
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
}
