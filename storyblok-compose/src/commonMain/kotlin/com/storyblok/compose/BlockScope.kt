package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.compose.provider.Provider
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Scope providing composable functions for rendering Storyblok [Component] and [RichText] content.
 */
@Stable
public interface BlockScope {
    /**
     * Renders a Storyblok [Component] using its registered composable.
     *
     * @param content The component to render.
     * @param modifier Optional [Modifier] applied to the rendered content.
     */
    @Composable
    public fun Block(content: Component, modifier: Modifier = Modifier)

    /**
     * Renders a [RichText] node using its registered composable.
     *
     * @param content The rich text node to render.
     * @param modifier Optional [Modifier] applied to the rendered content.
     */
    @Composable
    public fun RichText(content: RichText, modifier: Modifier = Modifier)

    /**
     * Appends a [RichText] node to an [AnnotatedString.Builder] using its registered builder.
     *
     * @param content The rich text node to append.
     */
    @Composable
    public fun AnnotatedString.Builder.RichText(content: RichText)

    /** @suppress */
    @Deprecated(
        message = "Renamed to Block.",
        replaceWith = ReplaceWith("Block(content, modifier)"),
        level = DeprecationLevel.WARNING,
    )
    @Composable
    public fun Blok(content: Component, modifier: Modifier = Modifier): Unit = Block(content, modifier)
}

@JvmInline
internal value class BlockScopeImpl internal constructor(val providers: Map<Any, Provider>) : BlockScope {

    @Composable
    override fun Block(content: Component, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No block registered for ${content.component}") }
            .invoke(content, modifier)

    @Composable
    override fun RichText(content: RichText, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No rich text block registered for ${content.type} accepting Modifier") }
            .invoke(content, modifier)

    @Composable
    override fun AnnotatedString.Builder.RichText(content: RichText) =
        with(providers.getOrElse(content::class to AnnotatedString::class) { error("No rich text block registered for ${content.type} receiving AnnotatedString.Builder") }) {
            invoke(content)
        }
}

/** @suppress */
@Deprecated(
    message = "Renamed to BlockScope.",
    replaceWith = ReplaceWith("BlockScope"),
    level = DeprecationLevel.WARNING,
)
public typealias BlokScope = BlockScope
