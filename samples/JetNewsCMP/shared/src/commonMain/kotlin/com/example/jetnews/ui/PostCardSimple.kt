package com.example.jetnews.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jetnews.model.Post
import com.storyblok.cdn.schema.Story

@Composable
fun PostCardSimple(post: Story<Post>, navigateToArticle: (Story<Post>) -> Unit) {
    Row(modifier = Modifier.clickable(onClick = { navigateToArticle(post) })) {
        PostImage(post.content.thumbnailImage, Modifier.padding(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp),
        ) {
            PostTitle(post.content)
            AuthorAndReadTime(post.content)
        }
    }
}
