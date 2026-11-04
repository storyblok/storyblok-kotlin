package com.storyblok.examples.cdn

import com.storyblok.ktor.Api.Config.Version.*
import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.query.Is.*
import kotlin.time.Instant
import com.storyblok.cdn.stories
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.paging.asItemSnapshotListFlow
import kotlin.test.Test
import kotlin.test.assertTrue

class FilterQueries {

    /**
     * Learn how to use filter queries with field-level translation in Storyblok by extending field keys with i18n and language codes for multilingual content filtering.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/field-level-translation
     */
    @Test
    fun `Filter Queries with Field-level Translation`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
            language = "de",
        )
        
        val stories = client
            .stories {
                parameter("filter_query[description__i18n__de][like]", "Deutsches*")
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Use filter queries to target nestable bloks and fields.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/nested-blocks-and-fields
     */
    @Test
    fun `Filter Queries with Nestable Blocks and Fields`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                parameter("filter_query[body.0.headline][in]", "Hello Earth!")
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter stories by checking if a field contains all of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-all-in-array
     */
    @Test
    fun all_in_array() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::categories allIn listOf("9aa72a2f-04ae-48df-b71f-25f53044dc97", "84550816-245d-4fe6-8ae8-b633d4a328f4")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter stories by checking if a field contains any of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-any-in-array
     */
    @Test
    fun any_in_array() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::categories anyIn listOf("9aa72a2f-04ae-48df-b71f-25f53044dc97", "84550816-245d-4fe6-8ae8-b633d4a328f4")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a date field value greater than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-date
     */
    @Test
    fun gt_date() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::schedule greaterThan Instant.parse("2026-10-15T09:00:00Z")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a float field value greater than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-float
     */
    @Test
    fun gt_float() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                filter {
                    Product::price greaterThan 300.99
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with an integer field value greater than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-int
     */
    @Test
    fun gt_int() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                filter {
                    Product::price greaterThan 1200L
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a field value matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-in
     */
    @Test
    fun `in`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::topics isIn listOf("Design", "Finance")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a field value of a specific type.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-is
     */
    @Test
    fun `is`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::categories `is` NotEmptyArray
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a field value matching a specific pattern.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-like
     */
    @Test
    fun like() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                parameter("filter_query[name][like]", "*Space*")
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a date field value less than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-date
     */
    @Test
    fun lt_date() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::schedule lessThan Instant.parse("2026-10-13T12:00:00Z")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a float field value less than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-float
     */
    @Test
    fun lt_float() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                startsWith = "products"
                filter {
                    Product::price lessThan 2.24
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with an integer field value less than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-int
     */
    @Test
    fun lt_int() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                startsWith = "products/"
                filter {
                    Product::price lessThan 20L
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a field value not matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-in
     */
    @Test
    fun not_in() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                filter {
                    Post::topics notIn listOf("Design", "Finance")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Filter to return stories with a field value not matching any of the provided patterns.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-like
     */
    @Test
    fun not_like() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                startsWith = "posts"
                filter {
                    Post::description notLike "*second*"
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Example showing how to filter stories by boolean field values using the 'in' operation in Storyblok's filter queries.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-a-boolean-value
     */
    @Test
    fun `Filtering Stories by a Boolean Value`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                startsWith = "posts"
                filter {
                    Post::highlighted isIn listOf("true")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Learn how to filter stories within a specific value range using gt_float and lt_float for price filtering and similar use cases.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-defining-a-value-range
     */
    @Test
    fun `Filtering Stories by Defining a Value Range`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                startsWith = "products"
                filter {
                    Product::price lessThan 300.0
                    Product::price greaterThan 200.0
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

}