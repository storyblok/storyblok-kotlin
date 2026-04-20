package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import com.storyblok.compose.BlokScope
import com.storyblok.compose.BlokScopeImpl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

/**
 * Container for registered component composables and their serializers.
 *
 * Create instances using [blokProvider][com.storyblok.compose.provider.blokProvider] or [blokProviderWithoutRichText].
 */
public class BlokProvider internal constructor(
    internal val blokScope: BlokScope,
    /** The [SerializersModule] containing serializers for all registered [Component] types. */
    public val serializersModule: SerializersModule
)

/**
 * Creates a [BlokProvider] by registering component composables and serializers (without rich text support).
 *
 * @param fallback Composable rendered for unknown or unregistered components.
 * @param builder Configuration block for registering blok composables via [BlokProviderScope].
 */
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