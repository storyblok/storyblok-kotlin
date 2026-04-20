@file:OptIn(ExperimentalUuidApi::class)

package com.storyblok.compose.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.LinkAnnotation.Clickable
import androidx.compose.ui.text.LinkAnnotation.Url
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.BaselineShift.Companion.Subscript
import androidx.compose.ui.text.style.BaselineShift.Companion.Superscript
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.material3.richtext.CssColorParser
import com.storyblok.material3.richtext.buildAnnotatedString
import com.storyblok.material3.richtext.withList
import com.storyblok.material3.richtext.withListItem
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Creates a [BlokProvider] with default Material 3 composables for all [RichText] node types.
 *
 * Registers sensible defaults for rendering headings, paragraphs, lists, tables, code blocks,
 * images, and inline text marks using Material 3 typography and color scheme.
 *
 * Custom component composables and overrides can be registered via the [builder] block.
 *
 * @param fallback Composable rendered for unknown or unregistered components.
 * @param storyLinkListener Callback invoked when a story link is clicked, receiving the story UUID and optional anchor.
 * @param builder Configuration block for registering blok composables via [BlokProviderScope].
 */
public fun blokProvider(
    fallback: @Composable (unknownComponent: Component, Modifier) -> Unit = { it, _ -> throw IllegalStateException("Unknown component ${it.component}") },
    storyLinkListener: (uuid: Uuid, anchor: String?) -> Unit = { _, _ -> TODO("No storyLinkListener provided to blokProvider()") },
    builder: BlokProviderScope.() -> Unit,
): BlokProvider = blokProviderWithoutRichText(fallback) {
    builder()

    defaultRichText<RichText.Document> { document, modifier ->
        LazyColumn(modifier) {
            items(document.content, key = { it.hashCode() }) {
                RichText(it, Modifier.fillMaxWidth().padding(bottom = 16.dp))
            }
        }
    }

    defaultRichText<RichText.Heading> { heading, modifier ->
        val style = when (heading.level) {
            1 -> typography.headlineLarge
            2 -> typography.headlineMedium
            3 -> typography.headlineSmall
            4 -> typography.titleLarge
            5 -> typography.titleMedium
            else -> typography.titleSmall
        }
        Text(
            buildAnnotatedString { heading.content.forEach { RichText(it) } },
            modifier = modifier,
            textAlign = heading.textAlign?.run { TextAlign.valueOf(ordinal + 1) },
            inlineContent = heading.inlineContent(style.fontSize),
            style = style
        )
    }

    defaultRichText<RichText.BulletList> { list, modifier ->
        Text(buildAnnotatedString { RichText(list) }, modifier, inlineContent = list.inlineContent())
    }

    defaultRichText<RichText.BulletList> { list ->
        withList(bullet = Bullet.Default) {
            list.content.forEach { item ->
                withListItem { item.content.forEach { RichText(it) } }
            }
        }
    }

    defaultRichText<RichText.OrderedList> { list, modifier ->
        Text(buildAnnotatedString { RichText(list) }, modifier, inlineContent = list.inlineContent())
    }

    defaultRichText<RichText.OrderedList> { list ->
        withList {
            list.content.forEachIndexed { index, item ->
                withListItem {
                    append("${index+1}. ")
                    item.content.forEach { RichText(it) }
                }
            }
        }
    }

    defaultRichText<RichText.Paragraph> { paragraph, modifier ->
        if(paragraph.content.singleOrNull() is RichText.Image)
            return@defaultRichText RichText(paragraph.content.single(), modifier)
        Text(
            buildAnnotatedString { RichText(paragraph) },
            modifier,
            textAlign = paragraph.textAlign?.run { TextAlign.valueOf(ordinal + 1) },
            inlineContent = paragraph.inlineContent()
        )
    }

    defaultRichText<RichText.Paragraph> { paragraph -> paragraph.content.forEach { RichText(it) } }

    defaultRichText<RichText.HardBreak> { appendLine() }

    defaultRichText<RichText.HorizontalRule> { _, modifier ->
        HorizontalDivider(modifier)
    }

    defaultRichText<RichText.Emoji> { append(it.emoji) }

    defaultRichText<RichText.Image> { image, modifier -> AsyncImage(image.src, image.alt, modifier) }

    defaultRichText<RichText.Image> { image ->
        appendInlineContent(image.id, image.alt.orEmpty().ifEmpty { "\uFFFD" })
    }

    defaultRichText<RichText.Blockquote> { quote, modifier  ->
        Text(
            modifier = modifier.padding(horizontal = 16.dp),
            text = buildAnnotatedString { quote.content.forEach { RichText(it) } },
            style = typography.bodyLarge,
            inlineContent = quote.inlineContent()
        )
    }

    defaultRichText<RichText.Text> { text ->
        text.marks.forEach { mark ->
            when (mark) {
                is RichText.Mark.Bold -> pushStyle(SpanStyle(fontWeight = Bold))
                is RichText.Mark.Italic -> pushStyle(SpanStyle(fontStyle = Italic))
                is RichText.Mark.Underline -> pushStyle(SpanStyle(textDecoration = Underline))
                is RichText.Mark.Strike -> pushStyle(SpanStyle(textDecoration = LineThrough))
                is RichText.Mark.Code -> pushStyle(
                    SpanStyle(fontFamily = Monospace, background = colorScheme.onSurface.copy(alpha = .15f))
                )
                is RichText.Mark.Link -> when(mark.linktype) {
                    "url", "asset" -> pushLink(Url(mark.href))
                    "email" -> pushLink(Url("mailto:${mark.href}"))
                    "story" -> pushLink(Clickable(mark.uuid!!, null) { storyLinkListener(Uuid.parse(mark.uuid!!), mark.anchor) })
                    else -> TODO("Unknown linktype: ${mark.linktype}")
                }
                is RichText.Mark.Highlight -> pushStyle(
                    SpanStyle(background = CssColorParser.parse(mark.color) ?: colorScheme.secondary)
                )
                is RichText.Mark.Subscript -> pushStyle(SpanStyle(baselineShift = Subscript))
                is RichText.Mark.Superscript -> pushStyle(SpanStyle(baselineShift = Superscript))
                is RichText.Mark.TextStyle -> pushStyle(SpanStyle(color = CssColorParser.parse(mark.color) ?: Color.Unspecified))
            }
        }
        append(text.text)
        text.marks.forEach { _ -> pop() }
    }

    defaultRichText<RichText.CodeBlock> { code, modifier ->
        Surface(
            color = colorScheme.onSurface.copy(alpha = .15f),
            shape = MaterialTheme.shapes.small,
            modifier = modifier,
        ) {
            Text(
                text = buildAnnotatedString { code.content.forEach { RichText(it) } },
                modifier = Modifier.padding(16.dp),
                color = colorScheme.onSurface,
                style = TextStyle(fontFamily = Monospace),
                inlineContent = code.inlineContent()
            )
        }
    }

    defaultRichText<RichText.Table> { table, modifier ->
        Column(modifier.border(1.dp, Color.Black)) {
            table.content.forEach { RichText(it) }
        }
    }

    defaultRichText<RichText.TableRow> { row, modifier ->
        Row(modifier) {
            row.content.forEach { RichText(it, Modifier.weight(it.columnSpan?.toFloat() ?: 1f)) }
        }
    }

    defaultRichText<RichText.TableCell> { cell, modifier ->
        Box(
            modifier.fillMaxHeight()
                .background(CssColorParser.parse(cell.backgroundColor) ?: Color.Transparent)
                .border(0.5.dp, Color.DarkGray)
                .padding(8.dp)
        ) { cell.content.forEach { RichText(it) } }
    }

    defaultRichText<RichText.TableHeader> { header, modifier ->
        Box(
            modifier.fillMaxHeight()
                .background(Color.LightGray)
                .border(0.5.dp, Color.DarkGray)
                .padding(8.dp)
        ) { header.content.forEach { RichText(it) } }
    }

    defaultRichText<RichText.Blok> { blok, modifier ->
        blok.body.forEach { Blok(it, modifier) }
    }
}


@Composable
private fun RichText.Composite.inlineContent(fontSize: TextUnit = LocalTextStyle.current.fontSize) = flatten()
    .filterIsInstance<RichText.Image>()
    .associate { image ->
        val placeholder = Placeholder(fontSize, fontSize, PlaceholderVerticalAlign.TextCenter)
        image.id to InlineTextContent(placeholder) { AsyncImage(image.src, it) }
    }
