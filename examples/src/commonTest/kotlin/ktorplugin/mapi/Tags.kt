package ktorplugin.mapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test

class Tags {

	/**
     * You can create a tag, and optionally add it to a story.
     * https://www.storyblok.com/docs/api/management/tags/create-a-tag
     */
    @Test
    fun `Create a Tag`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/288868932106293/tags") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "tag": {
		        "name": "Editor's Choice",
		        "story_id": 202
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a tag from a space.
     * https://www.storyblok.com/docs/api/management/tags/delete-a-tag
     */
    @Test
    fun `Delete a Tag`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/288868932106293/stories/2141")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of tag objects from a space.
     * https://www.storyblok.com/docs/api/management/tags/retrieve-multiple-tags
     */
    @Test
    fun `Retrieve Multiple Tags`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/288868932106293/tags/") {
		    url {
		        parameters.append("search", "article")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of tag objects from a space.
     * https://www.storyblok.com/docs/api/management/tags/retrieve-multiple-tags
     */
    @Test
    fun `Retrieve Multiple Tags 2`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/288868932106293/stories/") {
		    url {
		        parameters.append("all_tags", "true")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint is used to add a tag to multiple stories at once.
     * https://www.storyblok.com/docs/api/management/tags/tag-bulk-association
     */
    @Test
    fun `Tag Bulk Association`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/288868932106293/tags/bulk_association") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "tags": {
		        "stories": [
		          {
		            "story_id": 69934114531566,
		            "tag_list": [
		              "Editor's Choice",
		              "Featured"
		            ]
		          }
		        ]
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to edit the name of a tag.
     * https://www.storyblok.com/docs/api/management/tags/update-a-tag
     */
    @Test
    fun `Update a Tag`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/288868932106293/stories/2141") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "id": "Editor's Choice",
		      "tag": {
		        "name": "Editorial"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}