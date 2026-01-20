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
class BranchDeployments {

    /**
     * Once you have set your Pipelines (via the Storyblok App or the Management API), you can start to trigger the deployment. The deployment could be triggered via Storyblok UI in the Content section by selecting the pipeline in the Pipelines dropdown.
     * https://www.storyblok.com/docs/api/management/branch-deployments/create-a-branch-deployment
     */
    @Test
    fun `Create a Branch Deployment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/deployments/") {
            setBody(buildJsonObject {
                put("branch_id", 1)
                putJsonArray("release_uuids") {
                    add("1234-4567")
                    add("1234-4568")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}