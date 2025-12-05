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

class IdeationRoom {

	/**
     * This endpoint is to create an Idea in the Ideation Room. In the request body, passing name in the idea object is a minimum requirement.
     * https://www.storyblok.com/docs/api/management/ideation-room/create-an-idea
     */
    @Test
    fun `Create an Idea`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/ideas") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"idea":{"assignee":null,"author":{"avatar":"avatars/67891/838dcb304c/avatar.jpg","friendly_name":"Jon Doe","id":67891,"userid":"test@email.com"},"bookmarks":[],"content":{},"deleted_at":null,"description":"First idea","internal_tag_ids":["12345"],"internal_tags_list":[{"id":12345,"name":"docs"}],"is_private":true,"name":"My first idea","status":"draft","stories":[],"story_ids":[]}}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows the deletion of an idea using the uuid.
     * https://www.storyblok.com/docs/api/management/ideation-room/delete-an-idea
     */
    @Test
    fun `Delete an Idea`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/ideas/123ab45c-6d78-9101-11ef-213gh1i4j1k5")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows restoring an idea using the uuid. Use deleted idea's id value for idea_id. This endpoint also restores the idea's discussion comments.
     * https://www.storyblok.com/docs/api/management/ideation-room/restore-an-idea
     */
    @Test
    fun `Restore an Idea`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/ideas/123ab45c-6d78-9101-11ef-213gh1i4j1k5") {
		    contentType(ContentType.Application.Json)
		    setBody("""undefined""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns discussions in an idea.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-discussions-in-idea
     */
    @Test
    fun `Retrieve Discussions in Idea`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/ideas/1a2b3456-c7d8-9ef1-gh01-11i2jk13l14m/discussions")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of idea objects.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-multiple-ideas
     */
    @Test
    fun `Retrieve Multiple Ideas`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/ideas/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single idea object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-one-idea
     */
    @Test
    fun `Retrieve One Idea`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/ideas/1a2b3456-c7d8-9ef1-gh01-11i2jk13l14m")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update an idea using an idea uuid. In the request body, it's required to pass the idea object.
     * https://www.storyblok.com/docs/api/management/ideation-room/update-an-idea
     */
    @Test
    fun `Update an Idea`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/ideas/ab123cd4-5e6f-7gh8-9ij1-01k112l13m1n") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"idea":{"assignee":null,"author":{"avatar":"avatars/67891/838dcb304c/avatar.jpg","friendly_name":"Jon Doe","id":67891,"userid":"test@email.com"},"bookmarks":[],"content":{},"deleted_at":null,"description":"First idea","internal_tag_ids":["12345"],"internal_tags_list":[{"id":12345,"name":"docs"}],"is_private":true,"name":"My first idea","status":"draft","stories":[],"story_ids":[]}}""")
		}
		
		println(response.body<JsonElement>())
    }

}