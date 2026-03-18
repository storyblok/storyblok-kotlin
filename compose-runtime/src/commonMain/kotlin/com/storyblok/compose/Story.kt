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
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion

public interface StoryScope : BlokScope {
    public var isRefreshing: Boolean
}


@PublishedApi internal class StoryScopeImpl<T : Component>(
    slug: String,
    typeInfo: TypeInfo,
    client: StoryblokClient,
    blokScope: BlokScope
) : StoryScope, BlokScope by blokScope {
    override var isRefreshing: Boolean by mutableStateOf(true)

    val story: Flow<Story<T>> = snapshotFlow { isRefreshing }
        .filter { it }
        .flatMapLatest {
            client.story<T>(slug, typeInfo)
                .onCompletion { isRefreshing = false }
        }
}

@Composable
public inline fun <reified T : Component> StoryblokScope.Story(
    slug: String,
    content: @Composable StoryScope.(story: Story<T>?) -> Unit
) {
    val scope = remember { StoryScopeImpl<T>(slug, typeInfo<Story<T>>(), client, blokScope) }
    val story by scope.story.collectAsStateWithLifecycle(null)
    scope.content(story)
}

@Composable
public inline fun <reified T : Component> StoryblokScope.Story(
    story: Story<T>,
    content: @Composable StoryScope.(story: Story<T>) -> Unit
) {
    val scope = remember { StoryScopeImpl<T>(story.slug, typeInfo<Story<T>>(), client, blokScope) }
    val story by scope.story.collectAsStateWithLifecycle(story)
    scope.content(story)
}
