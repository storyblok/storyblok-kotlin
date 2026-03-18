package com.storyblok.compose.provider

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.LinkAnnotation.Url
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.BaselineShift.Companion.Subscript
import androidx.compose.ui.text.style.BaselineShift.Companion.Superscript
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            buildAnnotatedString { heading.content.forEach { RichText(it) } },
            modifier = modifier.fillMaxWidth().padding(bottom = 12.dp),
            style = when (heading.level) {
                1 -> typography.headlineLarge
                2 -> typography.headlineMedium
                3 -> typography.headlineSmall
                4 -> typography.titleLarge
                5 -> typography.titleMedium
                else -> typography.titleSmall
            }
        )
    }

    defaultRichText<RichText.BulletList> { list, modifier -> Text(buildAnnotatedString { RichText(list) }, modifier) }

    defaultRichText<RichText.BulletList> { list ->
        list.content.forEach {
            pushBullet(Bullet.Default)
            RichText(it)
            pop()
            withStyle(ParagraphStyle(lineHeight = 0.sp)) { appendLine() }
        }
    }

    defaultRichText<RichText.OrderedList> { list, modifier -> Text(buildAnnotatedString { RichText(list) }, modifier) }

    defaultRichText<RichText.OrderedList> { list ->
        list.content.forEachIndexed { index, item ->
            pushStyle(ParagraphStyle(textIndent = TextIndent(restLine = 16.sp)))
            append("${index+1}. ")
            RichText(item)
            pop()
            withStyle(ParagraphStyle(lineHeight = 0.sp)) { appendLine() }
        }
    }

    defaultRichText<RichText.ListItem> { item -> item.content.forEach { RichText(it) } }

    defaultRichText<RichText.Paragraph> { paragraph, modifier ->
        Text(
            buildAnnotatedString { RichText(paragraph) },
            modifier.padding(bottom = 24.dp)
        )
    }

    defaultRichText<RichText.Paragraph> { paragraph -> paragraph.content.forEach { RichText(it) } }

    defaultRichText<RichText.HardBreak> { appendLine() }

    defaultRichText<RichText.HorizontalRule> { _, modifier -> HorizontalDivider(modifier) }

    defaultRichText<RichText.Emoji> { append(it.emoji) }

    defaultRichText<RichText.Blockquote> { quote, _  ->
        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            text = buildAnnotatedString { quote.content.forEach { RichText(it) } },
            style = typography.bodyLarge
        )
    }

    defaultRichText<RichText.Text> { text ->
        text.marks.forEach {
            when (it) {
                is RichText.Mark.Bold -> pushStyle(SpanStyle(fontWeight = Bold))
                is RichText.Mark.Italic -> pushStyle(SpanStyle(fontStyle = Italic))
                is RichText.Mark.Underline -> pushStyle(SpanStyle(textDecoration = Underline))
                is RichText.Mark.Strike -> pushStyle(SpanStyle(textDecoration = LineThrough))
                is RichText.Mark.Code -> pushStyle(SpanStyle(fontFamily = Monospace, background = MaterialTheme.colorScheme.onSurface.copy(alpha = .15f)))
                is RichText.Mark.Link -> pushLink(Url(it.href))
                is RichText.Mark.Highlight -> pushStyle(SpanStyle(background = CssColorParser.parse(it.color) ?: Color.Yellow))
                is RichText.Mark.Subscript -> pushStyle(SpanStyle(baselineShift = Subscript))
                is RichText.Mark.Superscript -> pushStyle(SpanStyle(baselineShift = Superscript))
                is RichText.Mark.TextStyle -> pushStyle(SpanStyle(color = CssColorParser.parse(it.color) ?: Color.Unspecified))
            }
        }
        append(text.text)
        text.marks.forEach { _ -> pop() }
    }

    defaultRichText<RichText.CodeBlock> { code, modifier ->
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .15f),
            shape = MaterialTheme.shapes.small,
            modifier = modifier.fillMaxWidth().padding(bottom = 24.dp),
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = buildAnnotatedString { code.content.forEach { RichText(it) } },
                style = TextStyle(fontFamily = Monospace)
            )
        }
    }

    defaultRichText<RichText.Blok> { blok, modifier ->
        blok.body.forEach { Blok(it, modifier) }
    }
}