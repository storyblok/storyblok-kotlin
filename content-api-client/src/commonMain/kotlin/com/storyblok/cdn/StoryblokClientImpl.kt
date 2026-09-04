@file:Suppress("UNCHECKED_CAST")

package com.storyblok.cdn

import com.storyblok.cdn.query.StoriesQuery
import com.storyblok.cdn.query.StoryQuery
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.storyblok.InternalAPI
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.uuid.Uuid


/** Restricts a request to Ktor's HTTP cache, which answers a miss with a `504 Gateway Timeout`. */
internal fun HttpRequestBuilder.onlyIfCached(): Unit =
    header(HttpHeaders.CacheControl, "only-if-cached, max-stale=${Int.MAX_VALUE}")

/** The `rels` of a response envelope by uuid, which is where relation resolution reads resolved stories from. */
private val JsonObject.rels: Map<String, JsonObject>
    get() = this["rels"]?.jsonArray.orEmpty().map { it.jsonObject }.associateBy { it["uuid"]!!.jsonPrimitive.content }

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
        // Applied after jsonBuilder so it cannot be turned off: the schema classes name their fields with
        // @JsonNames, which is only honoured while alternative names are enabled.
        useAlternativeNames = true
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

    /**
     * Every registered relation as `<component>.<field>`, sorted so that the request URL — and so the HTTP cache key
     * — does not depend on map iteration order. Fixed for the lifetime of the client, since [relations] is.
     */
    private val resolveRelations: String =
        relations.entries
            .flatMap { (component, fields) -> fields.keys.map { "$component.$it" } }
            .sorted()
            .joinToString(",")

    /**
     * The relation-resolution parameters both story endpoints send, given the [resolveLevel] the caller asked for.
     * These are derived from the registered components rather than stated by a query, which is why they are applied
     * here and not through [StoryQuery].
     */
    private fun relationParameters(resolveLevel: Int): Map<String, String> = buildMap {
        if (resolveRelations.isEmpty() || resolveLevel <= 0) return@buildMap
        put("resolve_relations", resolveRelations)
        if (resolveLevel >= 2) put("resolve_level", resolveLevel.toString())
    }

    /**
     * Deserializes a story object as the [Story] type [typeInfo] describes, with its content relations resolved
     * [resolveLevel] deep from the [rels] of the response it arrived in.
     */
    private fun <T : Component> JsonObject.toStory(
        typeInfo: TypeInfo,
        rels: Map<String, JsonElement?>,
        resolveLevel: Int,
    ): Story<T> = json.decodeFromJsonElement(
        @OptIn(io.ktor.utils.io.InternalAPI::class) typeInfo.serializer() as KSerializer<Story<T>>,
        JsonObject(this + ("content" to this["content"]!!.jsonObject.resolve(rels, resolveLevel))),
    )

    override fun close(): Unit = http.close()

    override fun story(
        slug: String,
        resolveLevel: Int,
        query: StoryQuery<Component>.() -> Unit,
    ): Flow<Story<Component>> = story(slug, typeInfo<Story<Component>>(), resolveLevel, query)

    override fun story(
        uuid: Uuid,
        resolveLevel: Int,
        query: StoryQuery<Component>.() -> Unit,
    ): Flow<Story<Component>> = story(uuid, typeInfo<Story<Component>>(), resolveLevel, query)

    override fun <T : Component> story(
        uuid: Uuid,
        typeInfo: TypeInfo,
        resolveLevel: Int,
        query: StoryQuery<T>.() -> Unit,
    ): Flow<Story<T>> = story(
        uriString = "stories/$uuid",
        typeInfo,
        resolveLevel,
        mapOf("find_by" to "uuid") + StoryQuery<T>().apply(query).build(),
    )

    override fun <T : Component> story(
        slug: String,
        typeInfo: TypeInfo,
        resolveLevel: Int,
        query: StoryQuery<T>.() -> Unit,
    ): Flow<Story<T>> = story(
        uriString = "stories/$slug",
        typeInfo,
        resolveLevel,
        StoryQuery<T>().apply(query).build(),
    )

    override fun stories(
        config: PagingConfig,
        resolveLevel: Int,
        query: StoriesQuery<Component>.() -> Unit,
    ): Pager<Int, Story<Component>> = stories(config, typeInfo<Story<Component>>(), resolveLevel, query)

    override fun <T : Component> stories(
        config: PagingConfig,
        typeInfo: TypeInfo,
        resolveLevel: Int,
        query: StoriesQuery<T>.() -> Unit,
    ): Pager<Int, Story<T>> {
        val params = relationParameters(resolveLevel) + StoriesQuery<T>(typeInfo).apply(query).build()
        return http.pager("stories", config, params) { body ->
            val envelope = json.parseToJsonElement(body).jsonObject
            val rels = envelope.rels
            envelope["stories"]!!.jsonArray.map { it.jsonObject.toStory(typeInfo, rels, resolveLevel) }
        }
    }

    private fun <T : Component> story(
        uriString: String,
        typeInfo: TypeInfo,
        resolveLevel: Int,
        params: Map<String, String>,
    ) =
        flow {
            val parameters = relationParameters(resolveLevel) + params

            try {
                val cached = http.get(uriString) {
                    onlyIfCached()
                    parameters.forEach { (name, value) -> parameter(name, value) }
                }
                emit(cached.body<String>())
            } catch (e: ServerResponseException) {
                if(e.response.status != HttpStatusCode.GatewayTimeout) throw e
            }

            val response = http.get(uriString) {
                parameters.forEach { (name, value) -> parameter(name, value) }
            }
            emit(response.body<String>())
        }
        .distinctUntilChanged()
        .map { response ->
            val body = json.parseToJsonElement(response).jsonObject
            body["story"]!!.jsonObject.toStory<T>(typeInfo, body.rels, resolveLevel)
        }
        .catch {
            if (it is CancellationException) {
                currentCoroutineContext().ensureActive()
                throw it
            }
            // Deserialization failures are modelling errors rather than transient API failures, so they propagate
            // unwrapped instead of being reported as a (potentially retryable) StoryblokClientException.
            if (it is SerializationException) throw it
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
