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

class BranchDeployments {

	/**
     * Once you have set your Pipelines (via the Storyblok App or the Management API), you can start to trigger the deployment. The deployment could be triggered via Storyblok UI in the Content section by selecting the pipeline in the Pipelines dropdown.
     * https://www.storyblok.com/docs/api/management/branch-deployments/create-a-branch-deployment
     */
    @Test
    fun `Create a Branch Deployment`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/deployments/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "branch_id": 1,
		      "release_uuids": [
		        "1234-4567",
		        "1234-4568"
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}