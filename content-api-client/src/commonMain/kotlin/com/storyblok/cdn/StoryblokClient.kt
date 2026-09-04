@file:Suppress("UNCHECKED_CAST")

package com.storyblok.cdn

import com.storyblok.cdn.query.StoriesQuery
import com.storyblok.cdn.query.StoryQuery
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.Config.Version
import io.ktor.client.HttpClient
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.uuid.Uuid

/**
 * Exception thrown on errors occurring during [StoryblokClient] operations.
 *
 * @param message The error message, typically from the API response.
 * @param cause The underlying exception, if any.
 */
public open class StoryblokClientException(message: String?, cause: Throwable?) : Exception(message, cause) {
    public constructor(message: String?) : this(message, null)
    public constructor(cause: Throwable?) : this(null, cause)
}

/**
 * Retrieves a [Story] by its slug using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the story content.
 * @param slug The URL path segment identifying the story.
 * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct relations;
 * higher values resolve relations of relations; `0` disables relation resolution entirely — model relation fields as
 * [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level (including circular
 * relations) resolve to `null` for nullable story fields, and fail with a [SerializationException] naming the
 * uuid for non-nullable ones.
 * @param query Configures the query parameters via the [StoryQuery] DSL.
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(
    slug: String,
    resolveLevel: Int = 1,
    noinline query: StoryQuery<T>.() -> Unit = {},
): Flow<Story<T>> = story(slug, typeInfo<Story<T>>(), resolveLevel, query)

/**
 * Retrieves a [Story] by its UUID using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the story content.
 * @param uuid The unique identifier of the story.
 * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct relations;
 * higher values resolve relations of relations; `0` disables relation resolution entirely — model relation fields as
 * [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level (including circular
 * relations) resolve to `null` for nullable story fields, and fail with a [SerializationException] naming the
 * uuid for non-nullable ones.
 * @param query Configures the query parameters via the [StoryQuery] DSL.
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(
    uuid: Uuid,
    resolveLevel: Int = 1,
    noinline query: StoryQuery<T>.() -> Unit = {},
): Flow<Story<T>> = story(uuid, typeInfo<Story<T>>(), resolveLevel, query)

/**
 * Retrieves multiple [stories][Story] as a [PagingData] stream, using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the stories' content.
 * @param config The Paging [configuration][PagingConfig]; its [pageSize][PagingConfig.pageSize] maps to the API's `per_page`.
 * @param resolveLevel How deeply nested [Story] relations are resolved, see [story].
 * @param query Configures the query parameters via the [StoriesQuery] DSL.
 * @return A [Pager] whose [flow][Pager.flow] emits the stories, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.stories(
    config: PagingConfig = PagingConfig(pageSize = 25),
    resolveLevel: Int = 1,
    noinline query: StoriesQuery<T>.() -> Unit = {},
): Pager<Int, Story<T>> = stories(config, typeInfo<Story<T>>(), resolveLevel, query)

/**
 * Client for the Storyblok [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2).
 *
 * Provides type-safe access to stories with automatic JSON deserialization and relation resolution.
 */
public interface StoryblokClient {

    /** The underlying Ktor HTTP client. */
    public val http: HttpClient

    /** Closes the underlying HTTP client and releases resources. */
    public fun close()

    /**
     * Retrieves a [Story] by its slug.
     *
     * @param slug The URL path segment identifying the story.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [SerializationException] naming the uuid for non-nullable ones.
     * @param query Configures the query parameters via the [StoryQuery] DSL.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(
        slug: String,
        resolveLevel: Int = 1,
        query: StoryQuery<Component>.() -> Unit = {},
    ): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its UUID.
     *
     * @param uuid The unique identifier of the story.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [SerializationException] naming the uuid for non-nullable ones.
     * @param query Configures the query parameters via the [StoryQuery] DSL.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(
        uuid: Uuid,
        resolveLevel: Int = 1,
        query: StoryQuery<Component>.() -> Unit = {},
    ): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its slug with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param slug The URL path segment identifying the story.
     * @param typeInfo Type information for deserialization.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [SerializationException] naming the uuid for non-nullable ones.
     * @param query Configures the query parameters via the [StoryQuery] DSL.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(
        slug: String,
        typeInfo: TypeInfo,
        resolveLevel: Int = 1,
        query: StoryQuery<T>.() -> Unit = {},
    ): Flow<Story<T>>

    /**
     * Retrieves a [Story] by its UUID with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param uuid The unique identifier of the story.
     * @param typeInfo Type information for deserialization.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [Uuid] (or [String]) to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [SerializationException] naming the uuid for non-nullable ones.
     * @param query Configures the query parameters via the [StoryQuery] DSL.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(
        uuid: Uuid,
        typeInfo: TypeInfo,
        resolveLevel: Int = 1,
        query: StoryQuery<T>.() -> Unit = {},
    ): Flow<Story<T>>

    /**
     * Retrieves multiple [stories][Story] as a [PagingData] stream.
     *
     * @param config The Paging [configuration][PagingConfig]; its [pageSize][PagingConfig.pageSize] maps to the API's `per_page`, defaults to 25.
     * @param resolveLevel How deeply nested [Story] relations are resolved, see [story].
     * @param query Configures the query parameters via the [StoriesQuery] DSL.
     * @return A [Pager] whose [flow][Pager.flow] emits the stories with [Component] content, with potential cached
     * and fresh values.
     */
    public fun stories(
        config: PagingConfig = PagingConfig(pageSize = 25),
        resolveLevel: Int = 1,
        query: StoriesQuery<Component>.() -> Unit = {},
    ): Pager<Int, Story<Component>>

