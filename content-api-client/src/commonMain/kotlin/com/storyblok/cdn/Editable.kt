package com.storyblok.cdn

import com.storyblok.cdn.schema.Component
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val LOGGER = KtorSimpleLogger("com.storyblok.cdn.VisualEditor")

/**
 * Where a block lives, as the Visual Editor names it: what it has to be told to open that block's form.
 *
 * The Content Delivery API carries this on every block of a draft story, in `_editable`, as JSON wrapped in an HTML
 * comment — `<!--#storyblok#{"name": "teaser", "space": "1", "uid": "…", "id": "42"}-->` — for web pages to write into
 * their markup next to the block. This preview has no markup, so it is read from the block instead.
 */
@Serializable
internal class EditableBlock(
    /** The block's technical name. */
    val name: String,
    /** The space's id. Not every `_editable` carries it, and the editor does not need it. */
    val space: String? = null,
    /** The block's `_uid`. */
    val uid: String,
    /** The id of the story the block is in. */
    val id: String,
)

private val EDITABLE = Regex("""^<!--#storyblok#(.*?)-->""")

/** Lenient because the ids are numbers to the editor: it is not worth losing a block over which way one is quoted. */
private val editableJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Where this block lives, read from its `_editable` — or `null` when it has none, as a published story's blocks do not,
 * or when that cannot be read, so that the block is simply not one the editor can be pointed at.
 */
internal fun Component.editableBlock(): EditableBlock? {
    if (editable.isEmpty()) return null
    val payload = EDITABLE.find(editable)?.groupValues?.get(1) ?: return null.also {
        LOGGER.debug("Block ${uid.ifEmpty { component }} has an _editable the Visual Editor would not recognise: $editable")
    }
    return try {
        editableJson.decodeFromString<EditableBlock>(payload)
    } catch (e: SerializationException) {
        LOGGER.debug("Block ${uid.ifEmpty { component }} has an _editable that does not name a block: $editable", e)
        null
    }
}
