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

    private fun event(json: String): SelectionEvent = parse(json).unsafeCast()

    private val selectedU1 = SelectedBlock("u1")

    @Test
    fun `clicking a block in the form selects it`() {
        assertEquals(SelectedBlock("u2"), selectionAfter(event("""{"action": "enterComponent", "storyId": "42", "componentId": "u2"}"""), selectedU1))
    }

    @Test
    fun `the editor opening a block's form selects it`() {
        assertEquals(SelectedBlock("u2"), selectionAfter(event("""{"action": "editedBlok", "blok": {"uid": "u2", "name": "teaser"}}"""), selectedU1))
    }

    @Test
    fun `adding, moving or duplicating a block selects it`() {
        for (action in listOf("addedBlock", "movedBlock", "duplicatedBlock")) {
            assertEquals(SelectedBlock("u2"), selectionAfter(event("""{"action": "$action", "blockId": "u2"}"""), selectedU1), action)
        }
    }

    @Test
    fun `a block clicked in the form carries the blocks it is nested in`() {
        val selected = selectionAfter(
            event(
                """{"action": "enterComponent", "storyId": "42", "componentId": "u3",
                    "breadcrumbs": [{"_uid": "page", "component": "page"}, {"_uid": "u2", "component": "grid"}, {"_uid": "u3", "component": "teaser"}]}"""
            ),
            selectedU1,
        )

        assertEquals(SelectedBlock("u3", ancestors = listOf("page", "u2")), selected)
    }

    @Test
    fun `a block the editor opened carries the blocks it is nested in`() {
        val selected = selectionAfter(
            event("""{"action": "editedBlok", "blok": {"uid": "u3"}, "breadcrumbs": [{"_uid": "page"}, {"_uid": "u3"}]}"""),
            selectedU1,
        )

        assertEquals(SelectedBlock("u3", ancestors = listOf("page")), selected)
    }

    @Test
    fun `breadcrumbs that name no block are skipped`() {
        val selected = selectionAfter(
            event("""{"action": "editedBlok", "blok": {"uid": "u3"}, "breadcrumbs": [{"component": "page"}, {"_uid": ""}, {"_uid": "u2"}]}"""),
            selectedU1,
        )

        assertEquals(SelectedBlock("u3", ancestors = listOf("u2")), selected)
    }

    @Test
    fun `deselecting leaves nothing selected`() {
        assertNull(selectionAfter(event("""{"action": "deselectBlok"}"""), selectedU1))
    }

    @Test
    fun `an edit leaves the selection as it was`() {
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"action": "input", "story": {"id": 42, "content": {}}}"""), selectedU1))
    }

    @Test
    fun `a hover is not a selection`() {
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}"""), selectedU1))
    }

    @Test
    fun `an event that names no block leaves the selection as it was`() {
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"action": "enterComponent", "storyId": "42"}"""), selectedU1))
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"action": "addedBlock", "blockId": ""}"""), selectedU1))
    }

    @Test
    fun `an id that is not a string names no block`() {
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"action": "addedBlock", "blockId": 7}"""), selectedU1))
    }

    @Test
    fun `a message with no action leaves the selection as it was`() {
        assertEquals(SelectedBlock("u1"), selectionAfter(event("""{"componentId": "u2"}"""), selectedU1))
    }
}

private fun parse(text: String): JsAny = js("JSON.parse(text)")
