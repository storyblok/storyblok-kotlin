package com.example.jetnews.model

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("page")
data class Feed(
    val body: List<Component>
) : Component()

@Serializable
@SerialName("highlighted")
class HighlightedPost(
    val title: String,
    val post: Story<Post>
) : Component()

@Serializable
@SerialName("recent")
class RecentPosts(
    val posts: List<Story<Post>>
) : Component()

@Serializable
@SerialName("popular")
class PopularPosts(
    val title: String,
    val posts: List<Story<Post>>
) : Component()

@Serializable
@SerialName("recommended")
class RecommendedPosts(
    val strapline: String,
    val posts: List<Story<Post>>
) : Component()
