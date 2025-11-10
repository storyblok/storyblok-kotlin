package com.storyblok.ktor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Location
import io.ktor.http.HttpStatusCode.Companion.MovedPermanently
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.testTimeSource
import kotlinx.serialization.json.JsonObject
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class StoryblokTest {

    @Test
    fun `request url is correctly formed from specified region, access token and uri`() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("localhost", request.url.host)
            assertEquals("/mock-base-url/cdn/stories/mock-slug", request.url.encodedPath)
            assertEquals("mock-api-key", request.url.parameters["token"])
            respondOk()
        }) {
            install(Storyblok) {
                region = Region.Custom("https://localhost/mock-base-url/")
                accessToken = "mock-api-key"
            }
        }
        client.get("cdn/stories/mock-slug")
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
            install(Storyblok) {
                accessToken = "mock-api-key"
                cv = "mock-cv"
                version = Version.Draft
                language = "mock-language"
                fallbackLanguage = "mock-fallback-lang"
            }
        }
        client.get("cdn/stories/mock-slug")
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
        }) { install(Storyblok) { accessToken = "mock-api-key" } }

        repeat(2) {
            assertContains(client.get("cdn/stories/mock-slug").body<JsonObject>(), "story")
        }
    }

    @Test
    fun `throws on client error status codes`() = runTest {
        val client = HttpClient(MockEngine { respondBadRequest() }) {
            install(Storyblok) { accessToken = "mock-api-key" }
        }
        assertFailsWith<ClientRequestException> { client.get("cdn/stories/mock-slug") }
    }

    @Test
    fun `retries up to 5 times on server error or 429 (too many requests) status codes`() = runTest {
        fun clientThatEventually(sixthResponse: MockRequestHandleScope.() -> HttpResponseData) =
            HttpClient(MockEngine.create {
                addHandler { respondError(HttpStatusCode.InternalServerError) }
                addHandler { respondError(HttpStatusCode.BadGateway) }
                addHandler { respondError(HttpStatusCode.ServiceUnavailable) }
                addHandler { respondError(HttpStatusCode.GatewayTimeout) }
                addHandler { respondError(HttpStatusCode.TooManyRequests) }
                addHandler { sixthResponse() }
            }) { install(Storyblok) { accessToken = "mock-api-key" } }
        //successful retry
        with(clientThatEventually { respondJson("""{"story": { "content": {}}}""") }) {
            assertContains(get("cdn/stories/mock-slug").body<JsonObject>(), "story")
        }
        //client error
        with(clientThatEventually { respondError(HttpStatusCode.TooManyRequests) }) {
            assertFailsWith<ClientRequestException> { get("cdn/stories/mock-slug") }
        }
        //server error
        with(clientThatEventually { respondError(HttpStatusCode.InternalServerError) }) {
            assertFailsWith<ServerResponseException> { get("cdn/stories/mock-slug") }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `requests are throttled according to the specified value in requestsPerSecond`() = runTest {
        val client = HttpClient(MockEngine { respondOk() }) {
            install(Storyblok) {
                accessToken = "mock-api-key"
                requestsPerSecond = 1
                timeSource = testTimeSource
            }
        }
        client.get("cdn/stories/mock-slug")
        val timeAfterInitialRequest = testScheduler.currentTime.milliseconds
        client.get("cdn/stories/mock-slug")
        val timeAfterSubsequentRequest = testScheduler.currentTime.milliseconds
        assertEquals(1.seconds, timeAfterSubsequentRequest - timeAfterInitialRequest)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when retying a request other requests made during the delay are also subject to the delay`() = runTest {
        val engine = MockEngine.create {
            dispatcher = StandardTestDispatcher(testScheduler)
            reuseHandlers = false
            addHandler { respondError(HttpStatusCode.TooManyRequests) }
            addHandler { respondError(HttpStatusCode.TooManyRequests) }
            addHandler { respondOk() }
            addHandler { respondOk() }
        }
        val client = HttpClient(engine) {
            install(HttpRequestRetry) {
                constantDelay(5.seconds.inWholeMilliseconds, 0)
            }
            install(Storyblok) {
                accessToken = "mock-api-key"
                requestsPerSecond = 1
                timeSource = testTimeSource
            }
        }
        joinAll(
            launch { client.get("cdn/stories/first-mock-slug") },
            launch { client.get("cdn/stories/second-mock-slug") }
        )

        val firstRequestDelay = 0.seconds // no delay
        val secondRequestDelay = 1.seconds // delay respecting requestsPerSecond
        val firstRetryDelay = 5.seconds // HttpRequestRetry constant delay on 429
        val secondRetryDelay = 5.seconds + 1.seconds // HttpRequestRetry constant delay on 429 + delay respecting requestsPerSecond

        assertEquals(
            maxOf(firstRequestDelay, secondRequestDelay) + maxOf(firstRetryDelay, secondRetryDelay),
            testScheduler.currentTime.milliseconds
        )
    }

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(content, headers = headersOf(HttpHeaders.ContentType, "${ContentType.Application.Json}"))

}

//suspend fun main() {
//
//    val client = HttpClient(CIO) {
//        install(Logging) {
//            level = LogLevel.ALL
//            logger = object : Logger { override fun log(message: String) { println(message) } }
//        }
//        install(Storyblok) {
//            accessToken = "dpG18erLZFJTbIR5sciKFAtt" //required
//            baseUrl = "https://api.storyblok.com/v2/" // optional
//            version = Version.Draft // optional, defaults to published
//            cv = "1735815318" // optional, set when you make your first API call to the /stories/ endpoint
//            language = "de" // optional
//            fallbackLanguage = "en" // optional
//        }
//    }
//
//    val response = client.get("cdn/stories/hey-rick")
//    println(response.body<JsonObject>())
//
//}
