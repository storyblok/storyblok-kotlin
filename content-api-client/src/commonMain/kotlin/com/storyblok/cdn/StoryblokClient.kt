@file:Suppress("UNCHECKED_CAST")

package com.storyblok.cdn

import com.storyblok.InternalAPI
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.Config.Version
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
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

//public class RelationLimitExceededException(
//    public val story: Story<Component>,
//    public val uuids: List<String>
//) : StoryblokClientException("A maximum of 50 stories can be resolved.")

/**
 * Retrieves a [Story] by its slug using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the story content.
 * @param slug The URL path segment identifying the story.
 * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct relations;
 * higher values resolve relations of relations; `0` disables relation resolution entirely — model relation fields as
 * [String] to receive the raw uuids. Relations that cannot be resolved within the level (including circular
 * relations) resolve to `null` for nullable story fields, and fail with a [StoryblokClientException] naming the
 * uuid for non-nullable ones.
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(slug: String, resolveLevel: Int = 1): Flow<Story<T>> =
    story(slug, typeInfo<Story<T>>(), resolveLevel)

/**
 * Retrieves a [Story] by its UUID using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the story content.
 * @param uuid The unique identifier of the story.
 * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct relations;
 * higher values resolve relations of relations; `0` disables relation resolution entirely — model relation fields as
 * [String] to receive the raw uuids. Relations that cannot be resolved within the level (including circular
 * relations) resolve to `null` for nullable story fields, and fail with a [StoryblokClientException] naming the
 * uuid for non-nullable ones.
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(uuid: Uuid, resolveLevel: Int = 1): Flow<Story<T>> =
    story(uuid, typeInfo<Story<T>>(), resolveLevel)

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
     * relation fields as [String] to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [StoryblokClientException] naming the uuid for non-nullable ones.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(slug: String, resolveLevel: Int = 1): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its UUID.
     *
     * @param uuid The unique identifier of the story.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [String] to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [StoryblokClientException] naming the uuid for non-nullable ones.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(uuid: Uuid, resolveLevel: Int = 1): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its slug with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param slug The URL path segment identifying the story.
     * @param typeInfo Type information for deserialization.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [String] to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [StoryblokClientException] naming the uuid for non-nullable ones.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(slug: String, typeInfo: TypeInfo, resolveLevel: Int = 1): Flow<Story<T>>

    /**
     * Retrieves a [Story] by its UUID with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param uuid The unique identifier of the story.
     * @param typeInfo Type information for deserialization.
     * @param resolveLevel How deeply nested [Story] relations are resolved. `1` (the default) resolves direct
     * relations; higher values resolve relations of relations; `0` disables relation resolution entirely — model
     * relation fields as [String] to receive the raw uuids. Relations that cannot be resolved within the level
     * (including circular relations) resolve to `null` for nullable story fields, and fail with a
     * [StoryblokClientException] naming the uuid for non-nullable ones.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(uuid: Uuid, typeInfo: TypeInfo, resolveLevel: Int = 1): Flow<Story<T>>

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

/**
 * Configures an [HttpClient] for the Content Delivery API.
 */
internal fun HttpClientConfig<*>.configureStoryblok(json: Json, apiBuilder: Api.Config.Content.() -> Unit) {
    install(ContentNegotiation) { json(json) }
    install(Storyblok(Api.CDN), apiBuilder)
    install(HttpCache) {
        httpCacheStorage(PUBLIC_CACHE_NAME)?.let { publicStorage(it) }
        httpCacheStorage(PRIVATE_CACHE_NAME)?.let { privateStorage(it) }
    }
}

/**
 * Default [StoryblokClient] implementation. Create clients through the [StoryblokClient] factory functions instead of
 * instantiating this class directly.
 */
