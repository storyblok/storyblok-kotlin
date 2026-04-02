package com.example.jetnews.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jetnews.model.Post

@Composable
fun PostHeaderImage(post: Post) {
    AsyncImage(
        model = post.image.filename,
        contentDescription = post.image.alt,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .heightIn(min = 180.dp)
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.large)
    )
}