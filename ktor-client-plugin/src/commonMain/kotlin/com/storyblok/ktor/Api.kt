package com.storyblok.ktor

import com.storyblok.ktor.Api.Config.Cdn
import com.storyblok.ktor.Api.Config.Mapi
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

public sealed class Api<T : Api.Config>(internal val config: () -> T) {
    internal abstract fun HttpClientConfig<*>.configure(config: () -> T)
    internal open fun ClientPluginBuilder<*>.configure(config: T) {}

    public object MAPI : Api<Mapi>(::Mapi) {
        override fun HttpClientConfig<*>.configure(config: () -> Mapi) {
            install(DefaultRequest) {
                with(config()) {
                    url.takeFrom(region.mapiUrl)
                    headers.append(HttpHeaders.Authorization, accessToken.value)
                }
            }
        }
    }
    public object CDN : Api<Cdn>(::Cdn) {

        override fun HttpClientConfig<*>.configure(config: () -> Cdn) {
            install(HttpCache)
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

        override fun ClientPluginBuilder<*>.configure(config: Config.Cdn) {
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

    public sealed class Config {
        /** The API server location. */
        public var region: Region = Region.EU
        /**
         * Sets the maximum number of API requests allowed per second.
         *
         * You can lower the value if necessary to avoid exceeding the
         * [rate limits](https://www.storyblok.com/docs/api/content-delivery/v2/getting-started/rate-limit).
         */
        public abstract var requestsPerSecond: Int
        /** The time source used for request scheduling and delays. */
        internal var timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic

        public class Cdn public constructor(): Config() {

            /** API requests must be authenticated by providing an API access token. */
            public lateinit var accessToken: String
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
            override var requestsPerSecond: Int = 1000
        }

        public class Mapi : Config() {
            /** API requests must be authenticated by providing an API access token. */
            public lateinit var accessToken: AccessToken
            override var requestsPerSecond: Int = 6
            public sealed class AccessToken private constructor(internal val value: String) {
                public class OAuth(token: String) : AccessToken("Bearer ${token.removePrefix("Bearer ")}")
                public class Personal(token: String) : AccessToken(token)
            }
        }
    }
}
