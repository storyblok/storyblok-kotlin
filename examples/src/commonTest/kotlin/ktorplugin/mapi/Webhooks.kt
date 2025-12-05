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

class Webhooks {

	/**
     * You can set some of the fields available in the webhook object, below we only list the properties in the example and the possible required fields.
     * https://www.storyblok.com/docs/api/management/webhooks/add-a-webhook
     */
    @Test
    fun `Add a Webhook`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/webhook_endpoints/") {
		    setBody("""{
		      "webhook_endpoint": {
		        "actions": [
		          "story.published"
		        ],
		        "activated": true,
		        "endpoint": "https://apiendpoint.com",
		        "name": "Rebuild Website",
		        "secret": ""
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a webhook by its numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/delete-a-webhook
     */
    @Test
    fun `Delete a Webhook`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/webhook_endpoints/4573")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single webhook object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/retrieve-a-single-webhook
     */
    @Test
    fun `Retrieve a Single Webhook`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/webhook_endpoints/4570")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of webhook objects
     * https://www.storyblok.com/docs/api/management/webhooks/retrieve-multiple-webhooks
     */
    @Test
    fun `Retrieve Multiple Webhooks`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/webhook_endpoints/")
		
		println(response.body<JsonElement>())
    }

	/**
     * You can update an existing webhook field using the numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/update-a-webhook
     */
    @Test
    fun `Update a Webhook`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/webhook_endpoints/4570") {
		    setBody("""{
		      "webhook_endpoint": {
		        "actions": [
		          "story.published",
		          "story.unpublished"
		        ],
		        "activated": true,
		        "endpoint": "https://new-api-endpoint.com",
		        "name": "Rebuild Website",
		        "secret": "HelloSecret"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}