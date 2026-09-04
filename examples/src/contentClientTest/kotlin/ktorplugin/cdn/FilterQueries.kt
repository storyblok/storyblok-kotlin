package ktorplugin.cdn

import com.storyblok.ktor.Api.Config.Version.*
import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.query.Is.*
import kotlin.time.Instant
import com.storyblok.cdn.stories
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.paging.asItemSnapshotListFlow
import kotlin.test.Test

class FilterQueries {

    /**
     * Learn how to use filter queries with field-level translation in Storyblok by extending field keys with i18n and language codes for multilingual content filtering.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/field-level-translation
     */
    @Test
    fun `Filter Queries with Field-level Translation`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
            language = "es-co",
        )
        
        val stories = client
            .stories {
                parameter("filter_query[headline__i18n__es_co][in]", "Sinfonía de la Tierra: Navegar por las maravillas y los desafíos de nuestro oasis azul")
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Use filter queries to target nestable bloks and fields.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/nested-blocks-and-fields
     */
    @Test
    fun `Filter Queries with Nestable Blocks and Fields`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                parameter("filter_query[body.0.name][in]", "This is a nested blok")
            }
            .flow
            .asItemSnapshotListFlow()
            .first()

        println(stories)
    }

    /**
     * Filter stories by checking if a field contains all of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-all-in-array
     */
    @Test
    fun all_in_array() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::topics allIn listOf("solar-system", "space-exploration")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter stories by checking if a field contains any of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-any-in-array
     */
    @Test
    fun any_in_array() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::topics anyIn listOf("solar-system", "space-exploration")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a date field value greater than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-date
     */
    @Test
    fun gt_date() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::scheduled greaterThan Instant.parse("2023-12-31T09:00:00Z")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a float field value greater than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-float
     */
    @Test
    fun gt_float() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                filter {
                    Product::price greaterThan 1199.99
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with an integer field value greater than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-int
     */
    @Test
    fun gt_int() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
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
    }

    /**
     * Filter to return stories with a field value matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-in
     */
    @Test
    fun `in`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::categories isIn listOf("space-exploration", "solar-system")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a field value of a specific type.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-is
     */
    @Test
    fun `is`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::categories `is` NotEmptyArray
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a field value matching a specific pattern.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-like
     */
    @Test
    fun like() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::headline like "*space*"
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a date field value less than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-date
     */
    @Test
    fun lt_date() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::scheduled lessThan Instant.parse("2023-12-31T09:00:00Z")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a float field value less than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-float
     */
    @Test
    fun lt_float() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                filter {
                    Product::price lessThan 1199.99
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with an integer field value less than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-int
     */
    @Test
    fun lt_int() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                filter {
                    Product::price lessThan 1200L
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a field value not matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-in
     */
    @Test
    fun not_in() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::categories notIn listOf("space-exploration", "culture")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Filter to return stories with a field value not matching any of the provided patterns.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-like
     */
    @Test
    fun not_like() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                filter {
                    Article::headline notLike "*Mysteries*"
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Example showing how to filter stories by boolean field values using the 'in' operation in Storyblok's filter queries.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-a-boolean-value
     */
    @Test
    fun `Filtering Stories by a Boolean Value`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                startsWith = "articles/"
                filter {
                    Article::highlighted isIn listOf("true")
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Learn how to filter stories within a specific value range using gt_float and lt_float for price filtering and similar use cases.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-defining-a-value-range
     */
    @Test
    fun `Filtering Stories by Defining a Value Range`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Product> {
                startsWith = "products/"
                filter {
                    Product::price lessThan 300.0
                    Product::price greaterThan 100.0
                }
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

}