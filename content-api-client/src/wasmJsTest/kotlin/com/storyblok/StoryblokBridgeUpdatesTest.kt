package com.storyblok

import com.storyblok.cdn.NoVisualEditor
import com.storyblok.cdn.StoryblokBridge
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertSame

/**
 * The web target's preview bridge, on a page the Visual Editor is not previewing.
 *
 * This runs in both wasm test environments and each covers one of the two guards: Node has no
 * `window` at all, while the browser has one whose query string carries no preview token.
 */
class StoryblokBridgeUpdatesTest {

    @Test
    fun `a page that is not an editor preview connects to no editor`() {
        assertSame(NoVisualEditor, StoryblokBridge(Json, resolveRelations = ""))
    }
}
