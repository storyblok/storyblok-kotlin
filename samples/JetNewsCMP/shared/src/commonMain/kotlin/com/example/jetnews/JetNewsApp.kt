package com.example.jetnews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.example.jetnews.ContentState.*
import com.example.jetnews.ui.LoadError
import com.example.jetnews.ui.StoryTopBar
import com.example.jetnews.ui.theme.JetNewsTheme
import com.storyblok.cdn.StoryblokClientException
import com.storyblok.compose.Storyblok
import com.storyblok.compose.provider.blockProvider
import com.storyblok.ktor.Api.Config.Version.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

/**
 * The whole of JetNews. The Android, iOS and web entry points each do nothing but call this.
 *
 * @param draft Whether to read unpublished content. Android passes `BuildConfig.DEBUG`.
 */
@Composable
fun JetNewsApp(draft: Boolean) {
    // Coil registers its network fetcher automatically only on the JVM, so wire it up explicitly:
    // the Storyblok client already brings a Ktor engine for every target.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }

    JetNewsTheme {
        val backStack = rememberNavBackStack(NavConfiguration, HomeKey)

        Storyblok(
            accessToken = "t56rE6UQJVErhMrkKvAe8Att",
            version = if (draft) Draft else Published,
            blockProvider = blockProvider(
                fallback = { _, _ -> /* TODO: Show some kind of error UI */ },
                storyLinkListener = { uuid, _ -> backStack.add((StoryKey(uuid = uuid))) },
                builder = blockLibrary { backStack.add(it) },
            ),
        ) {
            NavDisplay(
                backStack,
                Modifier.fillMaxSize(),
                entryProvider = entryProvider {
                    entry<StoryKey> { key ->
                        var state by remember { mutableStateOf(Loading) }

                        val story by
                            remember {
                                snapshotFlow { state }
                                    .map { it.isFetching }
                                    .distinctUntilChanged()
                                    .filter { it }
                                    .flatMapLatest {
                                        val story = when (key.uuid) {
                                            null -> story(slug = key.slug!!)
                                            else -> story(uuid = key.uuid)
                                        }
                                        story.onCompletion { cause ->
                                            state = when (cause) {
                                                null -> Loaded
                                                is CancellationException -> Loading
                                                else -> Failed
                                            }
                                        }
                                        .catch { e ->
                                            if (e !is StoryblokClientException) throw e
                                            e.printStackTrace()
                                        }
                                    }
                            }
                            .collectAsStateWithLifecycle(key.story)

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = { StoryTopBar(key, story, backStack) }
                        ) { innerPadding ->
                            PullToRefreshBox(
                                isRefreshing = state == Refreshing,
                                onRefresh = { state = Refreshing },
                                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                            ) {
                                story?.run { return@PullToRefreshBox Block(content) }
                                when (state) {
                                    Loading -> LoadingIndicator(Modifier.align(Alignment.Center))
                                    Failed, Refreshing -> LoadError(Modifier.fillMaxSize())
                                    Loaded -> error("Loaded without content")
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}
