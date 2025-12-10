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
class Approvals {

	/**
     * 
     * https://www.storyblok.com/docs/api/management/approvals/create-approval
     */
    @Test
    fun `Create Approval`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/approvals/") {
		    setBody(buildJsonObject {
		        putJsonObject("approval") {
		            put("approver_id", 1028)
		            put("story_id", 1066)
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * The Approval feature mentioned is exclusive to Storyblok v1 and discontinued in v2.
     * https://www.storyblok.com/docs/api/management/approvals/create-release-approval
     */
    @Test
    fun `Create Release Approval`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/approvals/") {
		    setBody(buildJsonObject {
		        putJsonObject("approval") {
		            put("approver_id", 1030)
		            put("story_id", 1067)
		        }
		        put("release_id", 16)
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an approval by using its numeric id.
     * https://www.storyblok.com/docs/api/management/approvals/delete-an-approval
     */
    @Test
    fun `Delete an Approval`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/approvals/5405")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single approval object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/approvals/retrieve-a-single-approval
     */
    @Test
    fun `Retrieve a Single Approval`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/approvals/5405")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of approval objects. This endpoint can be filtered on the approver and is paged.
     * https://www.storyblok.com/docs/api/management/approvals/retrieve-multiple-approvals
     */
    @Test
    fun `Retrieve Multiple Approvals`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/approvals") {
		    url {
		        parameters.append("approver", "1028")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}