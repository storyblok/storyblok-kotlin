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

class Discussions {

	/**
     * This endpoint allows the creation of a comment in a particular discussion using the ID.
     * https://www.storyblok.com/docs/api/management/discussions/create-a-comment
     */
    @Test
    fun `Create a Comment`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/discussions/456/comments") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "comment": {
		        "message_json": [
		          {
		            "text": "Hello new comment",
		            "type": "text"
		          }
		        ]
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint creates a new discussion.
     * https://www.storyblok.com/docs/api/management/discussions/create-a-discussion
     */
    @Test
    fun `Create a Discussion`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/stories/12367/discussions") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"discussion":{"block_uid":"f7bd92e3-b309-4441-a8a0-654e499fefc8","comment":{"message_json":[{"text":"this is a comment ","type":"text"},{"attrs":{"id":99734,"label":"Fortune Ikechi"},"type":"mention"}]},"component":"feature","fieldname":"name","lang":"default","title":"Name"}}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows deletion of a comment using the numeric ID.
     * https://www.storyblok.com/docs/api/management/discussions/delete-a-comment
     */
    @Test
    fun `Delete a Comment`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/606/discussions/456/comments/789")
		
		println(response.body<JsonElement>())
    }

	/**
     * Resolves a comment in a discussion.
     * https://www.storyblok.com/docs/api/management/discussions/resolve-a-discussion
     */
    @Test
    fun `Resolve a Discussion`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/discussions/49468") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "discussion": {
		        "solved_at": "2024-02-06T22:07:04.729Z"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Get a specific discussion.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-a-specific-discussion
     */
    @Test
    fun `Retrieve a Specific Discussion`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/discussions/49473")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns comments for specific idea discussions from the Ideation Room.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-idea-discussions-comments
     */
    @Test
    fun `Retrieve Idea Discussions Comments`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/ideas/1a2b3456-c7d8-9ef1-gh01-11i2jk13l14m/discussions")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve multiple comments from a specific discussion
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-multiple-comments-from-a-specific-discussion
     */
    @Test
    fun `Retrieve Multiple Comments from a Specific Discussion`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/discussions/49471/comments")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of discussion objects present inside a particular story. This endpoint is paged and can be filtered by using page=1 , status and per_page=1 for retrieving discussions per page.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-multiple-discussions
     */
    @Test
    fun `Retrieve Multiple Discussions`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/1234/discussions") {
		    url {
		        parameters.append("per_page", "1")
		        parameters.append("page", "1")
		        parameters.append("by_status", "unsolved")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve your mentioned discussions. The response is paged.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-my-discussions
     */
    @Test
    fun `Retrieve My Discussions`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/mentioned_discussions/me")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update comments in a particular discussion using the discussion ID and comment ID
     * https://www.storyblok.com/docs/api/management/discussions/update-a-comment
     */
    @Test
    fun `Update a Comment`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/discussions/2345/comments/456") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "comment": {
		        "message_json": [
		          {
		            "text": "Updated Comment ",
		            "type": "text"
		          }
		        ]
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}