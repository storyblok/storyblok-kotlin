@file:OptIn(InternalAPI::class)

package com.storyblok

import com.storyblok.cdn.BlockEdit
import com.storyblok.cdn.RichTextEdit
import com.storyblok.cdn.editBetween
import com.storyblok.cdn.schema.RichText
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** How the preview finds where the author changed the story, from the story before and after. */
class BlockEditTest {

    private fun edit(before: String, after: String) =
        editBetween(Json.parseToJsonElement(before), Json.parseToJsonElement(after))

    private fun paragraph(text: String) = """{"type": "paragraph", "content": [{"type": "text", "text": "$text"}]}"""

    private fun page(vararg body: String, title: String = "Home") =
        """{"_uid": "page", "component": "page", "title": "$title", "body": [${body.joinToString()}]}"""

    private fun teaser(uid: String, headline: String) = """{"_uid": "$uid", "component": "teaser", "headline": "$headline"}"""

    private fun grid(uid: String, vararg columns: String) = """{"_uid": "$uid", "component": "grid", "columns": [${columns.joinToString()}]}"""

    @Test
    fun `nothing changed is no edit`() {
        assertNull(edit(page(teaser("a", "Hi")), page(teaser("a", "Hi"))))
    }

    @Test
    fun `a field of the story's content is an edit to it`() {
        assertEquals(BlockEdit("page"), edit(page(), page(title = "Welcome")))
    }

    @Test
    fun `a field of a nested block is an edit to that block inside its ancestors`() {
        assertEquals(
            BlockEdit("b", ancestors = listOf("page", "g")),
            edit(
                page(grid("g", teaser("a", "One"), teaser("b", "Two"))),
                page(grid("g", teaser("a", "One"), teaser("b", "Twos"))),
            ),
        )
    }

    @Test
    fun `a block added is the edit`() {
        assertEquals(
            BlockEdit("c", ancestors = listOf("page")),
            edit(page(teaser("a", "One"), teaser("b", "Two")), page(teaser("a", "One"), teaser("c", "New"), teaser("b", "Two"))),
        )
    }

    @Test
    fun `a block removed or moved is an edit to the block holding it`() {
        assertEquals(BlockEdit("page"), edit(page(teaser("a", "One"), teaser("b", "Two")), page(teaser("b", "Two"))))
        assertEquals(BlockEdit("page"), edit(page(teaser("a", "One"), teaser("b", "Two")), page(teaser("b", "Two"), teaser("a", "One"))))
    }

    @Test
    fun `typing in rich text names the node typed in`() {
        val before = """{"_uid": "post", "component": "post", "body": {"type": "doc", "content": [${paragraph("One")}, ${paragraph("Two")}]}}"""
        val after = before.replace("Two", "Twos")

        assertEquals(BlockEdit("post", richText = listOf(RichTextEdit(index = 1, nodes = 2, text = "Twos"))), edit(before, after))
    }

    @Test
    fun `a node inserted in rich text is the edit`() {
        val before = """{"_uid": "post", "component": "post", "body": {"type": "doc", "content": [${paragraph("One")}, ${paragraph("Two")}]}}"""
        val after = before.replace(paragraph("One") + ",", paragraph("One") + ", " + paragraph("") + ",")

        assertEquals(listOf(RichTextEdit(index = 1, nodes = 3, text = "")), edit(before, after)?.richText)
    }

    @Test
    fun `a node removed from the end of rich text puts the edit on the last node left`() {
        val before = """{"_uid": "post", "component": "post", "body": {"type": "doc", "content": [${paragraph("One")}, ${paragraph("Two")}]}}"""
        val after = before.replace(", " + paragraph("Two"), "")

        assertEquals(listOf(RichTextEdit(index = 0, nodes = 1, text = "One")), edit(before, after)?.richText)
    }

    @Test
    fun `a block in rich text is edited in its node`() {
        fun post(headline: String) = """{"_uid": "post", "component": "post", "body": {"type": "doc", "content": [
            {"type": "blok", "attrs": {"id": "x", "body": [${teaser("t", headline)}]}}, ${paragraph("Body")}]}}"""

        assertEquals(
            BlockEdit("t", ancestors = listOf("post"), richText = listOf(RichTextEdit(index = 0, nodes = 2, text = ""))),
            edit(post("Hi"), post("Hello")),
        )
    }

    @Test
    fun `a document is recognised by its nodes and the edited node's text`() {
        val document = Json.decodeFromString(
            RichText.serializer(),
            """{"type": "doc", "content": [${paragraph("One")}, {"type": "paragraph", "content": [
                {"type": "text", "text": "Tw"}, {"type": "text", "text": "os", "marks": [{"type": "bold"}]}]}]}""",
        ) as RichText.Document

        assertEquals(1, BlockEdit("post", richText = listOf(RichTextEdit(1, 2, "Twos"))).nodeIn(document))
        assertNull(BlockEdit("post", richText = listOf(RichTextEdit(1, 3, "Twos"))).nodeIn(document))
        assertNull(BlockEdit("post", richText = listOf(RichTextEdit(1, 2, "Two"))).nodeIn(document))
        assertNull(BlockEdit("post").nodeIn(document))
    }
}