@InternalAPI
public class StoryblokClientImpl constructor(
    apiBuilder: Api.Config.Content.() -> Unit,
    serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
    jsonBuilder: JsonBuilder.() -> Unit,
    public val json: Json = Json {
        isLenient = true
        decodeEnumsCaseInsensitive = true
        classDiscriminator = "component"
        serializersModule = SerializersModule {
            polymorphic(Component::class) {
                defaultDeserializer { serializer<Component.Unknown>() }
            }
            serializersModuleBuilder()
        }
        jsonBuilder()
    },
    override val http: HttpClient = HttpClient { configureStoryblok(json, apiBuilder) }
) : StoryblokClient {

    /**
     * Relation fields per component, mapping each field name to whether its [Story] type is nullable (for list
     * relations, whether the list's element type is nullable).
     */
    public val relations: Map<String, Map<String, Boolean>> =
        buildMap {
            json.serializersModule.dumpTo(object : SerializersModuleCollector {
                override fun <T : Any> contextual(kClass: KClass<T>, provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>) = Unit
                override fun <Base : Any> polymorphicDefaultSerializer(baseClass: KClass<Base>, defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?) = Unit
                override fun <Base : Any> polymorphicDefaultDeserializer(baseClass: KClass<Base>, defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?) = Unit

                override fun <Base : Any, Sub : Base> polymorphic(
                    baseClass: KClass<Base>,
                    actualClass: KClass<Sub>,
                    actualSerializer: KSerializer<Sub>
                ): Unit = with(actualSerializer.descriptor) {
                    elementNames
                        .withIndex()
                        .mapNotNull { (index, name) ->
                            generateSequence(getElementDescriptor(index)) { it.elementDescriptors.singleOrNull() }
                                .firstOrNull { "com.storyblok.cdn.schema.Story" in it.serialName }
                                ?.let { name to it.isNullable }
                        }
                        .let { put(serialName, it.ifEmpty { return@let }.toMap()) }
                }
            })
        }

    override fun close(): Unit = http.close()

    override fun story(slug: String, resolveLevel: Int): Flow<Story<Component>> =
        story(slug, typeInfo<Story<Component>>(), resolveLevel)

    override fun story(uuid: Uuid, resolveLevel: Int): Flow<Story<Component>> =
        story(uuid, typeInfo<Story<Component>>(), resolveLevel)

    override fun <T : Component> story(uuid: Uuid, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
        story(uriString = "stories/$uuid", typeInfo, resolveLevel) { parameter("find_by", "uuid") }
    override fun <T : Component> story(slug: String, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> =
        story(uriString = "stories/$slug", typeInfo, resolveLevel)

    private fun <T : Component> story(
        uriString: String,
        typeInfo: TypeInfo,
        resolveLevel: Int,
        block: HttpRequestBuilder.() -> Unit = {},
    ) =
        flow {

            // Sorted so the HttpCache key is not order depend
            val resolveRelations = relations.entries
                .flatMap { (component, fields) -> fields.keys.map { "$component.$it" } }
                .sorted()
                .joinToString(",")

            val parameters: HttpRequestBuilder.() -> Unit = {
                if(resolveRelations.isNotEmpty()) {
                    if (resolveLevel > 0) parameter("resolve_relations", resolveRelations)
                    if (resolveLevel >= 2) parameter("resolve_level", resolveLevel)
                }
                block()
            }

            try {
                val cached = http.get(uriString) {
                    header(HttpHeaders.CacheControl, "only-if-cached, max-stale=${Int.MAX_VALUE}")
                    parameters()
                }
                emit(cached.body<String>())
            } catch (e: ServerResponseException) {
                if(e.response.status != HttpStatusCode.GatewayTimeout) throw e
            }

            val response = http.get(uriString, parameters)

            emit(response.body<String>())
        }
        .distinctUntilChanged()
        .map { response ->
            val body = json.parseToJsonElement(response)

            val story = body.jsonObject["story"]!!.jsonObject

            val rels = body.jsonObject["rels"]
                ?.jsonArray
                .orEmpty()
                .map { it.jsonObject }
                .associateBy { it["uuid"]!!.jsonPrimitive.content }

            json.decodeFromJsonElement(
                @OptIn(io.ktor.utils.io.InternalAPI::class) typeInfo.serializer() as KSerializer<Story<T>>,
                JsonObject(story + ("content" to story["content"]!!.jsonObject.resolve(rels, resolveLevel)))
            )
        }
        .catch {
            if (it is CancellationException) {
                currentCoroutineContext().ensureActive()
                throw it
            }
            val message = (it as? ServerResponseException)?.response?.bodyAsText() ?: it.message
            throw StoryblokClientException(message, it)
        }

    private fun JsonObject.resolve(
        rels: Map<String, JsonElement?>,
        resolveLevel: Int = 1,
        resolving: Map<String, Int> = emptyMap(),
    ): JsonObject {
        val relations = relations[get("component")?.jsonPrimitive?.content].orEmpty()
        val replacements = entries.mapNotNull { (key, value) ->
            key to when(value) {
                is JsonObject if "component" in value -> value.resolve(rels, resolveLevel, resolving)
                is JsonObject if (value["type"] as? JsonPrimitive)?.content == "doc" ->
                    value.resolveRichText(rels, resolveLevel, resolving)
                is JsonPrimitive if value.isString && key in relations ->
                    resolveRelation(value.content, rels, resolveLevel, resolving, nullable = relations.getValue(key))
                is JsonArray -> when(val element = value.firstOrNull()) {
                    is JsonObject if "component" in element ->
                        JsonArray(value.map { it.jsonObject.resolve(rels, resolveLevel, resolving) })
                    is JsonPrimitive if element.isString && key in relations -> value
                        .map { resolveRelation(it.jsonPrimitive.content, rels, resolveLevel, resolving, nullable = relations.getValue(key)) }
                        .let { JsonArray(it) }
                    else -> return@mapNotNull null
                }
                else -> return@mapNotNull null
            }
        }
        return JsonObject(this + replacements.ifEmpty { return this })
    }

    /**
     * Substitutes a relation [uuid] with its story from [rels]. A uuid that is missing from [rels], or already being
     * resolved [resolveLevel] times further up the call stack (a circular relation), resolves to [JsonNull] when the
     * story field is [nullable], and fails with a [SerializationException] naming the uuid otherwise.
     */
    private fun resolveRelation(
        uuid: String,
        rels: Map<String, JsonElement?>,
        resolveLevel: Int,
        resolving: Map<String, Int>,
        nullable: Boolean,
    ): JsonElement {
        val depth = resolving[uuid] ?: 0
        val circular = depth >= resolveLevel
        rels[uuid]?.takeUnless { circular }
            ?.run { return jsonObject.resolve(rels, resolveLevel, resolving + (uuid to depth + 1)) }
        if (nullable) return JsonNull
        throw SerializationException(
            if (circular && resolveLevel > 0) "Circular story relation: $uuid (model the field as a nullable story)"
            else "Unresolved story relation: $uuid (model the field as a nullable story, or as a String to receive the raw uuid)"
        )
    }

    /**
     * Resolves relations of components embedded in a rich text node: `blok` nodes carry their components in
     * `attrs.body`, all other nodes are traversed through their `content` arrays.
     */
    private fun JsonObject.resolveRichText(
        rels: Map<String, JsonElement?>,
        resolveLevel: Int,
        resolving: Map<String, Int>,
    ): JsonObject {
        val replacements = entries.mapNotNull { (key, value) ->
            key to when(value) {
                is JsonArray if key == "content" ->
                    JsonArray(value.map { (it as? JsonObject)?.resolveRichText(rels, resolveLevel, resolving) ?: it })
                is JsonObject if key == "attrs" && (get("type") as? JsonPrimitive)?.content == "blok" -> {
                    val body = value["body"] as? JsonArray ?: return@mapNotNull null
                    JsonObject(value + ("body" to JsonArray(body.map { it.jsonObject.resolve(rels, resolveLevel, resolving) })))
                }
                else -> return@mapNotNull null
            }
        }
        return JsonObject(this + replacements.ifEmpty { return this })
    }
}