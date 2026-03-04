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
    fallback: @Composable (unknownComponent: Component) -> Unit = { throw IllegalStateException("Unknown component ${it.component}") },
    builder: BlokProviderScope.() -> Unit,
): BlokProvider = blokProviderWithoutRichText(fallback) {
    builder()
    defaultRichText<RichText.Document> { document ->
        FlowRow {
            document.content.forEach { RichText(it) }
        }
    }
    defaultRichText<RichText.Heading> {
        Text(
            it.text, modifier = Modifier.fillMaxWidth(), style = when (it.level) {
                1 -> MaterialTheme.typography.displayLarge
                2 -> MaterialTheme.typography.displayMedium
                3 -> MaterialTheme.typography.displaySmall
                4 -> MaterialTheme.typography.headlineLarge
                5 -> MaterialTheme.typography.headlineMedium
                else -> MaterialTheme.typography.headlineSmall
            }
        )
    }
    defaultRichText<RichText.BulletList> { list ->
        list.content.forEach { point ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("• \t\t")
                point.content.forEach { RichText(it) }
            }
        }
    }
    defaultRichText<RichText.Paragraph> { paragraph ->
        Text(buildAnnotatedString {
            paragraph.content.forEach { append(it.text) }
        })
    }
    defaultRichText<RichText.Blok> { blok ->
        blok.body.forEach { Blok(it) }
    }
}