package ktorplugin.mapi

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Api.Config.Management.AccessToken.OAuth
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test

class SchedulingStories {

	/**
     * This endpoint allows you to create a new story schedule.
     * https://www.storyblok.com/docs/api/management/scheduling-stories/create-a-story-schedule
     */
    @Test
    fun `Create a Story Schedule`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/story_schedulings") {
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
     * https://www.storyblok.com/docs/api/management/scheduling-stories/delete-a-story-schedule
     */
    @Test
    fun `Delete a Story Schedule`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/story_schedulings/123/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of story schedule objects.
     * https://www.storyblok.com/docs/api/management/scheduling-stories/retrieve-multiple-story-schedules
     */
    @Test
    fun `Retrieve Multiple Story Schedules`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/story_schedulings")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single schedule object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/scheduling-stories/retrieve-one-story-schedule
     */
    @Test
    fun `Retrieve One Story Schedule`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/story_schedulings/91")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a publishing schedule by the numeric ID.
     * https://www.storyblok.com/docs/api/management/scheduling-stories/update-a-story-schedule
     */
    @Test
    fun `Update a Story Schedule`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/story_schedulings/123") {
		    setBody("""{
		      "story_scheduling": {
		        "publish_at": "2024-08-26T06:56:00.000Z"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}