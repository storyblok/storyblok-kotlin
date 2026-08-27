package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import com.storyblok.compose.BlockScope
import com.storyblok.compose.BlockScopeImpl
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

/**
 * Container for registered component composables and their serializers.
 *
 * Create instances using [blockProvider][com.storyblok.compose.provider.blockProvider] or [blockProviderWithoutRichText].
 */
@Immutable
public class BlockProvider internal constructor(
    public val blockScope: BlockScope,
    /** The [SerializersModule] containing serializers for all registered [Component] types. */
    public val serializersModule: SerializersModule
) {
    /** @suppress */
    @Deprecated(
        message = "Renamed to blockScope.",
        replaceWith = ReplaceWith("blockScope"),
        level = DeprecationLevel.WARNING,
    )
    public val blokScope: BlockScope get() = blockScope
}

/**
 * Creates a [BlockProvider] by registering component composables and serializers (without rich text support).
 *
 * @param fallback Composable rendered for unknown or unregistered components.
 * @param builder Configuration block for registering block composables via [BlockProviderScope].
 */
public fun blockProviderWithoutRichText(
    fallback: @Composable (unknownComponent: Component, Modifier) -> Unit = { it, _ -> throw IllegalStateException("Unknown component ${it.component}") },
    builder: BlockProviderScope.() -> Unit,
): BlockProvider {
    val providers = mutableMapOf<Any, Provider>(Component.Unknown::class to Provider.Block(fallback))
    val blockScope = BlockScopeImpl(providers)
    return BlockProvider(
        blockScope,
        SerializersModule {
            polymorphic(Component::class) {
                BlockProviderScope(blockScope, providers, this)
                    .apply { builder() }
            }
        }
    )
}

/** @suppress */
@Deprecated(
    message = "Renamed to BlockProvider.",
    replaceWith = ReplaceWith("BlockProvider"),
    level = DeprecationLevel.WARNING,
)
public typealias BlokProvider = BlockProvider

/** @suppress */
@Deprecated(
    message = "Renamed to blockProviderWithoutRichText.",
    replaceWith = ReplaceWith("blockProviderWithoutRichText(fallback, builder)"),
    level = DeprecationLevel.WARNING,
)
public fun blokProviderWithoutRichText(
    fallback: @Composable (unknownComponent: Component, Modifier) -> Unit = { it, _ -> throw IllegalStateException("Unknown component ${it.component}") },
    builder: BlockProviderScope.() -> Unit,
): BlockProvider = blockProviderWithoutRichText(fallback, builder)
