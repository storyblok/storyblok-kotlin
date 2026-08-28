package com.example.jetnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetNewsTheme {
                val backStack = rememberNavBackStack(HomeKey)

                Storyblok(
                    accessToken = "t56rE6UQJVErhMrkKvAe8Att",
                    version = if (BuildConfig.DEBUG) Draft else Published,
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
    }
}
