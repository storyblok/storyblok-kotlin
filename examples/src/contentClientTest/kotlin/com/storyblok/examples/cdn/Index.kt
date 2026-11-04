package com.storyblok.examples.cdn

import com.storyblok.ktor.Api.Config.Version.*
import com.storyblok.cdn.StoryblokClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.paging.asItemSnapshotListFlow
import kotlin.test.Test
import kotlin.test.assertTrue

class Index {

    /**
     * Learn the basics of the Storyblok Content Delivery API, including authentication, caching, CDN, pagination, rate limits, and error handling.
     * https://www.storyblok.com/docs/api/content-delivery/v2/index
     */
    @Test
    fun `Introduction to the Content Delivery API`() = runBlocking {

        val client = StoryblokClient(
            accessToken = "ask9soUkv02QqbZgmZdeDAtt",
            version = Published,
        )
        
        val stories = client
            .stories()
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
        assertTrue(stories.isNotEmpty(), "Expected the example to retrieve stories")
    }

}