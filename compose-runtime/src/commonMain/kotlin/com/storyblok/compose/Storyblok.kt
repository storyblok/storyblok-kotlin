@file:OptIn(ExperimentalCoroutinesApi::class)

package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.storyblok.cdn.StoryblokClient
import com.storyblok.compose.provider.BlokProvider
import com.storyblok.ktor.Api.Config.Version
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Composable
public fun Storyblok(
    accessToken: String,
    version: Version,
    language: String? = null,
    fallbackLanguage: String? = null,
    blokProvider: BlokProvider,
    content: @Composable StoryblokScope.() -> Unit,
) {
    val client = remember { StoryblokClient(accessToken, version, language, fallbackLanguage, blokProvider.serializersModule) }
    DisposableEffect(client) { onDispose { client.close() } }
    content(StoryblokScope(client, blokProvider.blokScope))
}

public class StoryblokScope(internal val client: StoryblokClient, internal val blokScope: BlokScope)