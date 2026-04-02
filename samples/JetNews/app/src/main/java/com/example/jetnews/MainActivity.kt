@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class,)

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.jetnews.model.Header
import com.example.jetnews.model.HighlightedPost
import com.example.jetnews.model.Metadata
import com.example.jetnews.model.Feed
import com.example.jetnews.model.PopularPosts
import com.example.jetnews.model.Post
import com.example.jetnews.model.RecentPosts
import com.example.jetnews.model.RecommendedPosts
import com.example.jetnews.ui.PostCardHistory
import com.example.jetnews.ui.PostCardPopular
import com.example.jetnews.ui.PostCardSimple
import com.example.jetnews.ui.PostCardTop
import com.example.jetnews.ui.PostHeaderImage
import com.example.jetnews.ui.PostListDivider
import com.example.jetnews.ui.PostMetadata
import com.example.jetnews.ui.defaultSpacerSize
import com.example.jetnews.ui.theme.JetNewsTheme
import com.storyblok.compose.Storyblok
import com.storyblok.compose.provider.blokProvider
import com.storyblok.ktor.Api
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlin.uuid.ExperimentalUuidApi

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
                        version = Api.Config.Version.Draft,
                        blokProvider = blokProvider(
                            fallback = { _, _ -> /* TODO: Show some kind of error UI */ },
                            storyLinkListener = { uuid, _ -> backStack.add(StoryKey(uuid = uuid)) }
                        ) {

                            val LocalPost = compositionLocalOf<Post> { error("No post provided") }

                            //Content Types

                            blok<Post> { post, modifier ->
                                remember(post.uid) { post }
                                CompositionLocalProvider(LocalPost provides post) {
                                    RichText(post.body, modifier.padding(16.dp))
                                }
                            }

                            blok<Feed> { page, modifier ->
                                LazyColumn(modifier) {
                                    items(page.body, key = { it.uid }) { Blok(it, Modifier.fillMaxWidth()) }
                                }
                            }

                            // Nestables - Post

                            blok<Header> { header, _ ->
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

                            blok<Metadata> { _, modifier -> PostMetadata(LocalPost.current, modifier) }

                            // Nestables - Feed

                            blok<HighlightedPost> { highlighted, _ ->
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

                            blok<RecommendedPosts> { recommended, modifier ->
                                Column(modifier) {
                                    recommended.posts.forEach { post ->
                                        PostCardHistory(post, recommended.strapline.uppercase(), { backStack.add(StoryKey(post)) })
                                        PostListDivider()
                                    }
                                }
                            }

                            blok<PopularPosts> { popular, modifier ->
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

                            blok<RecentPosts> { recent, modifier ->
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

                                    var isRefreshing: Boolean by remember { mutableStateOf(true) }

                                    val story by
                                        remember {
                                            snapshotFlow { isRefreshing }
                                                .filter { it }
                                                .flatMapLatest {
                                                    val story = when(key.uuid) {
                                                        null -> story(slug = key.slug!!)
                                                        else -> story(uuid = key.uuid)
                                                    }
                                                    story.onCompletion { isRefreshing = false }
                                                }
                                        }
                                        .collectAsStateWithLifecycle(key.story)

                                    PullToRefreshBox(
                                        isRefreshing = isRefreshing,
                                        onRefresh = { isRefreshing = true },
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Blok(story?.content ?: return@PullToRefreshBox)
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
