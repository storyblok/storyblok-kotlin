@file:OptIn(ExperimentalCoroutinesApi::class)

package com.storyblok.compose

import androidx.compose.runtime.Composable
import com.storyblok.cdn.StoryblokClient
import com.storyblok.compose.provider.BlokProvider
import com.storyblok.ktor.Api.Config.Version
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val clients = mutableMapOf<List<Any?>, StoryblokClient>()

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

public class StoryblokScope(
    @PublishedApi internal val client: StoryblokClient,
    @PublishedApi internal val blokScope: BlokScope
)