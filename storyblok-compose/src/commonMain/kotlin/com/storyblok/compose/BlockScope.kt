package com.storyblok.compose

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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

    /**
     * Adds [blocks] to a lazy list, one item each, rendered with [Block] and keyed by their `_uid`.
     *
     * ```kotlin
     * val state = rememberLazyListState()
     * LazyColumn(state = state) {
     *     item { Header() }
     *     blocks(page.body, state)
     * }
     * ```
     *
     * Outside Storyblok's Visual Editor this is all it does. In the editor, when the author selects a
     * block whose item is off screen — in the editor's form, or by adding, moving or duplicating one —
     * the list scrolls to that item, which a lazy list cannot do by itself: an item off screen is not
     * composed, so it has no block to bring into view. That covers a block nested anywhere inside an
     * item, as far as the editor says which blocks it is nested in.
     *
     * The list follows a selection while one of these items is on screen, and each selection only
     * once, so the author can scroll away from it.
     *
     * Blocks are keyed by `_uid` so that the list can find them, and so that each keeps its state when
     * the author moves it. If two share one, or one has none, they are keyed by position instead and
     * the list does not follow the selection.
     *
     * @param blocks The blocks to add, such as a page's body.
     * @param state The [LazyListState] the list was given, which it is scrolled with.
     * @param itemContent Renders each block as its item: [Block] by default. Pass your own to give the
     * blocks a modifier, such as `{ Block(it, Modifier.fillMaxWidth().animateItem()) }`.
     */
    public fun LazyListScope.blocks(
        blocks: List<Component>,
        state: LazyListState,
        itemContent: @Composable LazyItemScope.(block: Component) -> Unit = { Block(it) },
    ) {
        val keyed = blocks.none { it.uid.isEmpty() } && blocks.distinctBy { it.uid }.size == blocks.size
        items(blocks, key = if (keyed) { block -> block.uid } else null) { block ->
            if (keyed) FollowSelection(block, blocks, state)
            itemContent(block)
        }
    }

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
    override fun Block(content: Component, modifier: Modifier) {
        val provider = providers.getOrElse(content::class) { error("No block registered for ${content.component}") }
        // Ahead of the caller's modifier, so that a tap the editor holds back never reaches a clickable in it.
        val editor = LocalVisualEditor.current
        provider.invoke(content, if (editor == null) modifier else Modifier.editable(content, editor).then(modifier))
    }

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
