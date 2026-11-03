@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.initialized
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.get
import kotlin.js.length
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The handshake the Visual Editor is sent. */
class BridgeParamsTest {

    @Test
    fun `the handshake is the initialized action`() {
        assertEquals("initialized", initialized("post.author").action)
    }

    @Test
    fun `the editor is asked to resolve the relations the request asks for`() {
        val relations = initialized("feed.posts,post.author").config.resolveRelations!!

        assertEquals(
            listOf("feed.posts", "post.author"),
            (0 until relations.length).map { relations[it].toString() },
        )
    }

    @Test
    fun `a client with no relations asks the editor for none`() {
        // Not an empty array: this is what the editor is sent as the preview initializes.
        assertNull(initialized("").config.resolveRelations)
    }
}
