package com.storyblok

import com.storyblok.cdn.EditorContext
import com.storyblok.cdn.displayName
import com.storyblok.cdn.humanize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

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

    private fun message(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    @Test
    fun `entering edit mode gives the editor's version and display names`() {
        val context = EditorContext().after(
            message("""{"action": "enterEditmode", "storyId": 42, "appVersion": "v2", "componentNames": {"teaser": "Teaser Card"}}""")
        )

        assertEquals(EditorContext(appVersion = "v2", componentNames = mapOf("teaser" to "Teaser Card"), storyId = "42"), context)
    }

    @Test
    fun `other messages give only the story being edited`() {
        val context = EditorContext(appVersion = "v2", componentNames = mapOf("teaser" to "Teaser Card"))

        assertEquals(context.copy(storyId = "7"), context.after(message("""{"action": "hoverComponent", "storyId": "7"}""")))
        assertEquals(context, context.after(message("""{"action": "hoverComponent"}""")))
    }

    @Test
    fun `a menu keeps the block's breadcrumb as the editor sent it`() {
        val crumb = """{"_uid": "u3", "component": "teaser", "_parentindex": 2, "_parent": "u2"}"""
        val menu = EditorContext().menuOf(
            message("""{"action": "editedBlok", "blok": {"uid": "u3", "name": "teaser"},
                "breadcrumbs": [{"_uid": "page", "component": "page"}, $crumb]}"""),
            "u3",
        )

        assertEquals(message(crumb), menu?.target)
    }

    @Test
    fun `a menu whose last breadcrumb is another block's has nothing to send actions with`() {
        val menu = EditorContext().menuOf(
            message("""{"action": "editedBlok", "blok": {"uid": "u3", "name": "teaser"},
                "breadcrumbs": [{"_uid": "page", "component": "page"}, {"_uid": "u2", "component": "grid"}]}"""),
            "u3",
        )

        assertNull(menu?.target)
    }
}
