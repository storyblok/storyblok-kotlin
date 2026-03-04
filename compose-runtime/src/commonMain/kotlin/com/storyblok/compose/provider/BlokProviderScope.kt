package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.compose.BlokScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

public class BlokProviderScope internal constructor(
    blokScope: BlokScope,
    private val providers: MutableMap<KClass<*>, Provider>,
    private val polymorphicModuleBuilder: PolymorphicModuleBuilder<Component>
) : BlokScope by blokScope {

    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>, composable: @Composable (T) -> Unit) {
        require(type !in providers) { "A blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.Blok(composable)
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.ModifiableBlok(composable)
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    public fun <T : RichText> richText(type: KClass<T>, composable: @Composable (T) -> Unit) {
        require(type !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.RichText(composable)
    }

    public fun <T : RichText> richText(type: KClass<out T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.ModifiableRichText(composable)
    }

    public fun <T : RichText> defaultRichText(type: KClass<out T>, composable: @Composable (T) -> Unit) {
        providers.getOrPut(type) { Provider.RichText(composable) }
    }

    public inline fun <reified T : Component> blok(noinline composable: @Composable (T) -> Unit): Unit =
        blok(T::class, serializer<T>(), composable)

    public inline fun <reified T : Component> blok(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        blok(T::class, serializer<T>(), composable)

    public inline fun <reified T : RichText> richText(noinline composable: @Composable (T) -> Unit): Unit =
        richText(T::class, composable)

    public inline fun <reified T : RichText> richText(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        richText(T::class, composable)

    public inline fun <reified T : RichText> defaultRichText(noinline composable: @Composable (T) -> Unit) {
        defaultRichText(T::class, composable)
    }
}