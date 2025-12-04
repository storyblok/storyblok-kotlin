package com.storyblok.ktor

import com.storyblok.ktor.Api.Config.Content
import com.storyblok.ktor.Api.Config.Management
import com.storyblok.ktor.Api.Config.Region.EU
import com.storyblok.ktor.Api.Config.Version.Published
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.api.ClientPluginBuilder
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.takeFrom
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlin.time.TimeSource

private val LOGGER: Logger = KtorSimpleLogger("com.storyblok.ktor.Api")

/**
 * It is necessary to specify an [inheritor][Api] of `Api` when [installing][Storyblok] the plugin to configure the
 * [HttpClient][io.ktor.client.HttpClient] for either the [Content Delivery API][CDN] or the [Management API][MAPI]
 */
public sealed class Api<T : Api.Config>(internal val config: () -> T) {
    internal abstract fun HttpClientConfig<*>.configure(config: () -> T)
    internal open fun ClientPluginBuilder<*>.configure(config: T) {}

    /**
     * Configure the [HttpClient][io.ktor.client.HttpClient] for the [Management API](https://www.storyblok.com/docs/api/management)
     *
     * @see Storyblok
     */
    public object MAPI : Api<Management>(::Management) {
        override fun HttpClientConfig<*>.configure(config: () -> Management) {
            install(DefaultRequest) {
                with(config()) {
                    url.takeFrom(region.mapiUrl)
                    headers.append(HttpHeaders.Authorization, accessToken.value)
                }
            }
        }
    }

    /**
     * Configure the [HttpClient][io.ktor.client.HttpClient] for the [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2)
     *
     * @see Storyblok
     */
    public object CDN : Api<Content>(::Content) {

        override fun HttpClientConfig<*>.configure(config: () -> Content) {
            install(HttpCache) {
                isShared = true
            }
            install(DefaultRequest) {
                url {
                    with(config()) {
                        takeFrom(region.cdnUrl)
                        parameters.append("token", accessToken)
                        parameters.append("version", version.value)
                        cv?.run { parameters.append("cv", this) }
                        language?.run { parameters.append("language", this) }
                        fallbackLanguage?.run { parameters.append("fallback_lang", this) }
                    }
                }
            }
        }

        override fun ClientPluginBuilder<*>.configure(config: Content) {
            onResponse { response ->
                when(response.status) {
                    HttpStatusCode.MovedPermanently -> {
                        config.cv = Url(response.headers[HttpHeaders.Location].orEmpty()).parameters["cv"] ?: return@onResponse
                        LOGGER.info("cv updated to ${config.cv}")
                    }
                }
            }
        }
    }

    /**
     * It is necessary to specify an [inheritor][Config] of `Config` when [installing][Storyblok] the plugin to configure the
     * plugin for either the [Content Delivery API][Content] or the [Management API][Management]
     */
    public sealed class Config {
        /**
         * Optionally, specify the [region][Region] depending on the server location of your space.
         * Defaults to the [European Union][EU]
         */
        public var region: Region = EU
        /**
         * Optionally, specify the maximum number of API requests allowed per second.
         *
         * Defaults to 1000 requests per second for the [Content Delivery API][Content.requestsPerSecond] and 6 requests per second for the [Management API][Management.requestsPerSecond].
         *
         * You can lower the value if necessary to avoid exceeding the
         * [rate limits](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/rate-limit).
         */
        public abstract var requestsPerSecond: Int
        /** The time source used for request scheduling and delays. */
        internal var timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic

