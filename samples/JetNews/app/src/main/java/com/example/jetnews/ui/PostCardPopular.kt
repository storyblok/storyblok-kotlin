package com.example.jetnews.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.jetnews.R
import com.example.jetnews.model.Post
import com.storyblok.cdn.schema.Story

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCardPopular(post: Story<Post>, navigateToArticle: (Story<Post>) -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = { navigateToArticle(post) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .width(280.dp),
    ) {
        Column {
            AsyncImage(
                model = post.content.image.filename,
                contentDescription = post.content.image.alt,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = post.content.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = post.content.author!!.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = stringResource(
                        id = R.string.home_post_min_read,
                        "${post.content.date.month.name.run { first() + drop(1).lowercase() }} ${post.content.date.day}",
                        post.content.readTimeMinutes,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}