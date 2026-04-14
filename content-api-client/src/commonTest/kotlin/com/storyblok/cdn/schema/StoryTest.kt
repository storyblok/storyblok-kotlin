@file:OptIn(ExperimentalUuidApi::class)

package com.storyblok.cdn.schema

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class StoryTest {

    @Serializable
    @SerialName("page")
    data class PageContent(val title: String = "") : Component()

    @Test
    fun `Story deserializes from JSON with example values from OpenAPI spec`() {
        val jsonString = """
            {
                "id": 1,
                "uuid": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Home",
                "content": {
                    "_uid": "54bac0c7-bf25-46d0-ba66-a0ea51091a8d",
                    "component": "page"
                },
                "slug": "home",
                "full_slug": "home",
                "created_at": "2025-07-09T14:35:26.851Z",
                "published_at": "2025-07-09T14:35:26.851Z",
                "first_published_at": "2025-07-09T14:35:26.851Z",
                "updated_at": "2025-07-09T14:35:26.851Z",
                "sort_by_date": "2025-07-09",
                "position": 1,
                "tag_list": ["home"],
                "is_startpage": true,
                "parent_id": 1,
                "meta_data": null,
                "group_id": "57350688-5a28-49d1-b5a9-086ae0d4c0d2",
                "release_id": 1,
                "lang": "default",
                "path": "/home",
                "alternates": [
                    {
                        "id": 12345,
                        "name": "Home ES",
                        "slug": "home-es",
                        "published": true,
                        "full_slug": "home-es",
                        "is_folder": false,
                        "parent_id": 0
                    }
                ],
                "default_full_slug": "home/",
                "translated_slugs": [
                    {
                        "path": "library/",
                        "name": "library",
                        "lang": "es",
                        "published": true
                    }
                ]
            }
        """.trimIndent()

        val story = Json.decodeFromString<Story<PageContent>>(jsonString)

        assertEquals(1L, story.id)
        assertEquals("123e4567-e89b-12d3-a456-426614174000", story.uuid.toString())
        assertEquals("Home", story.name)
        assertEquals("54bac0c7-bf25-46d0-ba66-a0ea51091a8d", story.content.uid)
        assertEquals("page", story.content.component)
        assertEquals("home", story.slug)
        assertEquals("home", story.fullSlug)

        // Verify date fields
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), story.createdAt)
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), story.publishedAt)
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), story.firstPublishedAt)
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), story.updatedAt)
        assertEquals(LocalDate.parse("2025-07-09"), story.sortByDate)

        assertEquals(1, story.position)
        assertEquals(listOf("home"), story.tagList)
        assertEquals(true, story.isStartPage)
        assertEquals(1L, story.parentId)
        assertEquals(null, story.metadata)
        assertEquals("57350688-5a28-49d1-b5a9-086ae0d4c0d2", story.groupId.toString())
        assertEquals(1L, story.releaseId)
        assertEquals("default", story.language)
        assertEquals("/home", story.path)
        assertEquals("home/", story.defaultFullSlug)

        // Verify alternates
        assertEquals(1, story.alternates.size)
        val alternate = story.alternates[0]
        assertEquals(12345L, alternate.id)
        assertEquals("Home ES", alternate.name)
        assertEquals("home-es", alternate.slug)
        assertEquals(true, alternate.published)
        assertEquals("home-es", alternate.fullSlug)
        assertEquals(false, alternate.isFolder)
        assertEquals(0L, alternate.parentId)

        // Verify translated slugs
        assertEquals(1, story.translatedSlugs?.size)
        val translatedSlug = story.translatedSlugs!![0]
        assertEquals("library/", translatedSlug.path)
        assertEquals("library", translatedSlug.name)
        assertEquals("es", translatedSlug.language)
        assertEquals(true, translatedSlug.published)
    }

    @Test
    fun `Story deserializes from JSON with null optional fields`() {
        val jsonString = """
            {
                "id": 1,
                "uuid": "123e4567-e89b-12d3-a456-426614174000",
                "name": "Home",
                "content": {
                    "_uid": "54bac0c7-bf25-46d0-ba66-a0ea51091a8d",
                    "component": "page"
                },
                "slug": "home",
                "full_slug": "home",
                "created_at": "2025-07-09T14:35:26.851Z",
                "published_at": null,
                "first_published_at": null,
                "updated_at": null,
                "sort_by_date": null,
                "position": 1,
                "tag_list": [],
                "is_startpage": false,
                "parent_id": null,
                "meta_data": null,
                "group_id": "57350688-5a28-49d1-b5a9-086ae0d4c0d2",
                "release_id": null,
                "lang": "default",
                "path": null,
                "alternates": [],
                "default_full_slug": null,
                "translated_slugs": null
            }
        """.trimIndent()

        val story = Json.decodeFromString<Story<PageContent>>(jsonString)

        assertEquals(1L, story.id)
        assertEquals("Home", story.name)
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), story.createdAt)
        assertEquals(null, story.publishedAt)
        assertEquals(null, story.firstPublishedAt)
        assertEquals(null, story.updatedAt)
        assertEquals(null, story.sortByDate)
        assertEquals(emptyList(), story.tagList)
        assertEquals(false, story.isStartPage)
        assertEquals(null, story.parentId)
        assertEquals(null, story.metadata)
        assertEquals(null, story.releaseId)
        assertEquals(null, story.path)
        assertEquals(emptyList(), story.alternates)
        assertEquals(null, story.defaultFullSlug)
        assertEquals(null, story.translatedSlugs)
    }
}


