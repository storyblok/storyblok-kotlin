@file:OptIn(InternalAPI::class, ExperimentalSerializationApi::class)
@file:Suppress("UNCHECKED_CAST")

package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.Config.Version
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.serializer
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.reflect.KClass

public open class StoryblokClientException(message: String) : Exception(message)

public class RelationLimitExceededException(
    public val story: Story<Component>,
    public val uuids: List<String>
) : StoryblokClientException("A maximum of 50 stories can be resolved.")

public class StoryblokClient constructor(
    apiBuilder: Api.Config.Content.() -> Unit,
    serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
    jsonBuilder: JsonBuilder.() -> Unit,
) {

    public constructor(
        lenientJsonParsing: Boolean = false,
        serializersModuleBuilder: SerializersModuleBuilder.() -> Unit,
        apiBuilder: Api.Config.Content.() -> Unit,
    ): this(
        apiBuilder,
        serializersModuleBuilder,
        jsonBuilder = {
            explicitNulls = !lenientJsonParsing
            coerceInputValues = lenientJsonParsing
            ignoreUnknownKeys = lenientJsonParsing
        }
    )

    public constructor(
        accessToken: String,
        version: Version,
        language: String? = null,
        fallbackLanguage: String? = null,
        cv: String? = null,
        serializersModule: SerializersModule = EmptySerializersModule()
    ): this(
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

    private val json = Json {
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

    internal val relations: Map<String, Set<String>> =
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
        install(Logging) {
            level = LogLevel.BODY
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
        }
    }

    public fun close(): Unit = ktor.close()

    public inline fun <reified T : Component> story(slug: String): Flow<Story<T>> =
        story(slug, typeInfo<Story<T>>())

    public fun <T : Component> story(slug: String, typeInfo: TypeInfo): Flow<Story<T>> = flow {

        val response = ktor.get("stories/$slug") {
            parameter(
                "resolve_relations",
                relations.ifEmpty { return@get }
                    .entries
                    .joinToString(",") { (component, keys) -> keys.joinToString(",") { "$component.$it" } }
            )
        }

        val body = json.parseToJsonElement(response.body<String>())

        if(!response.status.isSuccess()) throw StoryblokClientException(body.jsonArray.joinToString())

        val story = body.jsonObject["story"]!!.jsonObject

        val rels = body.jsonObject["rels"]
            ?.jsonArray
            .orEmpty()
            .map { it.jsonObject }
            .associateBy { it["uuid"]!!.jsonPrimitive.content }

        emit(json.decodeFromJsonElement(
            typeInfo.serializer() as KSerializer<Story<T>>,
            JsonObject(story + ("content" to story["content"]!!.jsonObject.resolve(rels)))
        ))

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
