package com.storyblok

import com.storyblok.cdn.editableBlock
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.storyblokJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Reading where a block lives from its `_editable`, which is what the Visual Editor is told to open. */
class EditableBlockTest {

    private val json = storyblokJson({}, {})

    private fun block(editable: String?): Component = json.decodeFromString<Component>(
        when (editable) {
            null -> """{"component": "teaser", "_uid": "u1"}"""
            else -> """{"component": "teaser", "_uid": "u1", "_editable": ${json.encodeToString(editable)}}"""
        }
    )

    @Test
    fun `a draft block names itself, its story and its space`() {
        val editable = assertNotNull(
            block("""<!--#storyblok#{"name": "teaser", "space": "1001", "uid": "u1", "id": "42"}-->""").editableBlock()
        )

        assertEquals("teaser", editable.name)
        assertEquals("1001", editable.space)
        assertEquals("u1", editable.uid)
        assertEquals("42", editable.id)
    }

    @Test
    fun `a block without a space still names itself`() {
        val editable = assertNotNull(block("""<!--#storyblok#{"name": "page", "id": "1", "uid": "u1"}-->""").editableBlock())

        assertNull(editable.space)
        assertEquals("1", editable.id)
    }

    @Test
    fun `ids the editor writes as numbers are read as it reads them`() {
        val editable = assertNotNull(
            block("""<!--#storyblok#{"name": "teaser", "space": 1001, "uid": "u1", "id": 42}-->""").editableBlock()
        )

        assertEquals("1001", editable.space)
        assertEquals("42", editable.id)
    }

    @Test
    fun `keys the client does not know are ignored`() {
        assertNotNull(
            block("""<!--#storyblok#{"name": "teaser", "uid": "u1", "id": "42", "hint": "new"}-->""").editableBlock()
        )
    }

    @Test
    fun `a published block names nothing`() {
        // The Content Delivery API only adds `_editable` to draft content.
        assertNull(block(null).editableBlock())
    }

    @Test
    fun `an editable that is not the editor's comment names nothing`() {
        assertNull(block("""{"name": "teaser", "uid": "u1", "id": "42"}""").editableBlock())
    }

    @Test
    fun `an editable whose comment is not JSON names nothing`() {
        assertNull(block("""<!--#storyblok#not json-->""").editableBlock())
    }

    @Test
    fun `an editable that leaves out the block names nothing`() {
        // Without the uid the editor has nothing to open.
        assertNull(block("""<!--#storyblok#{"name": "teaser", "id": "42"}-->""").editableBlock())
    }
}
