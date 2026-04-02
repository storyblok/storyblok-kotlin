package com.example.jetnews.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.jetnews.R
import com.example.jetnews.model.Post
import com.storyblok.cdn.schema.Story

@Composable
fun PostCardHistory(post: Story<Post>, strapline: String, navigateToArticle: (Story<Post>) -> Unit) {
    var openDialog by remember { mutableStateOf(false) }

    Row(
        Modifier
            .clickable(onClick = { navigateToArticle(post) }),
    ) {
        PostImage(
            image = post.content.thumbnailImage,
            modifier = Modifier.padding(16.dp),
        )
        Column(
            Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = strapline,
                style = MaterialTheme.typography.labelMedium,
            )
            PostTitle(post = post.content)
            AuthorAndReadTime(
                post = post.content,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        IconButton(onClick = { openDialog = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_more_vert),
                contentDescription = stringResource(R.string.cd_more_actions),
            )
        }
    }
    if (openDialog) {
        AlertDialog(
            modifier = Modifier.padding(20.dp),
            onDismissRequest = { openDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.fewer_stories),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.fewer_stories_content),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                Text(
                    text = stringResource(id = R.string.agree),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(15.dp)
                        .clickable { openDialog = false },
                )
            },
        )
    }
}