@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.EditorSelection
import com.storyblok.cdn.SelectionEvent
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

/**
 * The editor's selection as the bridge keeps it, including the second request to bring a block into
 * view that the content following an add, move or duplicate makes.
 */
class EditorSelectionTest {

    private val clock = TestTimeSource()
    private val selection = EditorSelection(clock, revealAgainWithin = 1.seconds)

    private fun send(json: String) = selection.handle(parseEvent(json).unsafeCast<SelectionEvent>())

    private val reveal get() = selection.selected.value?.reveal

    @Test
    fun `content soon after a move asks for the block to be brought into view again`() {
        send("""{"action": "movedBlock", "blockId": "u1"}""")
        val moved = reveal
        clock += 200.milliseconds
        send("""{"action": "input", "story": {"id": 1, "content": {}}}""")

        assertEquals(moved!! + 1, reveal)
        assertEquals("u1", selection.selected.value?.uid)
    }

    @Test
    fun `content soon after an add or a duplicate asks again too`() {
        for (action in listOf("addedBlock", "duplicatedBlock")) {
            send("""{"action": "$action", "blockId": "u-$action"}""")
            val added = reveal
            send("""{"action": "input"}""")

            assertEquals(added!! + 1, reveal, action)
        }
    }

    @Test
    fun `only the first content after a move asks again`() {
        // Later keystrokes are the author typing, who may have scrolled away by then.
        send("""{"action": "movedBlock", "blockId": "u1"}""")
        send("""{"action": "input"}""")
        val again = reveal
        send("""{"action": "input"}""")

        assertEquals(again, reveal)
    }

    @Test
    fun `content long after a move does not ask again`() {
        send("""{"action": "movedBlock", "blockId": "u1"}""")
        val moved = reveal
        clock += 2.seconds
        send("""{"action": "input"}""")

        assertEquals(moved, reveal)
    }

    @Test
    fun `content after a block picked in the form does not ask again`() {
        send("""{"action": "enterComponent", "componentId": "u1"}""")
        val picked = reveal
        send("""{"action": "input"}""")

        assertEquals(picked, reveal)
    }

    @Test
    fun `another selection after a move cancels the second request`() {
        send("""{"action": "movedBlock", "blockId": "u1"}""")
        send("""{"action": "enterComponent", "componentId": "u2"}""")
        val picked = reveal
        send("""{"action": "input"}""")

        assertEquals(picked, reveal)
    }

    @Test
    fun `a tap after a move cancels the second request`() {
        // The tapped block is where the author tapped it, and must not be scrolled.
        send("""{"action": "movedBlock", "blockId": "u1"}""")
        selection.opened("u2")
        val tapped = reveal
        send("""{"action": "input"}""")

        assertEquals(tapped, reveal)
    }

    @Test
    fun `a tap selects without asking for the block to be brought into view`() {
        send("""{"action": "enterComponent", "componentId": "u1"}""")
        val picked = reveal
        selection.opened("u2")

        assertEquals("u2", selection.selected.value?.uid)
        assertEquals(picked, reveal)
    }

    @Test
    fun `selecting again after deselecting gets a new request`() {
        send("""{"action": "enterComponent", "componentId": "u1"}""")
        val first = reveal
        send("""{"action": "deselectBlok"}""")
        send("""{"action": "enterComponent", "componentId": "u1"}""")

        assertEquals(first!! + 1, reveal)
    }
}

private fun parseEvent(text: String): JsAny = js("JSON.parse(text)")
