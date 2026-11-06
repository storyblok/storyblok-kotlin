@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.editorLanguage
import js.globals.globalThis
import js.objects.Object
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.Test
import kotlin.test.assertNull
import web.history.history
import web.window.window

/**
 * The web target's editor language, on a page the Visual Editor is not previewing.
 *
 * As with the bridge, Node covers a host with no `window`, and the browser one whose URL carries no
 * preview token.
 */
class EditorLanguageUrlTest {

    @Test
    fun `a page that is not an editor preview has no editor language`() {
        assertNull(editorLanguage())
    }

    @Test
    fun `a language in the URL of a page outside the editor is ignored`() {
        if (!Object.hasOwn(globalThis, "window")) return
        val url = window.location.href
        history.replaceState(null, "", "?_storyblok_lang=de")
        try {
            assertNull(editorLanguage())
        } finally {
            history.replaceState(null, "", url)
        }
    }
}
