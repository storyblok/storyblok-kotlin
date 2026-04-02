package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import com.storyblok.compose.BlokScope
import com.storyblok.compose.BlokScopeImpl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

public class BlokProvider internal constructor(
    internal val blokScope: BlokScope,
    public val serializersModule: SerializersModule
)

public fun blokProviderWithoutRichText(
    fallback: @Composable (unknownComponent: Component, Modifier) -> Unit = { it, _ -> throw IllegalStateException("Unknown component ${it.component}") },
    builder: BlokProviderScope.() -> Unit,
): BlokProvider {
    val providers = mutableMapOf<Any, Provider>(Component.Unknown::class to Provider.Blok(fallback))
    val blokScope = BlokScopeImpl(providers)
    return BlokProvider(
        blokScope,
        SerializersModule {
            polymorphic(Component::class) {
                BlokProviderScope(blokScope, providers, this)
                    .apply { builder() }
            }
        }
    )
}