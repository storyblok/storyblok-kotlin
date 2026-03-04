@file:OptIn(ExperimentalCoroutinesApi::class)

package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion

public class StoryScope(slug: String, client: StoryblokClient, blokScope: BlokScope) : BlokScope by blokScope {
    public var isRefreshing: Boolean by mutableStateOf(true)

    internal val story = snapshotFlow { isRefreshing }
        .filter { it }
        .flatMapLatest {
            client.story<Component>(slug)
                .onCompletion { isRefreshing = false }
        }
}

@Composable
public fun StoryblokScope.Story(
    slug: String,
    content: @Composable StoryScope.(story: Story<Component>?) -> Unit
) {
    val scope = remember { StoryScope(slug, client, blokScope) }
    val story by scope.story.collectAsStateWithLifecycle(null)
    scope.content(story)
}

@Composable
public fun StoryblokScope.Story(
    story: Story<Component>,
    content: @Composable StoryScope.(story: Story<Component>?) -> Unit
) {
    val scope = remember { StoryScope(story.slug, client, blokScope) }
    val story by scope.story.collectAsStateWithLifecycle(story)
    scope.content(story)
}
