@file:OptIn(InternalAPI::class, ExperimentalSerializationApi::class, ExperimentalUuidApi::class)
@file:Suppress("UNCHECKED_CAST")

package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.Config.Version
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
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
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
import kotlin.uuid.ExperimentalUuidApi
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
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(slug: String): Flow<Story<T>> =
    story(slug, typeInfo<Story<T>>())

/**
 * Retrieves a [Story] by its UUID using reified type information for the [Component] type.
 *
 * @param T The [Component] type of the story content.
 * @param uuid The unique identifier of the story.
 * @return A [Flow] emitting the story, with potential cached and fresh values.
 */
public inline fun <reified T : Component> StoryblokClient.story(uuid: Uuid): Flow<Story<T>> =
    story(uuid, typeInfo<Story<T>>())

/**
 * Client for the Storyblok [Content Delivery API](https://www.storyblok.com/docs/api/content-delivery/v2).
 *
 * Provides type-safe access to stories with automatic JSON deserialization and relation resolution.
 */
public interface StoryblokClient {

    /** Closes the underlying HTTP client and releases resources. */
    public fun close()

    /**
     * Retrieves a [Story] by its slug.
     *
     * @param slug The URL path segment identifying the story.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(slug: String): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its UUID.
     *
     * @param uuid The unique identifier of the story.
     * @return A [Flow] emitting the story with [Component] content, with potential cached and fresh values.
     */
    public fun story(uuid: Uuid): Flow<Story<Component>>

    /**
     * Retrieves a [Story] by its slug with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param slug The URL path segment identifying the story.
     * @param typeInfo Type information for deserialization.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(slug: String, typeInfo: TypeInfo): Flow<Story<T>>

    /**
     * Retrieves a [Story] by its UUID with explicit type information for the [Component] type.
     *
     * @param T The [Component] type of the story content.
     * @param uuid The unique identifier of the story.
     * @param typeInfo Type information for deserialization.
     * @return A [Flow] emitting the story, with potential cached and fresh values.
     */
    public fun <T : Component> story(uuid: Uuid, typeInfo: TypeInfo): Flow<Story<T>>

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
         * @param language Optional language code for localized content.
         * @param fallbackLanguage Optional fallback language for untranslated fields.
         * @param cv Optional cache version timestamp.
         * @param serializersModule Optional serializers module with custom [Component] serializers.
         */
        public operator fun invoke(
            accessToken: String,
            version: Version,
            language: String? = null,
            fallbackLanguage: String? = null,
            cv: String? = null,
            serializersModule: SerializersModule = EmptySerializersModule()
        ): StoryblokClient = this(
            lenientJsonParsing = version == Version.Published,
            serializersModuleBuilder = { include(serializersModule) },
            apiBuilder = {
                this.accessToken = accessToken
                this.version = version
                this.language = language
                this.fallbackLanguage = fallbackLanguage
                this.cv = cv
            }
        )
    }
}
internal class StoryblokClientImpl constructor(
    apiBuilder: Api.Config.Content.() -> Unit,
    serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
    jsonBuilder: JsonBuilder.() -> Unit,
) : StoryblokClient {

    private val json = Json {
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
    }

    val relations: Map<String, Set<String>> =
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
                        .filterIndexed { index, _ ->
                            generateSequence(getElementDescriptor(index)) { it.elementDescriptors.singleOrNull() }
                                .any { "com.storyblok.cdn.schema.Story" in it.serialName }
                        }
                        .let { put(serialName, it.ifEmpty { return@let }.toSet())}
                }
            })
        }

    private val ktor = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(Storyblok(Api.CDN), apiBuilder)
    }

    override fun close(): Unit = ktor.close()

    override fun story(slug: String): Flow<Story<Component>> =
        story(slug, typeInfo<Story<Component>>())

    override fun story(uuid: Uuid): Flow<Story<Component>> =
        story(uuid, typeInfo<Story<Component>>())

    override fun <T : Component> story(uuid: Uuid, typeInfo: TypeInfo): Flow<Story<T>> =
        story(uriString = "stories/$uuid", typeInfo) { parameter("find_by", "uuid") }
    override fun <T : Component> story(slug: String, typeInfo: TypeInfo): Flow<Story<T>> =
        story(uriString = "stories/$slug", typeInfo)

    private fun <T : Component> story(uriString: String, typeInfo: TypeInfo, block: HttpRequestBuilder.() -> Unit = {}) =
        flow {

            val resolveRelations = relations.entries
                .joinToString(",") { (component, keys) -> keys.joinToString(",") { "$component.$it" } }

            try {
                val cached = ktor.get(uriString) {
                    header(HttpHeaders.CacheControl, "only-if-cached, max-stale=${Int.MAX_VALUE}")
                    parameter("resolve_relations", resolveRelations.ifEmpty { return@get })
                    block()
                }
                emit(cached.body<String>())
            } catch (e: ServerResponseException) {
                if(e.response.status != HttpStatusCode.GatewayTimeout) throw e
            }

            val response = ktor.get(uriString) {
                parameter("resolve_relations", resolveRelations.ifEmpty { return@get })
                block()
            }

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
                    typeInfo.serializer() as KSerializer<Story<T>>,
                    JsonObject(story + ("content" to story["content"]!!.jsonObject.resolve(rels)))
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

    private fun JsonObject.resolve(rels: Map<String, JsonElement?>): JsonObject {
        val relations = relations[get("component")?.jsonPrimitive?.content].orEmpty()
        val replacements = entries.mapNotNull { (key, value) ->
            key to when(value) {
                is JsonObject if "component" in value -> value.resolve(rels)
                is JsonPrimitive if value.isString && key in relations ->
                    rels[value.content]?.jsonObject?.resolve(rels) ?: JsonNull
                is JsonArray -> when(val element = value.firstOrNull()) {
                    is JsonObject if "component" in element ->
                        JsonArray(value.map { it.jsonObject.resolve(rels) })
                    is JsonPrimitive if element.isString && key in relations -> value
                        .map { rels[it.jsonPrimitive.content]?.jsonObject?.resolve(rels) ?: JsonNull }
                        .let { JsonArray(it) }
                    else -> return@mapNotNull null
                }
                else -> return@mapNotNull null
            }
        }
        return JsonObject(this + replacements.ifEmpty { return this })
    }
}