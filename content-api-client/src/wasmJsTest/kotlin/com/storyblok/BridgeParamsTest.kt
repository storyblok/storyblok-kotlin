@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.bridgeParams
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.get
import kotlin.js.length
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** What the preview bridge is built with. */
class BridgeParamsTest {

    @Test
    fun `a bridge built while another is on the page still initializes`() {
        // The package skips init entirely when this is left on and a bridge is already present,
        // which costs the second one its message listener while leaving `on` looking like it worked.
        // A client is keyed on language and cache version, so a second bridge is an ordinary event.
        assertEquals(false, bridgeParams("post.author").initOnlyOnce)
    }

    @Test
    fun `the editor is asked to resolve the relations the request asks for`() {
        val relations = bridgeParams("feed.posts,post.author").resolveRelations!!

        assertEquals(
            listOf("feed.posts", "post.author"),
            (0 until relations.length).map { relations[it].toString() },
        )
    }

    @Test
    fun `a client with no relations asks the editor for none`() {
        // Not an empty array: the bridge forwards this to the editor as it initializes.
        assertNull(bridgeParams("").resolveRelations)
    }
}
