package com.storyblok

import com.storyblok.cdn.schema.RichText
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Rich text as the Visual Editor sends it, which is not quite as the Content Delivery API does: the
 * editor decorates the document with an `attrs` object the API omits entirely.
 */
class RichTextEditorShapeTest {

    private val json = Json { isLenient = true; classDiscriminator = "type" }

    @Test
    fun `the document the editor decorates with attrs still decodes`() {
        val document = json.decodeFromString<RichText>(
            """{"type": "doc", "attrs": {"backgroundColor": null}, "content": []}"""
        )

        assertIs<RichText.Document>(document)
    }

    @Test
    fun `the document the API sends without attrs still decodes`() {
        val document = json.decodeFromString<RichText>("""{"type": "doc", "content": []}""")

        assertEquals(null, assertIs<RichText.Document>(document).backgroundColor)
    }

    @Test
    fun `a document background colour is read`() {
        val document = json.decodeFromString<RichText>(
            """{"type": "doc", "attrs": {"backgroundColor": "#fff"}, "content": []}"""
        )

        assertEquals("#fff", assertIs<RichText.Document>(document).backgroundColor)
    }

    @Test
    fun `a link mark keeps reading its attributes`() {
        val mark = json.decodeFromString<RichText.Mark>(
            """{"type": "link", "attrs": {"href": "/post5", "linktype": "story"}}"""
        )

        assertEquals("/post5", assertIs<RichText.Mark.Link>(mark).href)
    }
}
