package com.example.jetnews

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
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

/**
 * How the back stack is saved and restored.
 *
 * On Android, Navigation 3 finds the serializer for a key by reflection. iOS and web have no
 * reflection, so every [NavKey] the app can navigate to has to be registered as a polymorphic
 * subclass up front.
 */
val NavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(StoryKey::class, StoryKey.serializer())
        }
    }
}
