@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.EditableBlock
import com.storyblok.cdn.edit
import js.objects.Object
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.get
import kotlin.js.length
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/** The message that opens a block's form in the Visual Editor. */
class EditMessageTest {

    private val teaser = EditableBlock(name = "teaser", space = "1001", uid = "u1", id = "42")

    @Test
    fun `the message is the edit action`() {
        assertEquals("edit", edit(teaser, "").action)
    }

    @Test
    fun `the message names the block as its editable does`() {
        val block = edit(teaser, "").dataC

        assertEquals(
            listOf("teaser", "1001", "u1", "42"),
            listOf(block.name, block.space, block.uid, block.id),
        )
    }

    @Test
    fun `a block without a space is sent without one`() {
        val block = edit(EditableBlock(name = "teaser", uid = "u1", id = "42"), "").dataC

        assertFalse(Object.hasOwn(block, "space"))
    }

    @Test
    fun `the message repeats the relations the handshake asked for`() {
        val relations = edit(teaser, "feed.posts,post.author").config.resolveRelations!!

        assertEquals(
            listOf("feed.posts", "post.author"),
            (0 until relations.length).map { relations[it].toString() },
        )
    }

    @Test
    fun `a client with no relations repeats none`() {
        assertNull(edit(teaser, "").config.resolveRelations)
    }
}
