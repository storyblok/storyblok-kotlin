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
class Workflows {

	/**
     * This end point creates a new workflow.
     * https://www.storyblok.com/docs/api/management/workflows/create-a-workflow
     */
    @Test
    fun `Create a Workflow`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/workflows") {
		    setBody(buildJsonObject {
		        putJsonObject("workflow") {
		            putJsonArray("content_types") {
		                add("page")
		            }
		            put("name", "page")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a workflow using the numeric ID. The default workflow cannot be deleted.
     * https://www.storyblok.com/docs/api/management/workflows/delete-a-workflow
     */
    @Test
    fun `Delete a Workflow`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/workflows/656")
		
		println(response.body<JsonElement>())
    }

	/**
     * Creates a new custom workflow by duplicating an existing workflow using the workflow id of the parent workflow. Duplicating a workflow keeps workflow stages the same for the new workflow.The name and content types are required and should be different.
     * https://www.storyblok.com/docs/api/management/workflows/duplicate-workflow
     */
    @Test
    fun `Duplicate a Workflow`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/workflows/656/duplicate") {
		    setBody(buildJsonObject {
		        putJsonObject("workflow") {
		            putJsonArray("content_types") {
		                add("page_new")
		            }
		            put("name", "duplicated page")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single, workflow object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/workflows/retrieve-a-single-workflow
     */
    @Test
    fun `Retrieve a Single Workflow`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/workflows/656")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of all the workflow stages in a space.
     * https://www.storyblok.com/docs/api/management/workflows/retrieve-multiple-workflows
     */
    @Test
    fun `Retrieve Multiple Workflows`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/workflows")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update a workflow using its numeric ID.
     * https://www.storyblok.com/docs/api/management/workflows/update-a-workflow
     */
    @Test
    fun `Update a Workflow`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/workflows/656") {
		    setBody(buildJsonObject {
		        putJsonObject("workflow") {
		            putJsonArray("content_types") {
		                add("page")
		                add("teaser")
		            }
		            put("name", "updated_name")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

}