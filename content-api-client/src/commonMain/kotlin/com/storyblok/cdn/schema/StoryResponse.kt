package com.storyblok.cdn.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class StoryResponse<T : Component>(
    val story: Story<T>,
    val cv: Long,
    @SerialName("rels")
    val relations: List<String>,
    val links: List<String>
)
