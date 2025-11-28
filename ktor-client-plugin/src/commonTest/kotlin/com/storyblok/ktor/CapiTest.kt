package com.storyblok.ktor

import com.storyblok.ktor.Api.CDN
import com.storyblok.ktor.Api.Config.Region.Custom
import com.storyblok.ktor.Api.Config.Version.Draft
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpStatusCode.Companion.MovedPermanently
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.*

class CapiTest {

    @Test
    fun `request url is correctly formed from specified region, access token and uri`() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("localhost", request.url.host)
            assertEquals("/mock-base-url/cdn/stories/mock-slug", request.url.encodedPath)
            assertEquals("mock-api-key", request.url.parameters["token"])
            respondOk()
        }) {
            install(Storyblok(CDN)) {
                region = Custom("https://localhost/mock-base-url/cdn/")
                accessToken = "mock-api-key"
            }
        }
        client.get("stories/mock-slug")
    }

    @Test
    fun `default query parameters set when specified in config`() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals("mock-cv", request.url.parameters["cv"])
            assertEquals("draft", request.url.parameters["version"])
            assertEquals("mock-language", request.url.parameters["language"])
            assertEquals("mock-fallback-lang", request.url.parameters["fallback_lang"])
            respondOk()
        }) {
            install(Storyblok(CDN)) {
                accessToken = "mock-api-key"
                cv = "mock-cv"
                version = Draft
                language = "mock-language"
                fallbackLanguage = "mock-fallback-lang"
            }
        }
        client.get("stories/mock-slug")
    }

    @Test
    fun `requests per second defaults to 1000`() = runTest {
        HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "mock-api-key"
                assertEquals(1000, requestsPerSecond)
            }
        }
    }

    @Test
    fun `follows redirect and updates cv on 301 from cdn`() = runTest {
        val client = HttpClient(MockEngine.create {
            addHandler { request ->
                assertNull(request.url.parameters["cv"])
                respond("", MovedPermanently, headersOf(Location, "${request.url}&cv=mock-cv"))
            }
            repeat(2) {
                addHandler { request ->
                    assertEquals("mock-cv", request.url.parameters["cv"])
                    respondJson("""{"story": { "content": {}}}""")
                }
            }
        }) { install(Storyblok(CDN)) { accessToken = "mock-api-key" } }

        repeat(2) {
            assertContains(client.get("stories/mock-slug").body<JsonObject>(), "story")
        }
    }

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(content, headers = headersOf(HttpHeaders.ContentType, "${ContentType.Application.Json}"))

}
