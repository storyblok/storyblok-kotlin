@file:OptIn(ExperimentalUuidApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a single story retrieved from the Storyblok API.
 */
@Serializable
public data class Story<T : Component>(
    /** Story ID */
    val id: Long,

    /** The unique identifier of the story. */
    val uuid: Uuid,

    /** The name of the story. */
    val name: String,

    /** The dynamic content of the story. This structure can handle various types of content blocks dynamically. */
    val content: T,

    /** The slug of the story, representing its relative path. */
    val slug: String,

    /** The full slug (absolute path) of the story. */
    @SerialName("full_slug")
    val fullSlug: String,

    /** The creation date of the story in ISO 8601 format. */
    @SerialName("created_at")
    val createdAt: String,

    /** Latest publication timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("published_at")
    val publishedAt: String? = null,

    /** First publication timestamp (Timestamps follow the ISO 8601 standard in UTC). */
    @SerialName("first_published_at")
    val firstPublishedAt: String? = null,

    /** The last update date of the story in ISO 8601 format. */
    @SerialName("updated_at")
    val updatedAt: String,

    /** The last update date of the story in ISO 8601 format. */
    @SerialName("sort_by_date")
    val sortByDate: String?,

    val position: Int,

    @SerialName("tag_list")
    val tagList: List<String>,

    @SerialName("is_startpage")
    val isStartPage: Boolean,

    @SerialName("parent_id")
    val parentId: Long,

    @SerialName("meta_data")
    val metadata: List<String>?,

    @SerialName("group_id")
    val groupId: Uuid,

    @SerialName("release_id")
    val releaseId: Uuid?,

    @SerialName("lang")
    val language: String,
    val path: String?,
    val alternates: List<Uuid>,
    @SerialName("default_full_slug")
    val defaultFullSlug : String?,
    @SerialName("translated_slugs")
    val translatedSlugs: List<String>?
)
