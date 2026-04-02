package com.storyblok.compose.provider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.compose.BlokScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.serializer
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

public class BlokProviderScope internal constructor(
    blokScope: BlokScope,
    private val providers: MutableMap<Any, Provider>,
    private val polymorphicModuleBuilder: PolymorphicModuleBuilder<Component>
) : BlokScope by blokScope {

    /**
     * Registers headless a blok.
     */
    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>) {
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.Blok(composable)
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    public fun <T : RichText> richText(type: KClass<T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.RichText(composable)
    }

    @JvmName("richTextWithinAnnotatedStringBuilder")
    public fun <T : RichText> richText(type: KClass<out T>, builder: @Composable AnnotatedString.Builder.(T) -> Unit) {
        require((type to AnnotatedString::class) !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type to AnnotatedString::class] = Provider.RichTextWithinAnnotatedStringBuilder(builder)
    }

    public fun <T : RichText> defaultRichText(type: KClass<out T>, composable: @Composable (T, Modifier) -> Unit) {
        providers.getOrPut(type) { Provider.RichText(composable) }
    }

    @JvmName("defaultRichTextWithinAnnotatedStringBuilder")
    public fun <T : RichText> defaultRichText(type: KClass<out T>, builder: @Composable AnnotatedString.Builder.(T) -> Unit) {
        providers.getOrPut(type to AnnotatedString::class) { Provider.RichTextWithinAnnotatedStringBuilder(builder) }
    }

    public inline fun <reified T : Component> blok(): Unit = blok(T::class, serializer<T>())

    public inline fun <reified T : Component> blok(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        blok(T::class, serializer<T>(), composable)

    public inline fun <reified T : RichText> richText(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        richText(T::class, composable)

    @JvmName("richTextWithAnnotatedStringBuilder")
    public inline fun <reified T : RichText> richText(noinline builder: @Composable AnnotatedString.Builder.(T) -> Unit): Unit =
        richText(T::class, builder)

    public inline fun <reified T : RichText> defaultRichText(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        defaultRichText(T::class, composable)

    @JvmName("defaultRichTextWithAnnotatedStringBuilder")
    public inline fun <reified T : RichText> defaultRichText(noinline builder: @Composable AnnotatedString.Builder.(T) -> Unit): Unit =
        defaultRichText(T::class, builder)


}