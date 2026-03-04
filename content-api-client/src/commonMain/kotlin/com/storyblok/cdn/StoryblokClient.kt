
package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.cdn.schema.StoryResponse
import com.storyblok.ktor.Api
import com.storyblok.ktor.Api.Config.Version
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer

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
        serializersModule: SerializersModule = EmptySerializersModule()
    ): this(
        lenientJsonParsing = version == Version.Published,
        serializersModuleBuilder = { include(serializersModule) },
        apiBuilder = {
            this.accessToken = accessToken
            this.version = version
            this.language = language
            this.fallbackLanguage = fallbackLanguage
        }
    )

    private val ktor = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                decodeEnumsCaseInsensitive = true
                classDiscriminator = "component"
                serializersModule = SerializersModule {
                    polymorphic(Component::class) {
                        defaultDeserializer { serializer<Component.Unknown>() }
                    }
                    serializersModuleBuilder()
                }
                jsonBuilder()
            })
        }
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
        story(typeInfo<StoryResponse<T>>(), slug)

    @PublishedApi
    internal fun <T : Component> story(
        typeInfo: TypeInfo,
        slug: String
    ): Flow<Story<T>> = flow {
        val response = ktor.get("stories/$slug")
        emit(response.body<StoryResponse<T>>(typeInfo).story)
    }

}