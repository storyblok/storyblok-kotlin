package com.example.jetnews.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.jetnews.HomeKey
import com.example.jetnews.R
import com.example.jetnews.StoryKey
import com.example.jetnews.model.Post
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.cdn.stories
import com.storyblok.compose.StoryblokScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun StoryblokScope.StoryTopBar(
    key: StoryKey,
    story: Story<out Component>?,
    backStack: NavBackStack<NavKey>
) = when (key) {
    HomeKey -> {
        val textFieldState = rememberTextFieldState()
        val searchBarState = rememberSearchBarState()
        val listState = rememberLazyListState()
        val term by
            remember {
                snapshotFlow { textFieldState.text.toString().trim() }
                    .debounce(300.milliseconds)
                    .distinctUntilChanged()
            }
            .collectAsState(initial = "")
        // A blank term omits `search_term`, so the endpoint returns all posts.
        val posts =
            remember(term) {
                stories<Post> {
                    searchTerm = term.ifEmpty { null }
                }
            }
            .collectAsLazyPagingItems()

        val coroutineScope = rememberCoroutineScope()
        val inputField = @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                onSearch = { },
                placeholder = { Text("Search posts") },
                // While expanded, a back button collapses the search and returns to the home page.
                leadingIcon = if (searchBarState.currentValue != SearchBarValue.Expanded) null else {
                    { BackButton(onClick = { coroutineScope.launch { searchBarState.animateToCollapsed() } }) }
                },
            )
        }

        AppBarWithSearch(state = searchBarState, inputField = inputField)

        ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(
                    count = posts.itemCount,
                    key = posts.itemKey { it.uuid.toString() },
                ) { index ->
                    val post = posts[index] ?: return@items
                    PostCardSimple(
                        post = post,
                        // Navigate without collapsing, so the results are still here when the user comes back.
                        navigateToArticle = { backStack.add(StoryKey(it)) },
                    )
                    PostListDivider()
                }
            }
        }
    }
    else -> TopAppBar(
            title = {
                Text(
                    text = (story?.content as? Post)?.title ?: "JetNews",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = { BackButton(onClick = { backStack.removeAt(backStack.lastIndex) }) },
        )
}

@Composable
private fun BackButton(onClick: () -> Unit) = IconButton(onClick) {
    Icon(
        painter = painterResource(R.drawable.ic_arrow_back),
        contentDescription = "Back",
    )
}

