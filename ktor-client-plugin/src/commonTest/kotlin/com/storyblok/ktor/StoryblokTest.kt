package com.storyblok.ktor

import com.storyblok.ktor.Api.CDN
import com.storyblok.ktor.Api.MAPI
import com.storyblok.ktor.Api.Config.Management.AccessToken
import com.storyblok.ktor.Api.Config.Management.AccessToken.OAuth
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders.ContentType
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
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
    fun `throws on client error status codes`() = runTest {
        val client = HttpClient(MockEngine { respondBadRequest() }) {
            install(Storyblok(CDN)) { accessToken = "mock-api-key" }
        }
        assertFailsWith<ClientRequestException> { client.get("stories/mock-slug") }
    }

    @Test
    fun `adds json content type header on put and post requests`() = runTest {
        val client = HttpClient(MockEngine.create {
            addHandler {
                when(it.method) {
                    Put, Post -> assertEquals(Json.toString(), it.headers[ContentType])
                    else -> assertNull(it.headers[ContentType])
                }
                respondOk()
            }
        }) {
            install(Storyblok(MAPI)) { accessToken = OAuth("mock-api-key") }
        }
        client.get("spaces/123/stories/1234")
        client.post("spaces/123/stories/1234")
        client.put("spaces/123/stories/1234")
        client.delete("spaces/123/stories/1234")
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
            }) { install(Storyblok(CDN)) { accessToken = "mock-api-key" } }
        //successful retry
        with(clientThatEventually { respondJson("""{"story": { "content": {}}}""") }) {
            assertContains(get("stories/mock-slug").body<JsonObject>(), "story")
        }
        //client error
        with(clientThatEventually { respondError(HttpStatusCode.TooManyRequests) }) {
            assertFailsWith<ClientRequestException> { get("stories/mock-slug") }
        }
        //server error
        with(clientThatEventually { respondError(HttpStatusCode.InternalServerError) }) {
            assertFailsWith<ServerResponseException> { get("stories/mock-slug") }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `requests are throttled according to the specified value in requestsPerSecond`() = runTest {
        val client = HttpClient(MockEngine { respondOk() }) {
            install(Storyblok(MAPI)) {
                timeSource = testTimeSource
                accessToken = AccessToken.Personal("mock-api-key")
                requestsPerSecond = 1
            }
        }
        client.get("stories/mock-slug")
        val timeAfterInitialRequest = testScheduler.currentTime.milliseconds
        client.get("stories/mock-slug")
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("mock-api-key")
                requestsPerSecond = 1
                timeSource = testTimeSource
            }
        }
        joinAll(
            launch { client.get("stories/first-mock-slug") },
            launch { client.get("stories/second-mock-slug") }
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
        respond(content, headers = headersOf(ContentType, "${Json}"))

}
