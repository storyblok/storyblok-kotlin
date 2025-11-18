package com.storyblok.ktor

import com.storyblok.ktor.Api.Config.Mapi.AccessToken
import com.storyblok.ktor.Api.MAPI
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpHeaders.Authorization
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class MapiTest {

    @Test
    fun `specifying a personal access token is added as auth header`() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("mapi.storyblok.com", request.url.host)
            assertEquals("/v1/spaces/606/stories/369689", request.url.encodedPath)
            assertEquals("mock-api-key", request.headers[Authorization])
            respondOk()
        }) {
            install(Storyblok(MAPI)) {
                region = Region.EU
                accessToken = AccessToken.Personal("mock-api-key")
            }
        }
        client.get("spaces/606/stories/369689")
    }

    @Test
    fun `specifying a oauth access token is added as auth header with bearer prefix`() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals(URLProtocol.HTTPS, request.url.protocol)
            assertEquals("mapi.storyblok.com", request.url.host)
            assertEquals("/v1/spaces/606/stories/369689", request.url.encodedPath)
            assertEquals("Bearer mock-api-key", request.headers[Authorization])
            respondOk()
        }) {
            install(Storyblok(MAPI)) {
                region = Region.EU
                accessToken = AccessToken.OAuth("mock-api-key")
            }
        }
        client.get("spaces/606/stories/369689")
    }

    @Test
    fun `requests per second defaults to 6`() = runTest {
        HttpClient {
            install(Storyblok(MAPI)) {
                region = Region.EU
                accessToken = AccessToken.OAuth("mock-api-key")
                assertEquals(6, requestsPerSecond)
            }
        }
    }
}