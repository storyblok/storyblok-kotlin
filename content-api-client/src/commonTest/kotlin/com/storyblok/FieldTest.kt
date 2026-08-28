package com.storyblok

import com.storyblok.cdn.schema.Asset
import com.storyblok.cdn.schema.Field
import com.storyblok.cdn.schema.Link
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FieldTest {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    @Test
    fun `a multilink decodes through the fieldtype discriminator`() {
        val field = json.decodeFromString<Field>(
            """
            {"id":"1cf5…","url":"https://example.com","linktype":"url","fieldtype":"multilink",
             "cached_url":"https://example.com","target":"_blank"}
            """.trimIndent(),
        )

        val link = assertIs<Link>(field)
        assertEquals("url", link.linkType)
        assertEquals("https://example.com", link.cachedUrl)
        assertEquals("multilink", link.fieldType)
    }

    @Test
    fun `an asset decodes through the fieldtype discriminator`() {
        val field = json.decodeFromString<Field>(
            """
            {"alt":"A ship","name":"ship","focus":null,"title":"Ship","source":null,
             "filename":"https://a.storyblok.com/f/1/ship.jpg","copyright":null,
             "fieldtype":"asset","meta_data":{"tag":"space"},"is_external_url":false}
            """.trimIndent(),
        )

        val asset = assertIs<Asset>(field)
        assertEquals("https://a.storyblok.com/f/1/ship.jpg", asset.filename)
        assertEquals(mapOf("tag" to "space"), asset.metadata)
        assertEquals(false, asset.isExternalUrl)
        assertEquals("asset", asset.fieldType)
    }
}
