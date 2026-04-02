package com.example.jetnews.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.storyblok.cdn.schema.Asset

@Composable
fun PostImage(image: Asset, modifier: Modifier = Modifier) {
    AsyncImage(
        model = image.filename,
        contentDescription = image.alt,
        modifier = modifier
            .size(40.dp, 40.dp)
            .clip(MaterialTheme.shapes.small)
    )
}