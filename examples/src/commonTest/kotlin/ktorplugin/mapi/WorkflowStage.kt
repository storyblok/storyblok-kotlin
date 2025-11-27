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

class WorkflowStage {

	/**
     * This endpoint allows you to create a workflow stage.
     * https://www.storyblok.com/docs/api/management/workflow-stage/create-a-workflow-stage
     */
    @Test
    fun `Create a Workflow Stage`() = runTest {

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
		
		val response = client.post("spaces/space_id/workflow_stages") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"workflow_stage":{"after_publish_id":561398,"allow_admin_change":true,"allow_admin_publish":true,"allow_all_stages":false,"allow_all_users":false,"allow_editor_change":false,"allow_publish":true,"color":"#2d3v22","is_default":false,"name":"testb","position":3,"space_role_ids":[111111,222222],"user_ids":[123123],"workflow_id":43112,"workflow_stage_ids":[561398]}}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a workflow stage using its numeric ID.
     * https://www.storyblok.com/docs/api/management/workflow-stage/delete-a-workflow-stage
     */
    @Test
    fun `Delete a Workflow Stage`() = runTest {

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
		
		val response = client.delete("spaces/606/workflow_stages/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single workflow stage object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/workflow-stage/retrieve-a-single-workflow-stage
     */
    @Test
    fun `Retrieve a Single Workflow Stage`() = runTest {

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
		
		val response = client.get("spaces/606/workflow_stages/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of workflow stages.
     * https://www.storyblok.com/docs/api/management/workflow-stage/retrieve-multiple-workflow-stages
     */
    @Test
    fun `Retrieve Multiple Workflow Stages`() = runTest {

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
		
		val response = client.get("spaces/606/workflow_stages/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update a workflow stage using the numeric ID.
     * https://www.storyblok.com/docs/api/management/workflow-stage/update-a-workflow-stage
     */
    @Test
    fun `Update a Workflow Stage`() = runTest {

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
		
		val response = client.put("spaces/space_id/workflow_stages/18") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"workflow_stage":{"after_publish_id":561398,"allow_admin_change":true,"allow_admin_publish":false,"allow_all_stages":false,"allow_all_users":false,"allow_editor_change":true,"allow_publish":true,"color":"#fff","is_default":true,"name":"an updated stage ","position":2,"space_role_ids":[232323],"user_ids":[343434],"workflow_stage_ids":[561398]}}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Create a workflow stage change. It is important to pass a story ID along with the object.
     * https://www.storyblok.com/docs/api/management/workflow-stage-changes/create-a-workflow-stage-change
     */
    @Test
    fun `Create a Workflow Stage Change`() = runTest {

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
		
		val response = client.post("spaces/space_id/workflow_stage_changes/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "workflow_stage_change": {
		        "story_id": 123,
		        "workflow_stage_id": 123
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of workflow stage change objects.
     * https://www.storyblok.com/docs/api/management/workflow-stage-changes/retrieve-multiple-workflow-stage-changes
     */
    @Test
    fun `Retrieve Multiple Workflow Stage Changes`() = runTest {

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
		
		val response = client.get("spaces/606/workflow_stage_changes") {
		    url {
		        parameters.append("with_story", "123")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}