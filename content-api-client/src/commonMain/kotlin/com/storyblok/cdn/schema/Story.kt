@file:OptIn(ExperimentalUuidApi::class)

package com.storyblok.cdn.schema

import kotlinx.datetime.LocalDate
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a single story retrieved from the Storyblok API.
 */
@Serializable
public data class Story<T : Component>(
    /** Story ID. */
    val id: Long,

    /** Story UUID. */
    val uuid: Uuid,

    /** Story name. */
    val name: String,

    /** An object containing the field data associated with a content type's specific structure. Also includes a component property with the content type's technical name. */
    val content: T,

    /** Story slug. */
    val slug: String,

    /** Story full slug, combining the parent folder(s) and the story slug. */
    @SerialName("full_slug")
    val fullSlug: String,

    /** Creation timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("created_at")
    val createdAt: Instant,

    /** Latest publication timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("published_at")
    val publishedAt: Instant?,

    /** First publication timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("first_published_at")
    val firstPublishedAt: Instant?,

    /** Latest update timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("updated_at")
    val updatedAt: Instant?,

    /** Date defined in the story's entry configuration (Format: YYYY-mm-dd). */
    @SerialName("sort_by_date")
    val sortByDate: LocalDate?,

    /** Numeric representation of the story's position in the folder. Users can change this property in the Content tab. */
    val position: Int,

    /** Array of tag names. */
    @SerialName("tag_list")
    val tagList: List<String>,

    /** True if the story is defined as folder root. */
    @SerialName("is_startpage")
    val isStartPage: Boolean,

    /** Parent folder ID. */
    @SerialName("parent_id")
    val parentId: Long?,

    /** Object to store non-editable data that is exclusively maintained with the Management API. */
    @SerialName("meta_data")
    val metadata: Map<String, String>?,

    /** Group ID (UUID string), shared between stories defined as alternates. */
    @SerialName("group_id")
    val groupId: Uuid,

    /** Current release ID (if requested via the from_release parameter). */
    @SerialName("release_id")
    val releaseId: Long? = null,

    /** Language code of the current language version (if requested via the language parameter). */
    @SerialName("lang")
    val language: String,

    /** Real path defined in the story's entry configuration (see Visual Editor). */
    val path: String?,

    /** An array containing objects that provide basic data of the stories defined as alternates of the current story. */
    val alternates: List<Alternate>,

    /** Contains the complete slug of the default language (if the Translatable Slugs app is installed). */
    @SerialName("default_full_slug")
    val defaultFullSlug: String?,

    /** Array of translated slug objects (if the Translatable Slugs app is installed). */
    @SerialName("translated_slugs")
    val translatedSlugs: List<TranslatedSlug>?
) {
    /**
     * Basic data for a story defined as an alternate of the current story.
     *
     * Alternates are different language versions or variants of the same content.
     */
    @Serializable
    public data class Alternate(
        /** Story ID. */
        val id: Long,
        /** Story name. */
        val name: String,
        /** Story slug. */
        val slug: String,
        /** True if the story is currently published. */
        val published: Boolean,
        /** Story full slug, combining the parent folder(s) and the story slug. */
        @SerialName("full_slug")
        val fullSlug: String,
        /** True if the instance constitutes a folder. */
        @SerialName("is_folder")
        val isFolder: Boolean,
        /** ID of the parent folder. */
        @SerialName("parent_id")
        val parentId: Long,
    )

    /**
     * Translated slug information for localized story variants.
     *
     * Available when the Translatable Slugs app is installed.
     */
    @Serializable
    public data class TranslatedSlug(
        /** Translated slug. */
        val path: String,
        /** Translated name. */
        val name: String?,
        /** Language code of story variant. */
        @SerialName("lang")
        val language: String,
        /** True if story variant is currently published. */
        val published: Boolean?,
    )

}
