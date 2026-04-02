package com.example.jetnews.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.jetnews.R
import com.example.jetnews.model.Post
import com.storyblok.cdn.schema.Story

@Composable
fun PostCardSimple(post: Story<Post>, navigateToArticle: (Story<Post>) -> Unit, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    val bookmarkAction = stringResource(if (isFavorite) R.string.unbookmark else R.string.bookmark)
    Row(
        modifier = Modifier
            .clickable(onClick = { navigateToArticle(post) })
            .semantics {
                // By defining a custom action, we tell accessibility services that this whole
                // composable has an action attached to it. The accessibility service can choose
                // how to best communicate this action to the user.
                customActions = listOf(
                    CustomAccessibilityAction(
                        label = bookmarkAction,
                        action = {
                            onToggleFavorite()
                            true
                        },
                    ),
                )
            },
    ) {
        PostImage(post.content.thumbnailImage, Modifier.padding(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
        ) {
            PostTitle(post.content)
            AuthorAndReadTime(post.content)
        }
        BookmarkButton(
            isBookmarked = isFavorite,
            onClick = onToggleFavorite,
            // Remove button semantics so action can be handled at row level
            modifier = Modifier
                .clearAndSetSemantics {}
                .padding(vertical = 2.dp, horizontal = 6.dp),
        )
    }
}

@Composable
fun BookmarkButton(isBookmarked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val clickLabel = stringResource(
        if (isBookmarked) R.string.unbookmark else R.string.bookmark,
    )
    IconToggleButton(
        checked = isBookmarked,
        onCheckedChange = { onClick() },
        modifier = modifier.semantics {
            // Use a custom click label that accessibility services can communicate to the user.
            // We only want to override the label, not the actual action, so for the action we pass null.
            this.onClick(label = clickLabel, action = null)
        },
    ) {
        Icon(
            painter = if (isBookmarked) painterResource(R.drawable.ic_bookmark) else painterResource(
                R.drawable.ic_bookmark_outline
            ),
            contentDescription = null, // handled by click label of parent
        )
    }
}