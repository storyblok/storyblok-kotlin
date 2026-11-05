package com.storyblok

import com.storyblok.cdn.displayName
import com.storyblok.cdn.humanize
import kotlin.test.Test
import kotlin.test.assertEquals

/** How the selected block's menu labels blocks, as `@storyblok/preview-bridge` labels them. */
class BlockMenuTest {

    @Test
    fun `a display name is used as given`() {
        assertEquals("Teaser Card", displayName("teaser", mapOf("teaser" to "Teaser Card")))
    }

    @Test
    fun `a block with no display name is labelled with its name made readable`() {
        assertEquals("Teaser", displayName("teaser", mapOf("teaser" to "")))
        assertEquals("Teaser", displayName("teaser", emptyMap()))
    }

    @Test
    fun `snake case and kebab case names are split into words`() {
        assertEquals("Hero Section", humanize("hero_section"))
        assertEquals("Hero Section", humanize("hero-section"))
    }

    @Test
    fun `camel case names are split into words`() {
        assertEquals("Hero Section", humanize("heroSection"))
        assertEquals("Hero Section", humanize("HeroSection"))
    }

    @Test
    fun `an empty name is an empty label`() {
        assertEquals("", humanize(""))
    }
}
