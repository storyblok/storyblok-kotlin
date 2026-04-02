@file:OptIn(ExperimentalUuidApi::class)

package com.example.jetnews

import androidx.navigation3.runtime.NavKey
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class StoryKey(
    @Transient
    val story: Story<out Component>? = null,
    @EncodeDefault
    val uuid: Uuid? = story?.uuid,
    @EncodeDefault
    val slug: String? = story?.slug,
) : NavKey

val HomeKey = StoryKey(slug = "home")
