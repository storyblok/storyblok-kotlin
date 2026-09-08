package com.example.jetnews.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.jetnews.model.Post
import com.example.jetnews.resources.Res
import com.example.jetnews.resources.home_post_min_read
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthorAndReadTime(post: Post, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text(
            text = stringResource(
                Res.string.home_post_min_read,
                post.author.name,
                post.readTimeMinutes,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}