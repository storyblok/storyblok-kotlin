package com.storyblok.ktor

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlinx.coroutines.delay
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndUpdate
import kotlin.concurrent.atomics.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val LOGGER: Logger = KtorSimpleLogger("com.storyblok.ktor.Storyblok")

/**
 * Storyblok is a pre-configured HTTP client plugin for interfacing with the Storyblok Content Delivery API.
 *
 * This plugin provides the following features:
 * - HTTP caching configuration through `HttpCache`.
 * - Content negotiation with default JSON support using `ContentNegotiation`.
 * - Retry logic for requests with exponential backoff using `HttpRequestRetry`.
 * - Request configuration through `DefaultRequest` where URL, token, version, and additional parameters are automatically set.
 * - Handling of special cases like `MovedPermanently` responses and automatic updates to cached version (`cv`).
 *
 * The required configuration, such as `accessToken`, region, and optional settings like language and fallback language,
 * must be provided through the `StoryblokConfig` configuration object.
 *
 * Delays between retry attempts are managed based on HTTP status codes (`429` or `5xx`) and controlled through adjustable parameters.
 *
 * The plugin ensures request adherence to the API specifications while reducing the complexity of manual configuration.
 */
@OptIn(ExperimentalAtomicApi::class)
public fun <T: Api.Config> HttpClientConfig<*>.Storyblok(api: Api<T>): ClientPlugin<T> {

    lateinit var config: T

    val timeSource by lazy { config.timeSource }
    val minDelayBetweenRequests by lazy { 1.seconds / config.requestsPerSecond }
    val backoffUntil by lazy { AtomicReference(timeSource.markNow()) } // to share back off across all requests pre-flight

    expectSuccess = true

    install(ContentNegotiation) { json() }

    install(HttpRequestRetry) {
        noRetry() //clear default of retryOnExceptionOrServerErrors()
        retryIf(5) { _, response ->
            response.status == HttpStatusCode.TooManyRequests || response.status.value in 500..599
        }
        delay { delay ->
            //delay is applied in onRequest instead of here
            backoffUntil.update { maxOf(it + minDelayBetweenRequests, timeSource.markNow() + delay.milliseconds) }
        }
    }

    with(api) { configure { config } }

    return createClientPlugin("Storyblok", api.config) {
        config = this.pluginConfig
        with(api) { configure(config) }
        // on user request or retries/redirects
        on(SendingRequest) { request, _ ->
            val backoffUntil = backoffUntil.fetchAndUpdate { maxOf(it, timeSource.markNow()) + minDelayBetweenRequests }
            val delay = maxOf(Duration.ZERO, -backoffUntil.elapsedNow())
            LOGGER.debug("delaying $delay for ${request.url}")
            delay(delay)
        }
    }
}

