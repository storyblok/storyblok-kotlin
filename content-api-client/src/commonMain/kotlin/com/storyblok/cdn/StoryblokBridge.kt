package com.storyblok.cdn

import com.storyblok.InternalAPI
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import io.ktor.util.reflect.TypeInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json

/**
 * Storyblok's preview bridge: a page's connection to the Visual Editor that embeds it.
 *
 * Only the web target can be embedded in one, so every other target gets [NoVisualEditor] — which is
 * what leaves [StoryblokClient.story] completing after its fetch exactly as it did before.
 *
 * A [StoryblokClient] holds one for its lifetime and [destroys][destroy] it with itself. That
 * outlives any single [story][StoryblokClient.story] collection, which come and go as the app
 * navigates, and the connection has to survive that.
 *
 * Public only so that the Compose SDK can reach the client's own, as [StoryblokClientImpl.bridge],
 * and point the editor at the blocks it draws.
 *
 * @suppress
 */
@InternalAPI
public interface StoryblokBridge {

    /**
     * [story] as the app should show it: what was fetched, then that story carrying the content the
     * author is editing.
     *
     * Only the content comes from the editor, so slug and name edits do not show here; the editor
     * reloads the preview for those. Relations arrive already resolved. The newest edit is retained,
     * so the restart that every completed fetch causes re-emits it rather than waiting for the next
     * keystroke. An update that cannot be decoded is dropped and the flow stays open, because the
     * editor also pushes the half-finished states between two valid ones.
     *
     * @param story The story as fetched: the first thing emitted and the envelope under every later
     * one. Updates for any other story are ignored.
     * @param resolveLevel How deep to follow a value that is already being read.
     */
    public fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>>

    /**
     * Opens [block]'s form in the editor, as clicking it does on a web page.
     *
     * Does nothing for a block that carries no `_editable`, which only a draft story's blocks do.
     */
    public fun openBlock(block: Component)

    /**
     * The block the author has selected, which the preview outlines and scrolls to — or `null` when
     * none is.
     *
     * The author selects a block by clicking it in the editor's form, by adding, moving or duplicating
     * one, or through [openBlock]. Outside the editor it is always `null`.
     */
    public val selectedBlock: StateFlow<SelectedBlock?>

    /**
     * Asks the editor to do [action] to the [selected block][selectedBlock], as its menu does on a web
     * page.
     *
     * Does nothing unless the selected block has a [menu][SelectedBlock.menu] offering [action].
     */
    public fun perform(action: BlockAction): Unit = Unit

    /**
     * Opens the form of [breadcrumb]'s block, from the selected block's [menu][SelectedBlock.menu], and
     * selects it.
     */
    public fun openBreadcrumb(breadcrumb: BlockBreadcrumb): Unit = Unit

    /** The blocks the editor is pointing the author at, other than the selected one. */
    public val hints: StateFlow<BlockHints> get() = NO_HINTS

    /**
     * Hides the [hovered][BlockHints.hovered] block's hint, as the package does once the pointer has
     * moved over the preview and come to rest.
     */
    public fun dismissHover(): Unit = Unit

    /**
     * Removes whatever the connection holds — on the web target its message listener.
     *
     * The editor is told nothing: a page may be torn down without the preview ending.
     */
    public fun destroy()
}

/**
 * A block the author has selected in the Visual Editor.
 *
 * @suppress
 */
@InternalAPI
public data class SelectedBlock(
    /** The block's `_uid`. */
    public val uid: String,
    /**
     * The `_uid`s of the blocks it is nested in, outermost first — what lets a lazy list find the item
     * holding a block it has not composed. Empty when the editor did not say, as it does not when a
     * block is added, moved or duplicated.
     */
    public val ancestors: List<String> = emptyList(),
    /**
     * The menu the preview draws on it, or `null` until the editor has described one. The editor does
     * when it opens the block's form or the author clicks the block there, not when a block is added,
     * moved or duplicated.
     */
    public val menu: BlockMenu? = null,
)

private val NO_HINTS: StateFlow<BlockHints> = MutableStateFlow(BlockHints())

/**
 * The bridge of a client that is not being previewed: every target but the web, and a web page
 * outside the editor.
 *
 * Public only so that the Compose SDK can tell, and leave the blocks it draws alone.
 *
 * @suppress
 */
@InternalAPI
public object NoVisualEditor : StoryblokBridge {
    override fun <T : Component> story(story: Story<T>, typeInfo: TypeInfo, resolveLevel: Int): Flow<Story<T>> = flowOf(story)
    override fun openBlock(block: Component): Unit = Unit
    override val selectedBlock: StateFlow<SelectedBlock?> = MutableStateFlow(null)
    override fun destroy(): Unit = Unit
}

/**
 * Opens the page's bridge, asking the editor to resolve [resolveRelations] — formatted as the
 * `resolve_relations` request parameter formats them — in everything it sends back, and decoding
 * what it sends with [json].
 *
 * The relations are fixed here rather than per subscription because the bridge posts them to the
 * editor as it initializes. A page shows one story fetched with one set of relations, so there is
 * nothing to vary them by.
 */
internal expect fun StoryblokBridge(json: Json, resolveRelations: String): StoryblokBridge

@Suppress("UNCHECKED_CAST")
internal fun <T : Component> TypeInfo.contentSerializer(): KSerializer<T> =
    serializer(kotlinType!!.arguments.first().type!!) as KSerializer<T>
