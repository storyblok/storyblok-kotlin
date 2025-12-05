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

class WorkflowStageChanges {

	/**
     * Create a workflow stage change. It is important to pass a story ID along with the object.
     * https://www.storyblok.com/docs/api/management/workflow-stage-changes/create-a-workflow-stage-change
     */
    @Test
    fun `Create a Workflow Stage Change`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/space_id/workflow_stage_changes/") {
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
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/workflow_stage_changes") {
		    url {
		        parameters.append("with_story", "123")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}