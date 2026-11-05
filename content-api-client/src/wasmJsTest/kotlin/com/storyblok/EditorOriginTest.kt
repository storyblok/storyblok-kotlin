@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.EditorInbox
import com.storyblok.cdn.editorOrigin
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.js
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Which editor a preview talks to, and that nothing else can drive it. */
class EditorOriginTest {

    @Test
    fun `a preview loaded by the editor talks to it`() {
        assertEquals("https://app.storyblok.com", editorOrigin("https://app.storyblok.com/", query = "?_storyblok_tk=1"))
    }

    @Test
    fun `a preview loaded by the China editor talks to it`() {
        assertEquals("https://app.storyblokchina.cn", editorOrigin("https://app.storyblokchina.cn/#/me/spaces/1", query = "?_storyblok_tk=1"))
    }

    @Test
    fun `a preview loaded by the staging editor talks to it`() {
        assertEquals("https://app-beta.storyblok.com", editorOrigin("https://app-beta.storyblok.com/", query = ""))
    }

    @Test
    fun `a referrer that only starts like an editor is not one`() {
        assertEquals("https://app.storyblok.com", editorOrigin("https://app.storyblok.com.example.org/", query = ""))
    }

    @Test
    fun `a referrer from elsewhere is not trusted`() {
        assertEquals("https://app.storyblok.com", editorOrigin("https://example.org/", query = ""))
    }

    @Test
    fun `without a referrer the query picks the staging editor`() {
        assertEquals("https://app-beta.storyblok.com", editorOrigin("", query = "?_storyblok_tk=1&_storyblok_env=stage"))
    }

    @Test
    fun `without a referrer the preview talks to the editor`() {
        assertEquals("https://app.storyblok.com", editorOrigin("", query = "?_storyblok_tk=1"))
    }

    @Test
    fun `messages from the editor drive the preview`() {
        val inbox = EditorInbox("https://app.storyblokchina.cn")

        inbox.receive("https://app.storyblokchina.cn", parseMessage("""{"action": "input", "story": {"id": 1, "content": {}}}"""))
        inbox.receive("https://app.storyblokchina.cn", parseMessage("""{"action": "enterComponent", "componentId": "u1"}"""))
        inbox.receive("https://app.storyblokchina.cn", parseMessage("""{"action": "hoverComponent", "componentId": "u2"}"""))

        assertNotNull(inbox.edits.value)
        assertEquals("u1", inbox.selection.selected.value?.uid)
        assertEquals("u2", inbox.state.hints.value.hovered)
    }

    @Test
    fun `messages from any other origin are ignored`() {
        // Any page may frame the preview and post to it.
        val inbox = EditorInbox("https://app.storyblok.com")

        for (origin in listOf("https://example.org", "https://app.storyblokchina.cn", "null")) {
            inbox.receive(origin, parseMessage("""{"action": "input", "story": {"id": 1, "content": {}}}"""))
            inbox.receive(origin, parseMessage("""{"action": "enterComponent", "componentId": "u1"}"""))
            inbox.receive(origin, parseMessage("""{"action": "hoverComponent", "componentId": "u2"}"""))
        }

        assertNull(inbox.edits.value)
        assertNull(inbox.selection.selected.value)
        assertNull(inbox.state.hints.value.hovered)
    }
}

private fun parseMessage(text: String): JsAny = js("JSON.parse(text)")
