package com.example.jetnews.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.jetnews.R
import com.example.jetnews.model.Post

@Composable
fun AuthorAndReadTime(post: Post, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text(
            text = stringResource(
                id = R.string.home_post_min_read,
                post.author.name,
                post.readTimeMinutes,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}