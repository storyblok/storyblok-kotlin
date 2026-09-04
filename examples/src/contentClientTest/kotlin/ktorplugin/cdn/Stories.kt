package ktorplugin.cdn

import com.storyblok.ktor.Api.Config.Version.*
import com.storyblok.cdn.StoryblokClient
import com.storyblok.cdn.schema.Story
import kotlin.uuid.Uuid
import com.storyblok.cdn.stories
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.first
import androidx.paging.asItemSnapshotListFlow
import kotlin.test.Test

class Stories {

    /**
     * Retrieve a single story by full slug, ID, or UUID using the Content Delivery API. Includes parameters for resolving links and relations.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-a-single-story
     */
    @Test
    fun `Retrieve a Single Story`() = runTest {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val story = client
            .story("posts/my-third-post")
            .last()
        
        println(story)
    }

    /**
     * Retrieve multiple stories from Storyblok using the Content Delivery API with filtering, pagination, sorting, and relation resolution options.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                startsWith = "articles"
            }
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Example showing how to retrieve a version of a story from a specific release by using the from_release query parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-an-edited-version-of-a-story-from-a-release
     */
    @Test
    fun `Retrieving a Story from a Specific Release`() = runTest {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Draft,
            cv = "1765990908",
        )
        
        val story = client
            .story("home") {
                fromRelease = "124105888551306"
            }
            .last()
        
        println(story)
    }

    /**
     * Example showing how to retrieve localized story versions using UUID and language parameters in the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-localized-stories-by-uuid
     */
    @Test
    fun `Retrieving Localized Stories by UUID`() = runTest {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
            language = "de",
        )
        
        val story = client
            .story(Uuid.parse("660452d2-1a68-4493-b5b6-2f03b6fa722b"))
            .last()
        
        println(story)
    }

    /**
     * Learn how to retrieve stories from specific folders using the starts_with parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-from-a-folder
     */
    @Test
    fun `Retrieving Stories from a Folder`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Draft,
        )
        
        val stories = client
            .stories {
                startsWith = "articles/"
            }
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Example demonstrating how to retrieve translated story versions using the language parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-in-a-particular-language
     */
    @Test
    fun `Retrieving Stories in a Particular Language`() = runTest {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
            language = "de",
        )
        
        val story = client
            .story("articles/earths-symphony-navigating-wonders-challenges-blue-oasis")
            .last()
        
        println(story)
    }

    /**
     * Learn how to resolve referenced stories using the resolve_relations parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-with-resolved-relations
     */
    @Test
    fun `Retrieving Stories with Resolved Relations`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                parameter("resolve_relations", "article.categories,article.author")
            }
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Example showing how to sort stories by custom fields defined in your story type schema using the sort_by parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-fields-associated-with-a-story-type
     */
    @Test
    fun `Sorting by Fields Associated with a Story Type`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories<Article> {
                sortBy(Article::headline)
            }
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

    /**
     * Learn how to sort stories by default story properties like name, position, and publication dates using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-story-object-property
     */
    @Test
    fun `Sorting by Story Object Property`() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.0")
        val client = StoryblokClient(
            accessToken = "krcV6QGxWORpYLUWt12xKQtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                sortByDescending(Story<*>::firstPublishedAt)
            }
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

}