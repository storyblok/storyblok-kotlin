package com.example.jetnews.model

import com.storyblok.cdn.schema.Asset
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Link
import com.storyblok.cdn.schema.RichText
import com.storyblok.cdn.schema.Story
import com.storyblok.serializers.StoryblokDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("post")
data class Post(
    val title: String,
    val subtitle: String? = null,
    val url: Link,
    val image: Asset,
    val thumbnailImage: Asset,
    val body: RichText,
    @Serializable(with = StoryblokDateTimeSerializer::class)
    val date: LocalDateTime,
    val author: Story<Author>,
    val readTimeMinutes: Int
) : Component()

@Serializable
@SerialName("author")
data class Author(
    val name: String,
    val url: Link? = null
) : Component()

@Serializable
@SerialName("header")
data class Header(
    val alternativeTitle: String,
    val alternativeSubtitle: String,
    val alternativeImage: Asset
) : Component()

@Serializable
@SerialName("metadata")
class Metadata : Component()
