package ktorplugin.cdn

import com.storyblok.ktor.Api.Config.Version.*
import com.storyblok.cdn.StoryblokClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.paging.asItemSnapshotListFlow
import kotlin.test.Test

class Index {

    /**
     * Learn the basics of the Storyblok Content Delivery API, including authentication, caching, CDN, pagination, rate limits, and error handling.
     * https://www.storyblok.com/docs/api/content-delivery/v2/index
     */
    @Test
    fun Introduction() = runBlocking {

        // implementation("com.storyblok:content-api-client:0.5.1")
        val client = StoryblokClient(
            accessToken = "wANpEQEsMYGOwLxwXQ76Ggtt",
            version = Published,
        )
        
        val stories = client
            .stories()
            .flow
            .asItemSnapshotListFlow()
            .first()
        
        println(stories)
    }

}