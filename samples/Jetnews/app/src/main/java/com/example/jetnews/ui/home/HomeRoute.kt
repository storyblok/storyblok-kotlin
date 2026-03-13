/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jetnews.model.HighlightedPost
import com.example.jetnews.model.Page
import com.example.jetnews.model.PopularPosts
import com.example.jetnews.model.Author
import com.example.jetnews.model.Body
import com.example.jetnews.model.Header
import com.example.jetnews.model.Metadata
import com.example.jetnews.model.Post
import com.example.jetnews.model.RecentPosts
import com.example.jetnews.model.RecommendedPosts
import com.example.jetnews.ui.article.ArticleScreen
import com.example.jetnews.ui.article.PostContent
import com.example.jetnews.ui.article.PostHeaderImage
import com.example.jetnews.ui.article.PostMetadata
import com.example.jetnews.ui.article.defaultSpacerSize
import com.storyblok.cdn.schema.RichText
import com.storyblok.cdn.schema.Story
import com.storyblok.compose.Story
import com.storyblok.compose.Storyblok
import com.storyblok.compose.provider.blokProvider
import com.storyblok.ktor.Api

/**
 * Displays the Home route.
 *
 * Note: AAC ViewModels don't work with Compose Previews currently.
 *
 * @param homeViewModel ViewModel that handles the business logic of this screen
 * @param isExpandedScreen (state) whether the screen is expanded
 * @param openDrawer (event) request opening the app drawer
 * @param snackbarHostState (state) state for the [Scaffold] component on this screen
 */
@Composable
fun HomeRoute(
    homeViewModel: HomeViewModel,
    isExpandedScreen: Boolean,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // UiState of the HomeScreen
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeRoute(
        uiState = uiState,
        isExpandedScreen = isExpandedScreen,
        onToggleFavorite = { homeViewModel.toggleFavourite(it) },
        onSelectPost = { homeViewModel.selectArticle(it) },
        onRefreshPosts = { homeViewModel.refreshPosts() },
        onErrorDismiss = { homeViewModel.errorShown(it) },
        onInteractWithFeed = { homeViewModel.interactedWithFeed() },
        onInteractWithArticleDetails = { homeViewModel.interactedWithArticleDetails(it) },
        onSearchInputChanged = { homeViewModel.onSearchInputChanged(it) },
        openDrawer = openDrawer,
        snackbarHostState = snackbarHostState,
    )
}

/**
 * Displays the Home route.
 *
 * This composable is not coupled to any specific state management.
 *
 * @param uiState (state) the data to show on the screen
 * @param isExpandedScreen (state) whether the screen is expanded
 * @param onToggleFavorite (event) toggles favorite for a post
 * @param onSelectPost (event) indicate that a post was selected
 * @param onRefreshPosts (event) request a refresh of posts
 * @param onErrorDismiss (event) error message was shown
 * @param onInteractWithFeed (event) indicate that the feed was interacted with
 * @param onInteractWithArticleDetails (event) indicate that the article details were interacted
 * with
 * @param openDrawer (event) request opening the app drawer
 * @param snackbarHostState (state) state for the [Scaffold] component on this screen
 */
