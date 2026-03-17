package com.storyblok.ktor

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpSendPipeline
import io.ktor.client.utils.CacheControl
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndUpdate
import kotlin.concurrent.atomics.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val LOGGER: Logger = KtorSimpleLogger("com.storyblok.ktor.Storyblok")

internal expect fun HttpClientConfig<*>.configureEngine()
/**
 * Invoke this function in your [HttpClient][io.ktor.client.HttpClient] configuration block to [install][HttpClientConfig.install] the Storyblok plugin.
 *
 * #### Example for the Content Delivery API
 * ```kotlin
 * val client = HttpClient {
 *   install(Storyblok(CDN)) {
 *     accessToken = "YOUR_ACCESS_TOKEN"
 *   }
 * }
 *  ```
 * #### Example for the Management API
 * ```kotlin
 * val client = HttpClient {
 *   install(Storyblok(MAPI)) {
 *     accessToken = OAuth("YOUR_OAUTH_TOKEN")
 *   }
 * }
 *  ```
 */
@OptIn(ExperimentalAtomicApi::class)
public fun <T: Api.Config> HttpClientConfig<*>.Storyblok(api: Api<T>): ClientPlugin<T> {
    configureEngine()

    var config: T? = null

    val timeSource by lazy { config!!.timeSource }
    val minDelayBetweenRequests by lazy { 1.seconds / config!!.requestsPerSecond }
    val backoffUntil by lazy { AtomicReference(timeSource.markNow()) } // to share back off across all requests pre-flight

    expectSuccess = true

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    // Installs retry logic with backoff for transient errors
    install(HttpRequestRetry) {
        retryOnExceptionIf { request, cause -> request.method == HttpMethod.Get && cause !is CancellationException }
        retryIf(5) { request, response ->
            (response.status == HttpStatusCode.TooManyRequests || response.status.value in 500..599)
                    && CacheControl.ONLY_IF_CACHED !in request.headers[HttpHeaders.CacheControl].orEmpty()
        }
        delay { delay ->
            //delay is applied in sendPipeline instead of here
            backoffUntil.update { maxOf(it + minDelayBetweenRequests, timeSource.markNow() + delay.milliseconds) }
        }
    }

    with(api) { configure { config } }

    return createClientPlugin("Storyblok", api.config) {
        config = this.pluginConfig
        with(api) { configure(config) }
        onRequest { request, _ ->
            when(request.method) {
                HttpMethod.Put, HttpMethod.Post -> request.contentType(ContentType.Application.Json)
            }
        }
        // Delays requests to respect rate limits
        client.sendPipeline.intercept(HttpSendPipeline.Monitoring) { _ ->
            val backoffUntil = backoffUntil.fetchAndUpdate { maxOf(it, timeSource.markNow()) + minDelayBetweenRequests }
            val delay = maxOf(Duration.ZERO, -backoffUntil.elapsedNow())
            LOGGER.debug("delaying $delay for ${context.url}")
            delay(delay)
        }
    }
}

