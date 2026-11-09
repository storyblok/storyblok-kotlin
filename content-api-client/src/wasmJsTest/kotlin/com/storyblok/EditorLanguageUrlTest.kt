package com.storyblok

import com.storyblok.cdn.editorLanguage
import kotlin.test.Test
import kotlin.test.assertNull

/**
 * The web target's editor language on a page that is not an editor preview: in Node, a host with no
 * `window`, and in the browser, a frame with no preview token in its URL. The preview itself is
 * covered by [EditorPreviewUrlTest], which only the browser runs.
 */
class EditorLanguageUrlTest {

    @Test
    fun `a page that is not an editor preview has no editor language`() {
        assertNull(editorLanguage())
    }
}
