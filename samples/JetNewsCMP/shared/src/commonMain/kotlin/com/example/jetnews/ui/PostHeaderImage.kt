package com.example.jetnews.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
            .fillMaxWidth()
            // The 180dp is the Android sample's, where it is written `heightIn(min = 180.dp)`.
            // There the maximum is unbounded, so `AsyncImage` has nothing to fill and settles on the
            // minimum — the asset is far wider than it is tall, so its own ratio would render
            // shorter. Stating it as a fixed height keeps that size while bounding the dimension:
            // an unbounded one makes Coil re-resolve its request size on every recomposition, which
            // flashed the image on each Visual Editor update, and any finite `heightIn` maximum is
            // filled rather than treated as a cap.
            .height(180.dp)
            .clip(shape = MaterialTheme.shapes.large)
    )
}
