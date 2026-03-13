package com.storyblok.compose.provider

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText

public fun blokProvider(
    fallback: @Composable (unknownComponent: Component, Modifier) -> Unit = { it, _ -> throw IllegalStateException("Unknown component ${it.component}") },
    builder: BlokProviderScope.() -> Unit,
): BlokProvider = blokProviderWithoutRichText(fallback) {
    builder()
    defaultRichText<RichText.Document> { document, modifier ->
        FlowRow(modifier) {
            document.content.forEach { RichText(it) }
        }
    }
    defaultRichText<RichText.Heading> { heading, modifier ->
        Text(
            heading.text, modifier = modifier.fillMaxWidth(), style = when (heading.level) {
                1 -> MaterialTheme.typography.displayLarge
                2 -> MaterialTheme.typography.displayMedium
                3 -> MaterialTheme.typography.displaySmall
                4 -> MaterialTheme.typography.headlineLarge
                5 -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.headlineSmall
            }
        )
    }
    defaultRichText<RichText.BulletList> { list, modifier ->
        list.content.forEach { point ->
            Row(modifier = modifier.fillMaxWidth()) {
                Text("• \t\t")
                point.content.forEach { RichText(it) }
            }
        }
    }
    defaultRichText<RichText.Paragraph> { paragraph, modifier ->
        Text(buildAnnotatedString {
            paragraph.content.forEach { append(it.text) }
        }, modifier)
    }
    defaultRichText<RichText.Blok> { blok, modifier ->
        blok.body.forEach { Blok(it, modifier) }
    }
}