@file:OptIn(ExperimentalCoroutinesApi::class)

package com.storyblok.compose

import androidx.compose.runtime.Composable
import com.storyblok.cdn.StoryblokClient
import com.storyblok.compose.provider.BlokProvider
import com.storyblok.ktor.Api.Config.Version
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val clients = mutableMapOf<List<Any?>, StoryblokClient>()

/**
 * Entry point composable for rendering Storyblok content.
 *
 * Creates a [StoryblokClient] and provides a [StoryblokScope] with access to both
 * the client and registered blok composables.
 *
 * @param accessToken The API access token for authentication.
 * @param version The content [version][Version] to retrieve (draft or published).
 * @param language Optional language code for localized content.
 * @param fallbackLanguage Optional fallback language for untranslated fields.
 * @param cv Optional cache version timestamp.
 * @param blokProvider The [BlokProvider] containing registered component composables and serializers.
 * @param content The composable content rendered within the [StoryblokScope].
 */
@Composable
public fun Storyblok(
    accessToken: String,
    version: Version,
    language: String? = null,
    fallbackLanguage: String? = null,
    cv: String? = null,
    blokProvider: BlokProvider,
    content: @Composable StoryblokScope.() -> Unit,
) {
    val client = clients.getOrPut(listOf(accessToken, version, language, fallbackLanguage, cv)) {
        StoryblokClient(accessToken, version, language, fallbackLanguage, cv, blokProvider.serializersModule)
    }
    content(StoryblokScope(client, blokProvider.blokScope))
}

/**
 * Scope combining [StoryblokClient] and [BlokScope] capabilities.
 *
 * Provides access to story fetching via [StoryblokClient] and component rendering via [BlokScope].
 */
public class StoryblokScope(
    /** The underlying [StoryblokClient] used to fetch stories. */
    public val client: StoryblokClient,
    blokScope: BlokScope
) : BlokScope by blokScope, StoryblokClient by client