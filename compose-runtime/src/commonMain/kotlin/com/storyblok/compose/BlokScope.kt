package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.compose.provider.Provider
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

public interface BlokScope {
    @Composable
    public fun Blok(content: Component)
    @Composable
    public fun Blok(content: Component, modifier: Modifier)
    @Composable
    public fun RichText(content: RichText)
    @Composable
    public fun RichText(content: RichText, modifier: Modifier)
}

@JvmInline
internal value class BlokScopeImpl internal constructor(val providers: Map<KClass<*>, Provider>) : BlokScope {
    @Composable
    override fun Blok(content: Component) {
        providers.getOrElse(content::class) { error("No blok registered for ${content.component}") }
            .invoke(content)
    }

    @Composable
    override fun Blok(content: Component, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No blok registered for ${content.component}") }
            .invoke(content, modifier)

    @Composable
    override fun RichText(content: RichText) =
        providers.getOrElse(content::class) { error("No rich text blok registered for ${content.type}") }
            .invoke(content)

    @Composable
    override fun RichText(content: RichText, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No rich text blok registered for ${content.type}") }
            .invoke(content, modifier)

}