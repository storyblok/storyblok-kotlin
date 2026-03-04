@file:Suppress("UNCHECKED_CAST")

package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import kotlin.jvm.JvmInline

internal sealed interface Provider {

    @Composable
    operator fun invoke(content: Component): Unit =
        error("${content.component} was registered as Blok(content: Component, modifier: Modifier) but invoked as Blok(content: Component)")
    @Composable
    operator fun invoke(content: Component, modifier: Modifier): Unit =
        error("${content.component} was registered as Blok(content: Component) but invoked as Blok(content: Component, modifier: Modifier)")
    @Composable
    operator fun invoke(richText: com.storyblok.cdn.schema.RichText): Unit =
        error("${richText.type} was registered as RichText(content: RichText, modifier: Modifier) but invoked as RichText(content: Component)")
    @Composable
    operator fun invoke(richText: com.storyblok.cdn.schema.RichText, modifier: Modifier): Unit =
        error("${richText.type} was registered as RichText(content: RichText) but invoked as RichText(content: Component, modifier: Modifier)")

    @JvmInline
    value class Blok<T : Component>(private val composable: @Composable (T) -> Unit) : Provider {
        @Composable
        override fun invoke(content: Component) = composable(content as T)
    }
    @JvmInline
    value class ModifiableBlok<T : Component>(private val composable: @Composable (T, Modifier) -> Unit) : Provider {
        @Composable
        override fun invoke(content: Component, modifier: Modifier) = composable(content as T, modifier)
    }
    @JvmInline
    value class RichText<T : com.storyblok.cdn.schema.RichText>(private val composable: @Composable (T) -> Unit) : Provider {
        @Composable
        override fun invoke(richText: com.storyblok.cdn.schema.RichText) = composable(richText as T)
    }
    @JvmInline
    value class ModifiableRichText<T : com.storyblok.cdn.schema.RichText>(private val composable: @Composable (T, Modifier) -> Unit) : Provider {
        @Composable
        override fun invoke(richText: com.storyblok.cdn.schema.RichText, modifier: Modifier) = composable(richText as T, modifier)
    }
}