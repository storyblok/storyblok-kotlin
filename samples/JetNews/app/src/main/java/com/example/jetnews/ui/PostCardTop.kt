package com.example.jetnews.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jetnews.R
import com.example.jetnews.model.Post

val defaultSpacerSize = 16.dp

@Composable
fun PostCardTop(post: Post, modifier: Modifier = Modifier) {
    val typography = MaterialTheme.typography
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        AsyncImage(
            model = post.image.filename,
            contentDescription = post.image.alt,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .heightIn(min = 180.dp)
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
        )
        Spacer(Modifier.height(defaultSpacerSize))

        Text(
            text = post.title,
            style = typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            text = post.author.name,
            style = typography.labelLarge,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = stringResource(
                id = R.string.home_post_min_read,
                "${post.date.month.name.run { first() + drop(1).lowercase() }} ${post.date.day}",
                post.readTimeMinutes,
            ),
            style = typography.bodySmall,
        )
    }
}