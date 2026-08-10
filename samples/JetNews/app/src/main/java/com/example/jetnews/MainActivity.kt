package com.example.jetnews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.jetnews.ContentState.*
import com.example.jetnews.model.Feed
import com.example.jetnews.model.Header
import com.example.jetnews.model.HighlightedPost
import com.example.jetnews.model.Metadata
import com.example.jetnews.model.PopularPosts
import com.example.jetnews.model.Post
import com.example.jetnews.model.RecentPosts
import com.example.jetnews.model.RecommendedPosts
import com.example.jetnews.ui.PostCardHistory
import com.example.jetnews.ui.PostCardPopular
import com.example.jetnews.ui.LoadError
import com.example.jetnews.ui.PostCardSimple
import com.example.jetnews.ui.PostCardTop
import com.example.jetnews.ui.PostHeaderImage
import com.example.jetnews.ui.PostListDivider
import com.example.jetnews.ui.PostMetadata
import com.example.jetnews.ui.defaultSpacerSize
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val backStack = rememberNavBackStack(HomeKey)
                    var favorites by remember { mutableStateOf(emptySet<String>()) }

                    Storyblok(
                        accessToken = "t56rE6UQJVErhMrkKvAe8Att",
                        version = if (BuildConfig.DEBUG) Draft else Published,
                        blockProvider = blockProvider(
                            fallback = { _, _ -> /* TODO: Show some kind of error UI */ },
                            storyLinkListener = { uuid, _ -> backStack.add(StoryKey(uuid = uuid)) }
                        ) {

                            val LocalPost = compositionLocalOf<Post> { error("No post provided") }

                            //Content Types

                            block<Post> { post, modifier ->
                                remember(post.uid) { post }
                                CompositionLocalProvider(LocalPost provides post) {
                                    RichText(post.body, modifier.padding(16.dp))
                                }
                            }

                            block<Feed> { page, modifier ->
                                LazyColumn(modifier) {
                                    items(page.body, key = { it.uid }) { Block(it, Modifier.fillMaxWidth()) }
                                }
                            }

                            // Nestables - Post

                            block<Header> { header, _ ->
                                val post = LocalPost.current
                                PostHeaderImage(post)
                                Spacer(Modifier.height(defaultSpacerSize))
                                Text(header.alternativeTitle.ifEmpty { post.title }, style = MaterialTheme.typography.headlineLarge)
                                Spacer(Modifier.height(8.dp))
                                val subtitle = header.alternativeSubtitle.ifEmpty { post.subtitle }
                                if (subtitle != null) {
                                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(defaultSpacerSize))
                                }
                            }

                            block<Metadata> { _, modifier -> PostMetadata(LocalPost.current, modifier) }

                            // Nestables - Feed

                            block<HighlightedPost> { highlighted, _ ->
                                Text(
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                                    text = highlighted.title,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                PostCardTop(
                                    post = highlighted.post.content,
                                    modifier = Modifier.clickable(onClick = { backStack.add(StoryKey(highlighted.post)) }),
                                )
                                PostListDivider()
                            }

                            block<RecommendedPosts> { recommended, modifier ->
                                Column(modifier) {
                                    recommended.posts.forEach { post ->
                                        PostCardHistory(post, recommended.strapline.uppercase(), { backStack.add(StoryKey(post)) })
                                        PostListDivider()
                                    }
                                }
                            }

                            block<PopularPosts> { popular, modifier ->
                                Column(modifier) {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        text = popular.title,
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .height(IntrinsicSize.Max)
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        for (post in popular.posts) {
                                            PostCardPopular(post, { backStack.add(StoryKey(post)) })
                                        }
                                    }
                                    Spacer(Modifier.height(defaultSpacerSize))
                                    PostListDivider()
                                }
                            }

                            block<RecentPosts> { recent, modifier ->
                                Column(modifier) {
                                    recent.posts.forEach { post ->
                                        PostCardSimple(
                                            post = post,
                                            navigateToArticle = { backStack.add(StoryKey(post)) },
                                            isFavorite = favorites.contains(post.slug),
                                            onToggleFavorite = {
                                                if(post.slug in favorites) {
                                                    favorites -= post.slug
                                                } else {
                                                    favorites += post.slug
                                                }
                                           },
                                        )
                                        PostListDivider()
                                    }
                                }
                            }
                        }
                    ) {

                        NavDisplay(
                            backStack,
                            Modifier.padding(innerPadding),
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
                                                    val story = when(key.uuid) {
                                                        null -> story(slug = key.slug!!)
                                                        else -> story(uuid = key.uuid)
                                                    }
                                                    story.onCompletion { cause ->
                                                        state = when(cause) {
                                                            null -> Loaded
                                                            is CancellationException -> Loading
                                                            else -> Failed
                                                        }
                                                    }
                                                    .catch { e ->
                                                        if(e !is StoryblokClientException) throw e
                                                        e.printStackTrace()
                                                    }
                                                }
                                        }
                                        .collectAsStateWithLifecycle(key.story)

                                    PullToRefreshBox(
                                        isRefreshing = state == Refreshing,
                                        onRefresh = { state = Refreshing },
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        story?.run { return@PullToRefreshBox Block(content) }
                                        when(state) {
                                            Loading -> LoadingIndicator(Modifier.align(Alignment.Center))
                                            Failed, Refreshing -> LoadError(Modifier.fillMaxSize())
                                            Loaded -> error("Loaded without content")
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
}
