@file:OptIn(ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject

/**
 * Base class for Storyblok field types.
 *
 * Represents special field types like [Link] and [Asset] that have structured data.
 */
@Serializable
@JsonClassDiscriminator("fieldtype")
public sealed class Field {
    /** Optional field identifier. */
    public val id: String? = null

    /** Technical name of the field type. */
    @SerialName("fieldtype")
    public val fieldType: String = ""
}

/**
 * Represents a Storyblok multi-link field.
 *
 * Supports various link types including URLs, stories, emails, and assets.
 */
@Serializable
@SerialName("multilink")
public class Link(
    /** The URL for external links. */
    public val url: String? = null,
    /** Link target attribute (e.g., "_blank" for new tab). */
    public val target: String? = null,
    /** Type of link: "url", "story", "email", or "asset". */
    @SerialName("linktype")
    public val linkType: String = "url",
    /** Cached URL resolved by Storyblok. */
    @SerialName("cached_url")
    public val cachedUrl: String? = null,
    /** Email address for email links. */
    public val email: String? = null
) : Field()

/**
 * Represents a Storyblok asset field.
 *
 * Contains metadata and URLs for images, documents, and other uploaded files.
 */
@Serializable
@SerialName("asset")
public class Asset(
    /** Original filename of the asset. */
    public val name: String? = null,
    /** Source or origin of the asset. */
    public val source: String? = null,
    /** Alternative text for accessibility. */
    public val alt: String? = null,
    /** Focal point coordinates for image cropping. */
    public val focus: String?,
    /** Custom metadata key-value pairs. */
    @SerialName("meta_data")
    public val metadata: Map<String, String>? = null,
    /** Asset title. */
    public val title: String? = null,
    /** Full URL to the asset file. */
    public val filename: String,
    /** Copyright information. */
    public val copyright: String? = null,
    /** True if the asset is hosted externally. */
    @SerialName("is_external_url")
    public val isExternalUrl: Boolean = false
) : Field()
