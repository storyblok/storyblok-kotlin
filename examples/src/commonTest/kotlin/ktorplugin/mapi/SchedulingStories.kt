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

class SchedulingStories {

	/**
     * This endpoint allows you to create a new story schedule.
     */
    @Test
    fun `Create a Story Schedule`() = runTest {

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
		
		val response = client.post("spaces/606/story_schedulings") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "story_scheduling": {
		        "language": "pt-br",
		        "publish_at": "2024-07-26T06:56:00.000Z",
		        "story_id": 362419485
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a schedule by the numeric ID.
     */
    @Test
    fun `Delete a Story Schedule`() = runTest {

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
		
		val response = client.delete("spaces/606/story_schedulings/123/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of story schedule objects.
     */
    @Test
    fun `Retrieve Multiple Story Schedules`() = runTest {

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
		
		val response = client.get("spaces/606/story_schedulings")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single schedule object by providing a specific numeric ID.
     */
    @Test
    fun `Retrieve One Story Schedule`() = runTest {

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
		
		val response = client.get("spaces/606/story_schedulings/91")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a publishing schedule by the numeric ID.
     */
    @Test
    fun `Update a Story Schedule`() = runTest {

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
		
		val response = client.put("spaces/606/story_schedulings/123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "story_scheduling": {
		        "publish_at": "2024-08-26T06:56:00.000Z"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}