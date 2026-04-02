package com.storyblok.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.RichText
import com.storyblok.compose.provider.Provider
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

public interface BlokScope {
    @Composable
    public fun Blok(content: Component, modifier: Modifier = Modifier)
    @Composable
    public fun RichText(content: RichText, modifier: Modifier = Modifier)
    public fun AnnotatedString.Builder.RichText(content: RichText)
}

@JvmInline
internal value class BlokScopeImpl internal constructor(val providers: Map<Any, Provider>) : BlokScope {

    @Composable
    override fun Blok(content: Component, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No blok registered for ${content.component}") }
            .invoke(content, modifier)

    @Composable
    override fun RichText(content: RichText, modifier: Modifier) =
        providers.getOrElse(content::class) { error("No rich text blok registered for ${content.type} accepting a Modifier") }
            .invoke(content, modifier)

    override fun AnnotatedString.Builder.RichText(content: RichText) =
        with(providers.getOrElse(content::class to AnnotatedString::class) { error("No rich text blok registered for ${content.type} accepting an AnnotatedString.Builder") }) {
            invoke(content)
        }
}