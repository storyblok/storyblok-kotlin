package com.storyblok

import com.storyblok.cdn.EditorLanguage
import com.storyblok.cdn.FallbackLanguage
import com.storyblok.cdn.configureStoryblok
import com.storyblok.cdn.editorLanguage
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.inVisualEditor
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Following the language the author picks in the Visual Editor. */
class EditorLanguageTest {

    // FileStorage creates its directory as soon as a client is constructed, so without this the
    // tests would leave directories in the developer's real cache directory.
    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Test
    fun `the language in a preview URL is the editor's language`() {
        val query = "?_storyblok=580906535&_storyblok_c=page&_storyblok_lang=de&_storyblok_tk[space_id]=313862"
        assertEquals("de", editorLanguage(query).code)
    }

    @Test
    fun `a query string without its question mark is read the same`() {
        assertEquals("de", editorLanguage("_storyblok_lang=de").code)
    }

    @Test
    fun `the editor names its default language default`() {
        assertNull(editorLanguage("?_storyblok_lang=default").code)
        assertNull(editorLanguage("?_storyblok_lang=Default").code)
    }

    @Test
    fun `a preview URL without a language shows the default language`() {
        assertNull(editorLanguage("").code)
        assertNull(editorLanguage("?_storyblok=580906535").code)
        assertNull(editorLanguage("?_storyblok_lang=").code)
    }

    @Test
    fun `a hyphenated language code is read as the space configures it`() {
        assertEquals("es-co", editorLanguage("?_storyblok_lang=es-co").code)
        assertEquals("pt-br", editorLanguage("?_storyblok_lang=pt%2Dbr").code)
    }

    @Test
    fun `a framed page with a preview token is in the editor`() {
        assertTrue(inVisualEditor("?_storyblok_tk[space_id]=313862&_storyblok_lang=de", framed = true))
    }

    @Test
    fun `a top-level page is not in the editor whatever its URL says`() {
        assertFalse(inVisualEditor("?_storyblok_tk[space_id]=313862&_storyblok_lang=de", framed = false))
    }

    @Test
    fun `a framed page without a preview token is not in the editor`() {
        assertFalse(inVisualEditor("?_storyblok=580906535&_storyblok_lang=de", framed = true))
    }

    @Test
    fun `a page outside the editor has no editor language`() {
        assertNull(editorLanguage())
    }

    @Test
    fun `requests ask for the editor's language in place of the app's`() = runTest {
        assertEquals(listOf("de"), languagesRequested(EditorLanguage("de"), appLanguage = "en"))
    }

    @Test
    fun `requests ask for the editor's language when the app sets none`() = runTest {
        assertEquals(listOf("de"), languagesRequested(EditorLanguage("de"), appLanguage = null))
    }

    @Test
    fun `requests ask for no language in an editor showing the default language`() = runTest {
        assertNull(languagesRequested(EditorLanguage(null), appLanguage = "en"))
    }

    @Test
    fun `requests keep the app's language outside the editor`() = runTest {
        assertEquals(listOf("en"), languagesRequested(editorLanguage = null, appLanguage = "en"))
    }

    @Test
    fun `a configured client records its fallback language for the editor`() {
        HttpClient(MockEngine { respondOk() }) {
            configureStoryblok(Json, editorLanguage = null) {
                accessToken = "mock-api-key"
                fallbackLanguage = "en"
            }
        }.use { assertEquals("en", it.attributes.getOrNull(FallbackLanguage)) }
    }

    @Test
    fun `a client without a fallback language records none`() {
        HttpClient(MockEngine { respondOk() }) {
            configureStoryblok(Json, editorLanguage = null) { accessToken = "mock-api-key" }
        }.use { assertNull(it.attributes.getOrNull(FallbackLanguage)) }
    }

    /** The `language` parameters a story request carries from a client configured with these languages. */
    private suspend fun languagesRequested(editorLanguage: EditorLanguage?, appLanguage: String?): List<String>? {
        var languages: List<String>? = null
        HttpClient(MockEngine { request ->
            languages = request.url.parameters.getAll("language")
            respondOk()
        }) {
            configureStoryblok(Json, editorLanguage) {
                accessToken = "mock-api-key"
                language = appLanguage
            }
        }.use { it.get("stories/home") }
        return languages
    }
}
