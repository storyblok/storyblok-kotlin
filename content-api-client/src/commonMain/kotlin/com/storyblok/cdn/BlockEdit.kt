package com.storyblok.cdn

import com.storyblok.InternalAPI
import com.storyblok.cdn.schema.RichText
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Where the author's latest edit in the Visual Editor is, so that the preview can show it.
 *
 * The editor says nothing about where its cursor is: it sends the whole story on every change. This is
 * worked out by comparing that story with the one before it.
 *
 * @suppress
 */
@InternalAPI
public data class BlockEdit(
    /** The `_uid` of the innermost block the edit changed. */
    public val uid: String,
    /** The `_uid`s of the blocks it is nested in, from the story's content inwards. */
    public val ancestors: List<String> = emptyList(),
    /** The node the edit is in in each rich text document on the way to it, outermost first. */
    public val richText: List<RichTextEdit> = emptyList(),
    /**
     * Whether the edit was found by comparing the story with the one before it. The first the editor
     * sends has none to be compared with, so its edit is put in the selected block, and [richText] is
     * not known.
     */
    public val compared: Boolean = true,
    /** Counts the edits, so that two in the same place are still two. */
    public val serial: Int = 0,
) {
    /**
     * The index in [document]'s content of the node the edit is in, or `null` when the edit is not in
     * [document].
     *
     * Rich text has no ids, so a document is recognised by its shape: as many nodes as the edited one,
     * with the same text in the edited node. Two documents that alike would both show the edit.
     *
     * An edit that was not [compared] says nothing about rich text, so [document] is compared with
     * [before], as it was drawn before the editor sent anything, by the text of each node. That misses
     * an edit that only changes formatting.
     */
    public fun nodeIn(document: RichText.Document, before: RichText.Document): Int? {
        if (!compared) return changedNode(before, document)
        return richText.firstNotNullOfOrNull { edit ->
            edit.index.takeIf {
                document.content.size == edit.nodes && document.content.getOrNull(it)?.plainText() == edit.text
            }
        }
    }
}

/** The first node of [after] whose text differs from [before]'s, or the last when nodes were only removed from its end. */
private fun changedNode(before: RichText.Document, after: RichText.Document): Int? {
    val old = before.content.map { it.plainText() }
    val new = after.content.map { it.plainText() }
    if (old == new || new.isEmpty()) return null
    return new.indices.firstOrNull { new[it] != old.getOrNull(it) } ?: new.lastIndex
}

/**
 * The top-level node of a rich text document an edit is in.
 *
 * @suppress
 */
@InternalAPI
public data class RichTextEdit(
    /** The node's index in the document's content. */
    public val index: Int,
    /** How many nodes the document's content holds, once edited. */
    public val nodes: Int,
    /** The node's text, once edited, which tells the document apart from others. */
    public val text: String,
)

/**
 * Where [next] differs from [previous], both a story's content as the editor sends it, or `null` when
 * nothing does.
 *
 * Blocks are matched by `_uid`, so that one added, removed or moved does not read as a change to every
 * block after it. The edit is put in the innermost block whose own fields changed: a block added is
 * the edit, and a block removed or moved is an edit to the block holding it. Where several places
 * changed, the first is taken.
 */
internal fun editBetween(previous: JsonElement, next: JsonElement): BlockEdit? {
    val spot = changed(previous, next, Spot(emptyList(), emptyList())) ?: return null
    val uid = spot.blocks.lastOrNull() ?: return null
    return BlockEdit(uid, spot.blocks.dropLast(1), spot.richText)
}

/** Where a walk through the content has got to: the blocks and rich text nodes it is inside. */
private class Spot(val blocks: List<String>, val richText: List<RichTextEdit>)

/** The innermost place [next] differs from [previous], starting from [spot], or `null` when they are equal. */
private fun changed(previous: JsonElement?, next: JsonElement, spot: Spot): Spot? {
    if (previous == next) return null
    return when (next) {
        is JsonObject -> {
            val uid = next.blockUid()
            val here = if (uid == null) spot else Spot(spot.blocks + uid, spot.richText)
            val before = previous as? JsonObject
            when {
                // A block that was not there before is the edit itself, whatever it holds.
                before == null || uid != before.blockUid() -> here
                next.isDocument() -> changedDocument(before, next, here)
                else -> {
                    val key = (next.keys + before.keys).firstOrNull { next[it] != before[it] } ?: return here
                    changed(before[key], next[key] ?: return here, here) ?: here
                }
            }
        }
        is JsonArray -> {
            val before = previous as? JsonArray ?: return spot
            // Indexed once, so that matching every block is linear in the length of the array, not quadratic.
            val beforeByUid = HashMap<String, JsonElement>()
            before.forEach { old -> (old as? JsonObject)?.blockUid()?.let { beforeByUid.getOrPut(it) { old } } }
            next.forEachIndexed { index, element ->
                val uid = (element as? JsonObject)?.blockUid()
                val old = if (uid == null) before.getOrNull(index) else beforeByUid[uid]
                changed(old, element, spot)?.let { return it }
            }
            // Only removed or reordered, which is a change to whatever holds the array.
            spot
        }
        else -> spot
    }
}

/**
 * The place in rich text document [next] that differs from [before]. Its nodes have no ids, so they are
 * matched by position, and the first that differs is the edit: a node typed in, or one inserted.
 */
private fun changedDocument(before: JsonObject, next: JsonObject, spot: Spot): Spot {
    val nodes = next["content"] as? JsonArray ?: return spot
    val old = before["content"] as? JsonArray
    // Only the document's own attributes changed, such as its background: no node is the edit.
    if (nodes.isEmpty() || nodes == old) return spot
    // When nothing differs as far as the edited document goes, nodes were removed from its end.
    val index = nodes.indices.firstOrNull { nodes[it] != old?.getOrNull(it) } ?: nodes.lastIndex
    val inside = Spot(spot.blocks, spot.richText + RichTextEdit(index, nodes.size, nodes[index].plainText()))
    return changed(old?.getOrNull(index), nodes[index], inside) ?: inside
}

/** The `_uid` of the block this is, or `null` when it is not one. */
private fun JsonObject.blockUid(): String? {
    if ("component" !in this) return null
    return (this["_uid"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() }
}

private fun JsonObject.isDocument() = (this["type"] as? JsonPrimitive)?.contentOrNull == "doc"

/** The text of a rich text node as the editor sends it, read as [RichText.plainText] reads a decoded one. */
private fun JsonElement.plainText(): String {
    val node = this as? JsonObject ?: return ""
    if ((node["type"] as? JsonPrimitive)?.contentOrNull == "text") return (node["text"] as? JsonPrimitive)?.contentOrNull.orEmpty()
    return (node["content"] as? JsonArray).orEmpty().joinToString("") { it.plainText() }
}

/** The text of a rich text node: all of the text nodes inside it, run together. */
private fun RichText.plainText(): String = when (this) {
    is RichText.Text -> text
    is RichText.Composite -> content.joinToString("") { it.plainText() }
    else -> ""
}
