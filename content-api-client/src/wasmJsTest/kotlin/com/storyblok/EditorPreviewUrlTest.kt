package com.storyblok

import com.storyblok.cdn.EditorLanguage
import com.storyblok.cdn.configureStoryblok
import com.storyblok.cdn.editorLanguage
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import web.history.history
import web.window.window

/**
 * The web target's editor language, read from the URL of a page in a frame, as the Visual Editor
 * frames a preview.
 *
 * The browser runs tests inside a frame, so the URL alone decides here; the top-level case is covered
 * by the common tests of `inVisualEditor`. Node has no page to read, so the build leaves this class
 * out of its run rather than letting it pass without checking anything.
 */
class EditorPreviewUrlTest {

    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Test
    fun `a language in the URL of a page outside the editor is ignored`() = withUrl("?_storyblok_lang=de") {
        assertNull(editorLanguage())
    }

    @Test
    fun `a framed editor preview has the language in its URL`() = withUrl(PREVIEW + "&_storyblok_lang=de") {
        assertEquals(EditorLanguage("de"), editorLanguage())
    }

    @Test
    fun `a framed editor preview of the default language has no language code`() =
        withUrl(PREVIEW + "&_storyblok_lang=default") {
            assertEquals(EditorLanguage(null), editorLanguage())
        }

    @Test
    fun `a client in an editor preview asks for the language in its URL`() = runTest {
        var languages: List<String>? = null
        withUrl(PREVIEW + "&_storyblok_lang=de") {
            HttpClient(MockEngine { request ->
                languages = request.url.parameters.getAll("language")
                respondOk()
            }) {
                configureStoryblok(Json) {
                    accessToken = "mock-api-key"
                    language = "en"
                }
            }
        }.use { it.get("stories/home") }
        assertEquals(listOf("de"), languages)
    }

    /** Runs [block] with the page's query string set to [query], restoring the URL after. */
    private fun <T> withUrl(query: String, block: () -> T): T {
        // A top-level window is its own parent.
        assertNotEquals(window.parent, window, "Expected the test page to be framed, as Karma frames it")
        val url = window.location.href
        history.replaceState(null, "", query)
        try {
            return block()
        } finally {
            history.replaceState(null, "", url)
        }
    }

    private companion object {
        const val PREVIEW = "?_storyblok_tk[space_id]=313862"
    }
}
