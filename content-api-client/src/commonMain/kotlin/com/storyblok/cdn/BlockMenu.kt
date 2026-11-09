package com.storyblok.cdn

import com.storyblok.InternalAPI
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/*
 * What the Visual Editor tells a preview about the block the author selected and the blocks it points
 * them at, for the preview to draw. `@storyblok/preview-bridge` draws the same thing over a web page:
 * a menu on the selected block, a hint over the block hovered in the editor's form, and highlights.
 */

/**
 * The menu the preview draws on a [selected block][SelectedBlock], as the Visual Editor describes it.
 *
 * @suppress
 */
@InternalAPI
public data class BlockMenu(
    /** The block's name as the editor shows it: its display name, or its technical name made readable. */
    public val label: String,
    /**
     * The blocks from the story's content down to this one, which is the last. Opening one of the others
     * opens its form instead. A block with no parents has just itself, and the menu then only labels it.
     */
    public val breadcrumbs: List<BlockBreadcrumb> = emptyList(),
    /** What the editor lets the author do to the block from the preview, in the order its menu lists them. */
    public val actions: List<BlockAction> = emptyList(),
    /**
     * The last of the breadcrumbs as the editor sent them, which an action on the block is sent back
     * with, as the package sends it, rather than as a copy that would lose what this does not read.
     * Kept with the menu so that an action always names the block the menu was drawn for.
     */
    internal val target: JsonObject? = null,
)

/**
 * A block on the path to a [selected block][SelectedBlock].
 *
 * @suppress
 */
@InternalAPI
public data class BlockBreadcrumb(
    /** The block's `_uid`. */
    public val uid: String,
    /** The block's technical name. */
    public val component: String,
    /** The block's name as the editor shows it. */
    public val label: String,
)

/**
 * What the author can do to a selected block from its menu in the preview. The editor carries each out.
 *
 * @suppress
 */
@InternalAPI
public enum class BlockAction(
    /** What the editor calls it in the message that asks for it. */
    internal val message: String,
) {
    /** Opens the editor's dialog for adding a block before this one. */
    AddBlockBefore("addBlockBefore"),

    /** Opens the editor's dialog for adding a block after this one. */
    AddBlockAfter("addBlockAfter"),

    /** Adds a copy of the block after it. */
    Duplicate("duplicateBlock"),

    /** Copies the block, for pasting elsewhere in the editor. */
    Copy("copy"),

    /** Swaps the block with the one after it. */
    MoveForward("moveForward"),

    /** Swaps the block with the one before it. */
    MoveBackward("moveBackward"),

    /** Deletes the block. */
    Delete("deleteBlock"),
}

/**
 * The blocks the Visual Editor is pointing the author at, other than the selected one.
 *
 * @suppress
 */
@InternalAPI
public data class BlockHints(
    /** The `_uid` of the block the author is hovering over in the editor's form, or `null`. */
    public val hovered: String? = null,
    /** The `_uid`s of the blocks the editor highlights. */
    public val highlighted: List<String> = emptyList(),
    /** The highlighted block to bring into view, if any is off screen. */
    public val highlightedFocus: String? = null,
)

/**
 * Which of [BlockAction] the editor allows, from the permissions it sends with a selection, in the
 * order the preview bridge package lists them and under the same conditions.
 *
 * [Copy] depends on the editor's version alone. Moving backward also needs the block not to be the
 * first in its field: the package tests `_parentindex` for truth, and a first block's is 0.
 */
internal fun blockActions(
    canAddBlocks: Boolean,
    canMoveForward: Boolean,
    canMoveBackward: Boolean,
    canDeleteBlocks: Boolean,
    parentIndex: Int?,
    appVersion: String,
): List<BlockAction> = buildList {
    if (canAddBlocks) addAll(listOf(BlockAction.AddBlockBefore, BlockAction.AddBlockAfter, BlockAction.Duplicate))
    if (appVersion == "v2") add(BlockAction.Copy)
    if (canMoveForward) add(BlockAction.MoveForward)
    if (canMoveBackward && parentIndex != null && parentIndex != 0) add(BlockAction.MoveBackward)
    if (canDeleteBlocks) add(BlockAction.Delete)
}

/**
 * A block's name as the editor shows it: the display name [componentNames] gives its technical [name],
 * or that name made readable, as the preview bridge package labels it.
 */
internal fun displayName(name: String, componentNames: Map<String, String>): String =
    componentNames[name]?.takeIf { it.isNotEmpty() } ?: humanize(name)