@Composable
fun HomeRoute(
    uiState: HomeUiState,
    isExpandedScreen: Boolean,
    onToggleFavorite: (String) -> Unit,
    onSelectPost: (Story<Post>) -> Unit,
    onRefreshPosts: () -> Unit,
    onErrorDismiss: (Long) -> Unit,
    onInteractWithFeed: () -> Unit,
    onInteractWithArticleDetails: (Story<Post>) -> Unit,
    onSearchInputChanged: (String) -> Unit,
    openDrawer: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    Storyblok(
        accessToken = "t56rE6UQJVErhMrkKvAe8Att",
        version = Api.Config.Version.Draft,
        blokProvider = blokProvider {
            blok<Page> { page, modifier ->
                LazyColumn(modifier, verticalArrangement = spacedBy(32.dp)) {
                    items(page.body, key = { it.uid }) { Blok(it, Modifier.fillMaxWidth()) }
                }
            }
            blok<HighlightedPost> { highlighted, _ -> PostListTopSection(highlighted, onSelectPost) }
            blok<RecommendedPosts> { recommended, _ -> PostListHistorySection(recommended, onSelectPost) }
            blok<PopularPosts> { popular, _ -> PostListPopularSection(popular, onSelectPost) }
            blok<RecentPosts> { recent, _ ->
                PostListSimpleSection(
                    recent.posts,
                    onSelectPost,
                    (uiState as HomeUiState.HasPosts).favorites,
                    onToggleFavorite,
                )
            }
            blok<Post>()
            blok<Header, Post> { header, post, _ ->
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
            blok<Metadata, Post> { _, post, modifier -> PostMetadata(post, modifier.padding(bottom = 24.dp)) }
            blok<Body, Post> { body, _, modifier -> RichText(body.text, modifier) }
        }
    ) {
        val homeScreenType = getHomeScreenType(isExpandedScreen, uiState)
        // Guaranteed by above condition for home screen type
        check(uiState is HomeUiState.HasPosts)

        when (homeScreenType) {
            HomeScreenType.ArticleDetails -> Story(uiState.selectedPost!!) { story ->

                ArticleScreen(
                    post = story,
                    isExpandedScreen = isExpandedScreen,
                    onBack = onInteractWithFeed,
                    isFavorite = uiState.favorites.contains(uiState.selectedPost.slug),
                    onToggleFavorite = {
                        onToggleFavorite(uiState.selectedPost.slug)
                    },
                )

                // If we are just showing the detail, have a back press switch to the list.
                // This doesn't take anything more than notifying that we "interacted with the list"
                // since that is what drives the display of the feed
                BackHandler {
                    onInteractWithFeed()
                }
            }
            else -> Story<Page>("home") { story ->
                // Construct the lazy list states for the list and the details outside of deciding which one to
                // show. This allows the associated state to survive beyond that decision, and therefore
                // we get to preserve the scroll throughout any changes to the content.
                val homeListLazyListState = rememberLazyListState()
                when (homeScreenType) {
                    HomeScreenType.FeedWithArticleDetails -> {
                        HomeFeedWithArticleDetailsScreen(
                            home = story?.content,
                            uiState = uiState,
                            showTopAppBar = !isExpandedScreen,
                            onToggleFavorite = onToggleFavorite,
                            onSelectPost = onSelectPost,
                            onRefreshPosts = onRefreshPosts,
                            onErrorDismiss = onErrorDismiss,
                            onInteractWithList = onInteractWithFeed,
                            onInteractWithDetail = onInteractWithArticleDetails,
                            openDrawer = openDrawer,
                            homeListLazyListState = homeListLazyListState,
                            articleDetailLazyListStates = emptyMap(),
                            snackbarHostState = snackbarHostState,
                            onSearchInputChanged = onSearchInputChanged,
                        )
                    }

                    HomeScreenType.Feed -> {
                        HomeFeedScreen(
                            home = story?.content,
                            uiState = uiState,
                            showTopAppBar = !isExpandedScreen,
                            onToggleFavorite = onToggleFavorite,
                            onSelectPost = onSelectPost,
                            onRefreshPosts = onRefreshPosts,
                            onErrorDismiss = onErrorDismiss,
                            openDrawer = openDrawer,
                            homeListLazyListState = homeListLazyListState,
                            snackbarHostState = snackbarHostState,
                            onSearchInputChanged = onSearchInputChanged,
                        )
                    }
                }
            }
        }
    }
}

/**
 * A precise enumeration of which type of screen to display at the home route.
 *
 * There are 3 options:
 * - [FeedWithArticleDetails], which displays both a list of all articles and a specific article.
 * - [Feed], which displays just the list of all articles
 * - [ArticleDetails], which displays just a specific article.
 */
private enum class HomeScreenType {
    FeedWithArticleDetails,
    Feed,
    ArticleDetails,
}

/**
 * Returns the current [HomeScreenType] to display, based on whether or not the screen is expanded
 * and the [HomeUiState].
 */
@Composable
private fun getHomeScreenType(isExpandedScreen: Boolean, uiState: HomeUiState): HomeScreenType = when (isExpandedScreen) {
    false -> {
        when (uiState) {
            is HomeUiState.HasPosts -> {
                if (uiState.isArticleOpen) {
                    HomeScreenType.ArticleDetails
                } else {
                    HomeScreenType.Feed
                }
            }
        }
    }

    true -> HomeScreenType.FeedWithArticleDetails
}
