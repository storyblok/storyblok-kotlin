@file:OptIn(ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject

@Serializable
@JsonClassDiscriminator("fieldtype")
public sealed class Field {
    public val id: String = ""
    @SerialName("fieldtype")
    public val fieldType: String = ""
}

@Serializable
@SerialName("multilink")
public class Link(
    public val url: String? = null,
    public val target: String? = null,
    @SerialName("linktype")
    public val linkType: String = "url",
    @SerialName("cached_url")
    public val cachedUrl: String? = null,
    public val email: String? = null
) : Field()

@Serializable
@SerialName("asset")
public class Asset(
    public val name: String? = null,
    public val source: String? = null,
    public val alt: String?,
    public val focus: Nothing?,
    @SerialName("meta_data")
    public val metadata: JsonObject? = null,
    public val title: String?,
    public val filename: String,
    public val copyright: String?,
) : Field()
