@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.SelectedBlock
import com.storyblok.cdn.SelectionEvent
import com.storyblok.cdn.selectionAfter
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Which block the editor's messages leave selected, shaped as the preview bridge package reads them. */
class SelectionTest {

    /** The block selected before each message, which an earlier request brought into view. */
    private val selectedU1 = SelectedBlock("u1", ancestors = listOf("page"), reveal = 3)

    /** The [SelectedBlock.reveal] a selection takes when the message asks for it to be brought into view. */
    private val next = 4

    private fun after(json: String, selected: SelectedBlock? = selectedU1): SelectedBlock? =
        selectionAfter(parse(json).unsafeCast<SelectionEvent>(), selected, reveal = next)

    @Test
    fun `clicking a block in the form selects it and brings it into view`() {
        assertEquals(SelectedBlock("u2", reveal = next), after("""{"action": "enterComponent", "storyId": "42", "componentId": "u2"}"""))
    }

    @Test
    fun `clicking the selected block in the form brings it into view again`() {
        assertEquals(next, after("""{"action": "enterComponent", "storyId": "42", "componentId": "u1"}""")?.reveal)
    }

    @Test
    fun `the editor opening another block's form selects it and brings it into view`() {
        assertEquals(SelectedBlock("u2", reveal = next), after("""{"action": "editedBlok", "blok": {"uid": "u2", "name": "teaser"}}"""))
    }

    @Test
    fun `the editor confirming the selected block does not bring it into view again`() {
        // Its answer to a tap: the block is where the author tapped it.
        val selected = after("""{"action": "editedBlok", "blok": {"uid": "u1"}, "breadcrumbs": [{"_uid": "root"}, {"_uid": "u1"}]}""")

        assertEquals(SelectedBlock("u1", ancestors = listOf("root"), reveal = 3), selected)
    }

    @Test
    fun `adding moving or duplicating a block selects it and brings it into view`() {
        for (action in listOf("addedBlock", "movedBlock", "duplicatedBlock")) {
            assertEquals(SelectedBlock("u2", reveal = next), after("""{"action": "$action", "blockId": "u2"}"""), action)
        }
    }

    @Test
    fun `moving the selected block again brings it into view again`() {
        // Equal values would not be emitted at all, so the second move would leave the block off screen.
        val first = after("""{"action": "movedBlock", "blockId": "u1"}""")
        val second = selectionAfter(parse("""{"action": "movedBlock", "blockId": "u1"}""").unsafeCast(), first, reveal = next + 1)

        assertEquals(listOf(next, next + 1), listOf(first?.reveal, second?.reveal))
    }

    @Test
    fun `a moved block keeps the blocks it is nested in`() {
        assertEquals(listOf("page"), after("""{"action": "movedBlock", "blockId": "u1"}""")?.ancestors)
    }

    @Test
    fun `a block clicked in the form carries the blocks it is nested in`() {
        val selected = after(
            """{"action": "enterComponent", "storyId": "42", "componentId": "u3",
                "breadcrumbs": [{"_uid": "page", "component": "page"}, {"_uid": "u2", "component": "grid"}, {"_uid": "u3", "component": "teaser"}]}"""
        )

        assertEquals(SelectedBlock("u3", ancestors = listOf("page", "u2"), reveal = next), selected)
    }

    @Test
    fun `a block the editor opened carries the blocks it is nested in`() {
        val selected = after("""{"action": "editedBlok", "blok": {"uid": "u3"}, "breadcrumbs": [{"_uid": "page"}, {"_uid": "u3"}]}""")

        assertEquals(SelectedBlock("u3", ancestors = listOf("page"), reveal = next), selected)
    }

    @Test
    fun `breadcrumbs that name no block are skipped`() {
        val selected = after("""{"action": "editedBlok", "blok": {"uid": "u3"}, "breadcrumbs": [{"component": "page"}, {"_uid": ""}, {"_uid": "u2"}]}""")

        assertEquals(SelectedBlock("u3", ancestors = listOf("u2"), reveal = next), selected)
    }

    @Test
    fun `deselecting leaves nothing selected`() {
        assertNull(after("""{"action": "deselectBlok"}"""))
    }

    @Test
    fun `selecting a block after deselecting brings it into view`() {
        assertEquals(SelectedBlock("u1", reveal = next), after("""{"action": "enterComponent", "componentId": "u1"}""", selected = null))
    }

    @Test
    fun `an edit leaves the selection as it was`() {
        assertEquals(selectedU1, after("""{"action": "input", "story": {"id": 42, "content": {}}}"""))
    }

    @Test
    fun `a hover is not a selection`() {
        assertEquals(selectedU1, after("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}"""))
    }

    @Test
    fun `an event that names no block leaves the selection as it was`() {
        assertEquals(selectedU1, after("""{"action": "enterComponent", "storyId": "42"}"""))
        assertEquals(selectedU1, after("""{"action": "addedBlock", "blockId": ""}"""))
    }

    @Test
    fun `an id that is not a string names no block`() {
        assertEquals(selectedU1, after("""{"action": "addedBlock", "blockId": 7}"""))
    }

    @Test
    fun `a message with no action leaves the selection as it was`() {
        assertEquals(selectedU1, after("""{"componentId": "u2"}"""))
    }
}

private fun parse(text: String): JsAny = js("JSON.parse(text)")
