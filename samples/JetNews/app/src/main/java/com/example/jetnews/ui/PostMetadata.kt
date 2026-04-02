package com.example.jetnews.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.jetnews.R
import com.example.jetnews.model.Post

@Composable
fun PostMetadata(post: Post, modifier: Modifier = Modifier) {
    Row(
        // Merge semantics so accessibility services consider this row a single element
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_account_circle),
            contentDescription = null, // decorative
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = post.author.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 4.dp),
            )

            Text(
                text = stringResource(
                    id = R.string.article_post_min_read,
                    "${post.date.month.name.run { first() + drop(1).lowercase() }} ${post.date.day}",
                    post.readTimeMinutes,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}