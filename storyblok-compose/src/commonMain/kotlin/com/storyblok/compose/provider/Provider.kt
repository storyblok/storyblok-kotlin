@file:Suppress("UNCHECKED_CAST")

package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.storyblok.cdn.schema.Component
import kotlin.jvm.JvmInline

internal sealed interface Provider {

    @Composable
    operator fun invoke(content: Component, modifier: Modifier): Unit = TODO()

    @Composable
    operator fun invoke(richText: com.storyblok.cdn.schema.RichText, modifier: Modifier): Unit = TODO()

    @Composable
    operator fun AnnotatedString.Builder.invoke(richText: com.storyblok.cdn.schema.RichText): Unit = TODO()

    @JvmInline
    value class Blok<T : Component>(private val composable: @Composable (T, Modifier) -> Unit) : Provider {
        @Composable
        override fun invoke(content: Component, modifier: Modifier) = composable(content as T, modifier)
    }

    @JvmInline
    value class RichText<T : com.storyblok.cdn.schema.RichText>(private val composable: @Composable (T, Modifier) -> Unit) : Provider {
        @Composable
        override fun invoke(richText: com.storyblok.cdn.schema.RichText, modifier: Modifier) = composable(richText as T, modifier)
    }

    @JvmInline
    value class RichTextWithinAnnotatedStringBuilder<T : com.storyblok.cdn.schema.RichText>(private val builder: @Composable AnnotatedString.Builder.(T) -> Unit) : Provider {
        @Composable
        override fun AnnotatedString.Builder.invoke(richText: com.storyblok.cdn.schema.RichText) = builder(richText as T)
    }
}
