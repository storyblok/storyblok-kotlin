package ktorplugin.mapi

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Api.Config.Management.AccessToken.OAuth
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test

@OptIn(ExperimentalSerializationApi::class)
class Tasks {

	/**
     * This endpoint creates a new task.
     * https://www.storyblok.com/docs/api/management/tasks/create-a-task
     */
    @Test
    fun `Create a Task`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/tasks/") {
		    setBody(buildJsonObject {
		        putJsonObject("task") {
		            put("name", "My Task Name")
		            put("task_type", "webhook")
		            put("webhook_url", "https://www.storyblok.com")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a task using the numeric ID.
     * https://www.storyblok.com/docs/api/management/tasks/delete-a-task
     */
    @Test
    fun `Delete a Task`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/tasks/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single task object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/tasks/retrieve-a-single-task
     */
    @Test
    fun `Retrieve a Single Task`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/tasks/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of task objects. This endpoint is paged.
     * https://www.storyblok.com/docs/api/management/tasks/retrieve-multiple-tasks
     */
    @Test
    fun `Retrieve Multiple Tasks`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/tasks/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update tasks.
     * https://www.storyblok.com/docs/api/management/tasks/update-a-task
     */
    @Test
    fun `Update a Task`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/tasks/124") {
		    setBody(buildJsonObject {
		        putJsonObject("task") {
		            put("name", "My Updated Task Name")
		            put("task_type", "webhook")
		            put("webhook_url", "https://www.storyblok.com")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

}