/** `heroSection`, `hero_section` and `hero-section` as `Hero Section`, as the preview bridge package humanizes a name. */
internal fun humanize(name: String): String = name
    .replace(Regex("([A-Z])"), " $1")
    .trim()
    .lowercase()
    .replace(Regex("[_-]"), " ")
    .replace(Regex("""(?:^|\s)\S""")) { it.value.uppercase() }

/**
 * What the editor has said about itself, which the [menus][BlockMenu] it describes are labelled and
 * filled in from.
 */
internal data class EditorContext(
    /** The editor's version, from `enterEditmode`. Only `v2` lets a block be copied. */
    val appVersion: String = "v1",
    /** Display names by technical name, from `enterEditmode`. */
    val componentNames: Map<String, String> = emptyMap(),
    /** The id of the story being edited, which opening a block's form has to name. */
    val storyId: String? = null,
) {
    /**
     * This context once [message] is taken in: with the editor's version and display names from
     * `enterEditmode`, and the story being edited from any message that names it.
     */
    fun after(message: JsonObject): EditorContext {
        val named = message.string("storyId")?.let { copy(storyId = it) } ?: this
        if (message.string("action") != "enterEditmode") return named
        return named.copy(
            appVersion = message.string("appVersion") ?: appVersion,
            componentNames = (message["componentNames"] as? JsonObject)
                ?.mapNotNull { (name, display) -> display.string()?.let { name to it } }
                ?.toMap()
                ?: componentNames,
        )
    }

    /**
     * The menu of the block [uid] that [message] selects, or `null` when it is not an `editedBlok` or
     * `enterComponent` that selects that block.
     */
    fun menuOf(message: JsonObject, uid: String): BlockMenu? {
        val block = message["blok"] as? JsonObject
        when (message.string("action")) {
            "editedBlok" -> if (block?.string("uid") != uid) return null
            "enterComponent" -> if (message.string("componentId") != uid) return null
            else -> return null
        }
        val path = (message["breadcrumbs"] as? JsonArray).orEmpty().mapNotNull { it as? JsonObject }
        val breadcrumbs = path.mapNotNull { crumb ->
            val crumbUid = crumb.string("_uid")?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val component = crumb.string("component").orEmpty()
            BlockBreadcrumb(crumbUid, component, displayName(component, componentNames))
        }
        val name = block?.string("name") ?: path.lastOrNull()?.string("component").orEmpty()
        return BlockMenu(
            label = displayName(name, componentNames),
            breadcrumbs = breadcrumbs,
            // The package has no actions for a block with no parents, which is the story's content.
            actions = if (breadcrumbs.size < 2) emptyList() else blockActions(
                canAddBlocks = message.boolean("canAddBlocks"),
                canMoveForward = message.boolean("canMoveForward"),
                canMoveBackward = message.boolean("canMoveBackward"),
                canDeleteBlocks = message.boolean("canDeleteBlocks"),
                parentIndex = path.lastOrNull()?.get("_parentindex")?.int(),
                appVersion = appVersion,
            ),
            target = path.lastOrNull()?.takeIf { it.string("_uid") == uid },
        )
    }
}

/**
 * The blocks pointed at once [message] is handled, given [hints] before it: a hover hint on
 * `hoverComponent`, highlights on `highlightComponent`, and no hover once the author opens a block from
 * the form, as the package hides its hint then.
 */
internal fun hintsAfter(message: JsonObject, hints: BlockHints): BlockHints = when (message.string("action")) {
    "hoverComponent" -> hints.copy(hovered = message.string("componentId")?.takeIf { it.isNotEmpty() })
    "highlightComponent" -> hints.copy(
        highlighted = (message["componentIds"] as? JsonArray).orEmpty().mapNotNull { id -> id.string()?.takeIf { it.isNotEmpty() } },
        highlightedFocus = message.string("componentId")?.takeIf { it.isNotEmpty() },
    )
    "enterComponent" -> hints.copy(hovered = null)
    else -> hints
}

/** A string, or a number written as the editor's ids are sometimes written: as a whole number. */
private fun JsonElement?.string(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive.isString) return primitive.content
    return primitive.contentOrNull?.takeIf { it.toLongOrNull() != null }
}

private fun JsonObject.string(key: String): String? = get(key).string()

private fun JsonObject.boolean(key: String): Boolean = (get(key) as? JsonPrimitive)?.booleanOrNull ?: false

private fun JsonElement.int(): Int? = (this as? JsonPrimitive)?.let { it.intOrNull ?: it.contentOrNull?.toIntOrNull() }
