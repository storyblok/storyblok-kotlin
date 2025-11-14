package com.storyblok.ktor

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.cache.HttpCache
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
import kotlin.time.TimeSource

private val LOGGER: Logger = KtorSimpleLogger("com.storyblok.ktor.Storyblok")

/**
 * The API server location, the base URL for the Content Delivery API depends on the server location of the space.
 *
 * Learn more in the [Content Delivery API Reference](https://www.storyblok.com/docs/api/content-delivery/v2)
 */
public sealed class Region(internal val url: Url) {
    private constructor(url: String) : this(Url(url))
    /** European Union API server location. */
    public object EU : Region("https://api.storyblok.com/v2/")
    /** United States API server location. */
    public object USA: Region("https://api-us.storyblok.com/v2/")
    /** Canada API server location. */
    public object CAN: Region("https://api-ca.storyblok.com/v2/")
    /** Australia API server location. */
    public object AUS: Region("https://api-ap.storyblok.com/v2/")
    /** China API server location. */
    public object CHN: Region("https://app.storyblokchina.cn")
    /** Custom API server location
     * @param url the custom base URL
     * */
    public class Custom(url: String): Region(url)
}

/**
 * A resource's version determines who can access it.
 *
 * Resources have two potential versions: draft (unpublished) or published.
 */
public enum class Version(internal val value: String) {
    /** This resource will only appear in preview versions of your website. */
    Draft("draft"),
    /** This resource will appear live on your website. */
    Published("published")
}

public class StoryblokConfig {
    /** API requests must be authenticated by providing an API access token. */
    public lateinit var accessToken: String
    /** The API server location. */
    public var region: Region = Region.EU
    /**
     * Default language to retrieve resources.
     *
     * Accepts any language code configured in the Storyblok space.
     *
     * Note that the language code needs to be provided with underscores, even if it is defined with hyphens. E.g., es_co instead of es-co.
     * */
    public var language: String? = null
    /**
     * Default fallback language to handle untranslated fields.
     *
     * Accepts any language code configured in the Storyblok space.
     *
     * Note that the language code needs to be provided with underscores, even if it is defined with hyphens. E.g., es_co instead of es-co.
     * */
    public var fallbackLanguage: String? = null
    /**
     * Default version to retrieve resources.
     * */
    public var version: Version = Version.Published

    /** Cached version Unix timestamp (see [Cache Invalidation](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/cache-invalidation))
     *
     * Note this is set automatically, set this property manually if you want to override this behavior.
     *
     * Learn more in [How stories are cached in the Content Delivery API](https://www.storyblok.com/faq/how-stories-are-cached-content-delivery-api#how-the-js-client-uses-the-cv-param)
     * */
    public var cv: String? = null

    /**
     * Sets the maximum number of API requests allowed per second.
     *
     * You can lower the value if necessary to avoid exceeding the
     * [rate limits](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/rate-limit).
     */
    public var requestsPerSecond: Int = 1000

    internal var timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic
}

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
public val HttpClientConfig<*>.Storyblok: ClientPlugin<StoryblokConfig> get() {

    lateinit var config: StoryblokConfig

    val timeSource by lazy { config.timeSource }
    val minDelayBetweenRequests by lazy { 1.seconds / config.requestsPerSecond }
    val backoffUntil by lazy { AtomicReference(timeSource.markNow()) } // to share back off across all requests pre-flight

    expectSuccess = true
    install(HttpCache)
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
    install(DefaultRequest) {
        url {
            takeFrom(config.region.url)
            with(parameters) {
                append("token", config.accessToken)
                append("version", config.version.value)
                config.cv?.run { append("cv", this) }
                config.language?.run { append("language", this) }
                config.fallbackLanguage?.run { append("fallback_lang", this) }
            }
        }
    }
    return createClientPlugin("Storyblok", ::StoryblokConfig) {

        config = this.pluginConfig

        //on each user request
        onRequest { request, _ -> }

        // on user request or retries/redirects
        on(SendingRequest) { request, _ ->
            val backoffUntil = backoffUntil.fetchAndUpdate { maxOf(it, timeSource.markNow()) + minDelayBetweenRequests }
            val delay = maxOf(Duration.ZERO, -backoffUntil.elapsedNow())
            LOGGER.debug("delaying {} for {}", delay, request.url)
            delay(delay)
        }

        onResponse { response ->
            when(response.status) {
                HttpStatusCode.MovedPermanently -> {
                    this@createClientPlugin.pluginConfig.cv =
                        Url(response.headers[HttpHeaders.Location].orEmpty()).parameters["cv"] ?: return@onResponse
                    LOGGER.info("cv updated to {}", this@createClientPlugin.pluginConfig.cv)
                }
            }
        }
    }
}

