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

/**
 * Scope for registering [Component] and [RichText] composables within a [BlokProvider].
 *
 * Also implements [BlokScope], allowing nested component rendering during registration.
 */
public class BlokProviderScope internal constructor(
    blokScope: BlokScope,
    private val providers: MutableMap<Any, Provider>,
    private val polymorphicModuleBuilder: PolymorphicModuleBuilder<Component>
) : BlokScope by blokScope {

    /**
     * Registers a headless component (serializer only, no composable).
     *
     * @param T The [Component] type to register.
     * @param type The class of the component.
     * @param serializer The serializer for the component.
     */
    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>) {
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    /**
     * Registers a component with its serializer and composable.
     *
     * @param T The [Component] type to register.
     * @param type The class of the component.
     * @param serializer The serializer for the component.
     * @param composable The composable used to render the component.
     */
    public fun <T : Component> blok(type: KClass<T>, serializer: KSerializer<T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.Blok(composable)
        polymorphicModuleBuilder.subclass(type, serializer)
    }

    /**
     * Registers a composable for a [RichText] node type.
     *
     * @param T The [RichText] type to register.
     * @param type The class of the rich text node.
     * @param composable The composable used to render the node.
     */
    public fun <T : RichText> richText(type: KClass<T>, composable: @Composable (T, Modifier) -> Unit) {
        require(type !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type] = Provider.RichText(composable)
    }

    /**
     * Registers an [AnnotatedString.Builder] handler for a [RichText] node type.
     *
     * @param T The [RichText] type to register.
     * @param type The class of the rich text node.
     * @param builder The builder function appending rich text content to an [AnnotatedString].
     */
    @JvmName("richTextWithinAnnotatedStringBuilder")
    public fun <T : RichText> richText(type: KClass<out T>, builder: @Composable AnnotatedString.Builder.(T) -> Unit) {
        require((type to AnnotatedString::class) !in providers) { "A rich text blok for ${type.simpleName} has already been registered" }
        providers[type to AnnotatedString::class] = Provider.RichTextWithinAnnotatedStringBuilder(builder)
    }

    /**
     * Registers a default composable for a [RichText] node type, only if not already registered.
     *
     * @param T The [RichText] type to register.
     * @param type The class of the rich text node.
     * @param composable The composable used to render the node.
     */
    public fun <T : RichText> defaultRichText(type: KClass<out T>, composable: @Composable (T, Modifier) -> Unit) {
        providers.getOrPut(type) { Provider.RichText(composable) }
    }

    /**
     * Registers a default [AnnotatedString.Builder] handler for a [RichText] node type, only if not already registered.
     *
     * @param T The [RichText] type to register.
     * @param type The class of the rich text node.
     * @param builder The builder function appending rich text content to an [AnnotatedString].
     */
    @JvmName("defaultRichTextWithinAnnotatedStringBuilder")
    public fun <T : RichText> defaultRichText(type: KClass<out T>, builder: @Composable AnnotatedString.Builder.(T) -> Unit) {
        providers.getOrPut(type to AnnotatedString::class) { Provider.RichTextWithinAnnotatedStringBuilder(builder) }
    }

    /**
     * Registers a headless component using reified type information.
     *
     * @param T The [Component] type to register.
     */
    public inline fun <reified T : Component> blok(): Unit = blok(T::class, serializer<T>())

    /**
     * Registers a component with its composable using reified type information.
     *
     * @param T The [Component] type to register.
     * @param composable The composable used to render the component.
     */
    public inline fun <reified T : Component> blok(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        blok(T::class, serializer<T>(), composable)

    /**
     * Registers a rich text composable using reified type information.
     *
     * @param T The [RichText] type to register.
     * @param composable The composable used to render the node.
     */
    public inline fun <reified T : RichText> richText(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        richText(T::class, composable)

    /**
     * Registers an [AnnotatedString.Builder] handler for a rich text node using reified type information.
     *
     * @param T The [RichText] type to register.
     * @param builder The builder function appending rich text content to an [AnnotatedString].
     */
    @JvmName("richTextWithAnnotatedStringBuilder")
    public inline fun <reified T : RichText> richText(noinline builder: @Composable AnnotatedString.Builder.(T) -> Unit): Unit =
        richText(T::class, builder)

    /**
     * Registers a default rich text composable using reified type information.
     *
     * @param T The [RichText] type to register.
     * @param composable The composable used to render the node.
     */
    public inline fun <reified T : RichText> defaultRichText(noinline composable: @Composable (T, Modifier) -> Unit): Unit =
        defaultRichText(T::class, composable)

    /**
     * Registers a default [AnnotatedString.Builder] handler for a rich text node using reified type information.
     *
     * @param T The [RichText] type to register.
     * @param builder The builder function appending rich text content to an [AnnotatedString].
     */
    @JvmName("defaultRichTextWithAnnotatedStringBuilder")
    public inline fun <reified T : RichText> defaultRichText(noinline builder: @Composable AnnotatedString.Builder.(T) -> Unit): Unit =
        defaultRichText(T::class, builder)


}