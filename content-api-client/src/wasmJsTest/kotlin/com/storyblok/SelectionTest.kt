@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

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

    @Test
    fun `clicking a block in the form selects it`() {
        assertEquals("u2", selectionAfter(event("""{"action": "enterComponent", "storyId": "42", "componentId": "u2"}"""), "u1"))
    }

    @Test
    fun `the editor opening a block's form selects it`() {
        assertEquals("u2", selectionAfter(event("""{"action": "editedBlok", "blok": {"uid": "u2", "name": "teaser"}}"""), "u1"))
    }

    @Test
    fun `adding, moving or duplicating a block selects it`() {
        for (action in listOf("addedBlock", "movedBlock", "duplicatedBlock")) {
            assertEquals("u2", selectionAfter(event("""{"action": "$action", "blockId": "u2"}"""), "u1"), action)
        }
    }

    @Test
    fun `deselecting leaves nothing selected`() {
        assertNull(selectionAfter(event("""{"action": "deselectBlok"}"""), "u1"))
    }

    @Test
    fun `an edit leaves the selection as it was`() {
        assertEquals("u1", selectionAfter(event("""{"action": "input", "story": {"id": 42, "content": {}}}"""), "u1"))
    }

    @Test
    fun `a hover is not a selection`() {
        assertEquals("u1", selectionAfter(event("""{"action": "hoverComponent", "storyId": "42", "componentId": "u2"}"""), "u1"))
    }

    @Test
    fun `an event that names no block leaves the selection as it was`() {
        assertEquals("u1", selectionAfter(event("""{"action": "enterComponent", "storyId": "42"}"""), "u1"))
        assertEquals("u1", selectionAfter(event("""{"action": "addedBlock", "blockId": ""}"""), "u1"))
    }

    @Test
    fun `an id that is not a string names no block`() {
        assertEquals("u1", selectionAfter(event("""{"action": "addedBlock", "blockId": 7}"""), "u1"))
    }

    @Test
    fun `a message with no action leaves the selection as it was`() {
        assertEquals("u1", selectionAfter(event("""{"componentId": "u2"}"""), "u1"))
    }
}

private fun parse(text: String): JsAny = js("JSON.parse(text)")
