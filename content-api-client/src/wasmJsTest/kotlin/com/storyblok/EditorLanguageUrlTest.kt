@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.configureStoryblok
import com.storyblok.cdn.editorLanguage
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import js.globals.globalThis
import js.objects.Object
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import web.history.history
import web.window.window

/**
 * The web target's editor language, read from the page's own URL.
 *
 * As with the bridge, Node covers a host with no `window`. The browser runs tests inside a frame, as
 * the Visual Editor runs a preview, so the URL alone decides there; the top-level case is covered by
 * the common tests of `inVisualEditor`.
 */
class EditorLanguageUrlTest {

    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Test
    fun `a page that is not an editor preview has no editor language`() {
        assertNull(editorLanguage())
    }

    @Test
    fun `a language in the URL of a page outside the editor is ignored`() {
        withUrl("?_storyblok_lang=de") { assertNull(editorLanguage()) }
    }

    @Test
    fun `a framed editor preview has the language in its URL`() {
        withUrl("?_storyblok_tk[space_id]=313862&_storyblok_lang=de", framedOnly = true) {
            assertEquals("de", editorLanguage())
        }
    }

    @Test
    fun `a client in an editor preview asks for the language in its URL`() = runTest {
        var languages: List<String>? = null
        withUrl("?_storyblok_tk[space_id]=313862&_storyblok_lang=de", framedOnly = true) {
            HttpClient(MockEngine { request ->
                languages = request.url.parameters.getAll("language")
                respondOk()
            }) {
                configureStoryblok(Json) {
                    accessToken = "mock-api-key"
                    language = "en"
                }
            }
        }?.use { it.get("stories/home") } ?: return@runTest
        assertEquals(listOf("de"), languages)
    }

    /**
     * Runs [block] with the page's query string set to [query], restoring the URL after, or returns
     * `null` without running it where there is no `window` — or, when [framedOnly], no parent frame.
     */
    private fun <T> withUrl(query: String, framedOnly: Boolean = false, block: () -> T): T? {
        if (!Object.hasOwn(globalThis, "window")) return null
        if (framedOnly && window.parent == window) return null
        val url = window.location.href
        history.replaceState(null, "", query)
        try {
            return block()
        } finally {
            history.replaceState(null, "", url)
        }
    }
}
