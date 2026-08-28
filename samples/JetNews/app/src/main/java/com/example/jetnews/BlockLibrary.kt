package com.example.jetnews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.jetnews.ui.PostCardSimple
import com.example.jetnews.ui.PostCardTop
import com.example.jetnews.ui.PostHeaderImage
import com.example.jetnews.ui.PostListDivider
import com.example.jetnews.ui.PostMetadata
import com.example.jetnews.ui.defaultSpacerSize
import com.storyblok.compose.provider.BlockProviderScope

/**
 * The post currently being rendered, so that nestables such as [Header] and [Metadata] can read the fields of their
 * containing story without them being duplicated onto every nested component.
 */
private val LocalPost = compositionLocalOf<Post> { error("No post provided") }

/**
 * The JetNews component catalog: every Storyblok content type and nestable mapped to the composable that renders it.
 *
 * Pass the result as the `builder` of
 * [blockProvider][com.storyblok.compose.provider.blockProvider], which supplies the Material 3 rich text defaults.
 *
 * @param onOpenStory Navigates to the story identified by the given key.
 */
fun blockLibrary(onOpenStory: (StoryKey) -> Unit): BlockProviderScope.() -> Unit = {

    //Content Types

    block<Post> { post, modifier ->
        remember(post.uid) { post }
        CompositionLocalProvider(LocalPost provides post) {
            RichText(post.body, modifier.padding(16.dp))
        }
    }

    block<Feed> { page, modifier ->
        LazyColumn(modifier) {
            items(page.body, key = { it.uid }) {
                Block(
                    it,
                    Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Nestables - Post

    block<Header> { header, _ ->
        val post = LocalPost.current
        PostHeaderImage(post)
        Spacer(Modifier.height(defaultSpacerSize))
        Text(
            header.alternativeTitle.ifEmpty { post.title },
            style = MaterialTheme.typography.headlineLarge
        )
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
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp
            ),
            text = highlighted.title,
            style = MaterialTheme.typography.titleMedium,
        )
        PostCardTop(
            post = highlighted.post.content,
            modifier = Modifier.clickable(onClick = { onOpenStory(StoryKey(highlighted.post)) }),
        )
        PostListDivider()
    }

    block<RecommendedPosts> { recommended, modifier ->
        Column(modifier) {
            recommended.posts.forEach { post ->
                PostCardHistory(
                    post,
                    recommended.strapline.uppercase(),
                    { onOpenStory(StoryKey(post)) })
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
                    PostCardPopular(post, { onOpenStory(StoryKey(post)) })
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
                    navigateToArticle = { onOpenStory(StoryKey(post)) },
                )
                PostListDivider()
            }
        }
    }
}