        /**
         * Optionally, specify an [inheritor][Region] of `Region` when [configuring][Api.Config.region]
         * the plugin depending on the server location of your space. Defaults to the [European Union][EU].
         *
         * Learn more in the [Content Delivery API Reference](https://www.storyblok.com/docs/api/content-delivery/v2)
         * or [Management API Reference](https://www.storyblok.com/docs/api/management).
         */
        public sealed class Region(internal val cdnUrl: Url, internal val mapiUrl: Url) {
            private constructor(cdn: String, mapi: String) : this(Url(cdn), Url(mapi))
            /** European Union API server location. */
            public object EU: Region("https://api.storyblok.com/v2/cdn/", "https://mapi.storyblok.com/v1/")
            /** United States API server location. */
            public object USA: Region("https://api-us.storyblok.com/v2/cdn/","https://api-us.storyblok.com/")
            /** Canada API server location. */
            public object CAN: Region("https://api-ca.storyblok.com/v2/cdn/", "https://api-ca.storyblok.com/")
            /** Australia API server location. */
            public object AUS: Region("https://api-ap.storyblok.com/v2/cdn/","https://api-ap.storyblok.com/")
            /** China API server location. */
            public object CHN: Region("https://app.storyblokchina.cn/", "https://app.storyblokchina.cn/")
            /** A custom API server location you specify.
             * @param url The base URL of the custom API server
             * */
            public class Custom(url: String): Region(url, url)
        }

        /**
         * Optionally, specify an [inheritor][Version] of `Version` when [configuring][Api.Config.Content.version]
         * the plugin for the [Content Delivery API][Api.CDN] that will affect all requests. Defaults to [Published].
         *
         * Resources have two potential versions: draft (unpublished) or published.
         */
        public enum class Version(internal val value: String) {
            /** The draft (unpublished) version of your resource. */
            Draft("draft"),
            /** This published (live) version of your resource. */
            Published("published")
        }

        /**
         * Configure the plugin for the [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2).
         *
         * @see Storyblok
         */
        public class Content internal constructor(): Config() {

            /** It is necessary to specify an API access token to [authenticate requests to the Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/authentication). */
            public lateinit var accessToken: String
            /**
             * Optionally, specify the default language to retrieve resources.
             *
             * Accepts any language code configured in the Storyblok space.
             *
             * Note that the language code needs to be provided with underscores, even if it is defined with hyphens. E.g., es_co instead of es-co.
             * */
            public var language: String? = null
            /**
             * Optionally, specify the default fallback language to handle untranslated fields.
             *
             * Accepts any language code configured in the Storyblok space.
             *
             * Note that the language code needs to be provided with underscores, even if it is defined with hyphens. E.g., es_co instead of es-co.
             * */
            public var fallbackLanguage: String? = null
            /**
             * Optionally, specify the [version][Version] to retrieve all resources. Defaults to [Published].
             */
            public var version: Version = Published
            /** Optionally, specify the cached version Unix timestamp (see [Cache Invalidation](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/cache-invalidation)).
             *
             * Note this is set automatically, set this property manually if you want to retrieve a specific cached version of a resource.
             *
             * Learn more in [How stories are cached in the Content Delivery API](https://www.storyblok.com/faq/how-stories-are-cached-content-delivery-api#how-the-js-client-uses-the-cv-param).
             * */
            public var cv: String? = null

            /**
             * Optionally, specify the maximum number of API requests allowed per second, defaults to 1000.
             */
            override var requestsPerSecond: Int = 1000
        }

        /**
         * Configure the plugin for the [Management API](https://www.storyblok.com/docs/api/management).
         *
         * @see Storyblok
         */
        public class Management internal constructor() : Config() {
            /** It is necessary to specify a [personal][AccessToken.Personal] or [OAuth][AccessToken.OAuth] access token to
             * [authenticate requests to the Management API](https://www.storyblok.com/docs/api/management/getting-started/authentication).
             * */
            public lateinit var accessToken: AccessToken
            /**
             * Optionally, specify the maximum number of API requests allowed per second, defaults to 6.
             */
            override var requestsPerSecond: Int = 6
            /**
             * It is necessary to specify an [inheritor][AccessToken] of `AccessToken` when [configuring][Management.accessToken] the plugin to
             * [authenticate requests to the Management API](https://www.storyblok.com/docs/api/management/getting-started/authentication).             */
            public sealed class AccessToken private constructor(internal val value: String) {
                /**
                 * An OAuth Access Token is obtained via the OAuth2 authentication flow and is tied to a single space.
                 */
                public class OAuth(token: String) : AccessToken("Bearer ${token.removePrefix("Bearer ")}")

                /**
                 * A Personal Access Token is obtained from the Storyblok UI and grants access to all spaces associated with your account
                 */
                public class Personal(token: String) : AccessToken(token)
            }
        }
    }
}
