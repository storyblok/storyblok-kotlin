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

class Workflows {

	/**
     * This end point creates a new workflow.
     */
    @Test
    fun `Create a Workflow`() = runTest {

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
		
		val response = client.post("spaces/606/workflows") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "workflow": {
		        "content_types": [
		          "page"
		        ],
		        "name": "page"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a workflow using the numeric ID. The default workflow cannot be deleted.
     */
    @Test
    fun `Delete a Workflow`() = runTest {

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
		
		val response = client.delete("spaces/606/workflows/656")
		
		println(response.body<JsonElement>())
    }

	/**
     * Creates a new custom workflow by duplicating an existing workflow using the workflow id of the parent workflow. Duplicating a workflow keeps workflow stages the same for the new workflow.The name and content types are required and should be different.
     */
    @Test
    fun `Duplicate a Workflow`() = runTest {

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
		
		val response = client.post("spaces/606/workflows/656/duplicate") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "workflow": {
		        "content_types": [
		          "page_new"
		        ],
		        "name": "duplicated page"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single, workflow object by providing a specific numeric ID.
     */
    @Test
    fun `Retrieve a Single Workflow`() = runTest {

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
		
		val response = client.get("spaces/606/workflows/656")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of all the workflow stages in a space.
     */
    @Test
    fun `Retrieve Multiple Workflows`() = runTest {

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
		
		val response = client.get("spaces/606/workflows")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update a workflow using its numeric ID.
     */
    @Test
    fun `Update a Workflow`() = runTest {

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
		
		val response = client.put("spaces/606/workflows/656") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "workflow": {
		        "content_types": [
		          "page",
		          "teaser"
		        ],
		        "name": "updated_name"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}