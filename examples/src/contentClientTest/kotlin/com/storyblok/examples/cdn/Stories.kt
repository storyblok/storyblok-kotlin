package com.storyblok.examples.cdn

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
import kotlin.test.assertTrue

class Stories {

    /**
     * Example showing how to retrieve a version of a story from a specific release by using the from_release query parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-an-edited-version-of-a-story-from-a-release
     */
    @Test
    fun `Retrieving a Story from a Specific Release`() = runTest {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Draft,
        )
        
        val story = client
            .story("home") {
                fromRelease = "230891299745176"
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

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
            language = "de-DE",
        )
        
        val story = client
            .story(Uuid.parse("bfea4895-8a19-4e82-ae1c-1c591dce3094"))
            .last()
        
        println(story)
    }

    /**
     * Learn how to retrieve stories from specific folders using the starts_with parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-from-a-folder
     */
    @Test
    fun `Retrieving Stories from a Folder`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Draft,
        )
        
        val stories = client
            .stories {
                startsWith = "posts"
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Example demonstrating how to retrieve translated story versions using the language parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-in-a-particular-language
     */
    @Test
    fun `Retrieving Stories in a Particular Language`() = runTest {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
            language = "de-DE",
        )
        
        val story = client
            .story("posts/my-first-post")
            .last()
        
        println(story)
    }

    /**
     * Learn how to resolve referenced stories using the resolve_relations parameter in Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/retrieving-stories-with-resolved-relations
     */
    @Test
    fun `Retrieving Stories with Resolved Relations`() = runTest {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val story = client
            .story("posts/my-first-post") {
                parameter("resolve_relations", "post.categories,post.author")
            }
            .last()
        
        println(story)
    }

    /**
     * Example showing how to sort stories by custom fields defined in your story type schema using the sort_by parameter.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-fields-associated-with-a-story-type
     */
    @Test
    fun `Sorting by Fields Associated with a Story Type`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories<Post> {
                sortBy(Post::topics)
                startsWith = "posts"
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

    /**
     * Learn how to sort stories by default story properties like name, position, and publication dates using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/stories/examples/sorting-by-story-object-property
     */
    @Test
    fun `Sorting by Story Object Property`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories {
                sortByDescending(Story<*>::firstPublishedAt)
                startsWith = "authors"
            }
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

}