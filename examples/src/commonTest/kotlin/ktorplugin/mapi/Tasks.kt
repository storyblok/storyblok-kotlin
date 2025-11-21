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

class Tasks {

	/**
     * This endpoint creates a new task.
     */
    @Test
    fun `Create a Task`() = runTest {

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
		
		val response = client.post("spaces/606/tasks/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "task": {
		        "name": "My Task Name",
		        "task_type": "webhook",
		        "webhook_url": "https://www.storyblok.com"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a task using the numeric ID.
     */
    @Test
    fun `Delete a Task`() = runTest {

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
		
		val response = client.delete("spaces/606/tasks/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single task object with a specific numeric id.
     */
    @Test
    fun `Retrieve a Single Task`() = runTest {

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
		
		val response = client.get("spaces/606/tasks/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of task objects. This endpoint is paged.
     */
    @Test
    fun `Retrieve Multiple Tasks`() = runTest {

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
		
		val response = client.get("spaces/606/tasks/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update tasks.
     */
    @Test
    fun `Update a Task`() = runTest {

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
		
		val response = client.put("spaces/606/tasks/124") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "task": {
		        "name": "My Updated Task Name",
		        "task_type": "webhook",
		        "webhook_url": "https://www.storyblok.com"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}