    /**
     * Retrieves multiple [stories][Story] as a [PagingData] stream with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the stories' content.
     * @param config The Paging [configuration][PagingConfig]; its [pageSize][PagingConfig.pageSize] maps to the API's `per_page`, defaults to 25.
     * @param typeInfo Type information for deserialization; must describe a `Story<T>`.
     * @param resolveLevel How deeply nested [Story] relations are resolved, see [story].
     * @param query Configures the query parameters via the [StoriesQuery] DSL.
     * @return A [Pager] whose [flow][Pager.flow] emits the stories, with potential cached and fresh values.
     */
    public fun <T : Component> stories(
        config: PagingConfig,
        typeInfo: TypeInfo,
        resolveLevel: Int = 1,
        query: StoriesQuery<T>.() -> Unit = {},
    ): Pager<Int, Story<T>>

    public companion object {

        /**
         * Creates a [StoryblokClient] with full configuration control.
         *
         * @param apiBuilder Configuration block for the [Content Delivery API][Api.Config.Content].
         * @param serializersModuleBuilder Configuration block for registering custom [Component] serializers.
         * @param jsonBuilder Configuration block for JSON parsing settings.
         */
        public operator fun invoke(
            apiBuilder: Api.Config.Content.() -> Unit,
            serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
            jsonBuilder: JsonBuilder.() -> Unit,
        ): StoryblokClient = StoryblokClientImpl(apiBuilder, serializersModuleBuilder, jsonBuilder)

        /**
         * Creates a [StoryblokClient] with simplified configuration.
         *
         * @param lenientJsonParsing When `true`, enables lenient JSON parsing (ignores unknown keys, coerces nulls).
         * @param serializersModuleBuilder Configuration block for registering custom [Component] serializers.
         * @param apiBuilder Configuration block for the [Content Delivery API][Api.Config.Content].
         */
        public operator fun invoke(
            lenientJsonParsing: Boolean = false,
            serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
            apiBuilder: Api.Config.Content.() -> Unit,
        ): StoryblokClient = this(
            apiBuilder,
            serializersModuleBuilder,
            jsonBuilder = {
                explicitNulls = !lenientJsonParsing
                coerceInputValues = lenientJsonParsing
                ignoreUnknownKeys = lenientJsonParsing
            }
        )

        /**
         * Creates a [StoryblokClient] with minimal configuration.
         *
         * @param accessToken The API access token for authentication.
         * @param version The content [version][Api.Config.Version] to retrieve (draft or published).
         * @param region Optional [region][Api.Config.Region] depending on the server location of your space. Defaults to [EU][Api.Config.Region.EU].
         * @param language Optional language code for localized content.
         * @param fallbackLanguage Optional fallback language for untranslated fields.
         * @param cv Optional cache version timestamp.
         * @param serializersModule Optional serializers module with custom [Component] serializers.
         */
        public operator fun invoke(
            accessToken: String,
            version: Version,
            region: Api.Config.Region = Api.Config.Region.EU,
            language: String? = null,
            fallbackLanguage: String? = null,
            cv: String? = null,
            serializersModule: SerializersModule = EmptySerializersModule()
        ): StoryblokClient = this(
            lenientJsonParsing = version == Version.Published,
            serializersModuleBuilder = { include(serializersModule) },
            apiBuilder = {
                this.region = region
                this.accessToken = accessToken
                this.version = version
                this.language = language
                this.fallbackLanguage = fallbackLanguage
                this.cv = cv
            }
        )
    }
}
