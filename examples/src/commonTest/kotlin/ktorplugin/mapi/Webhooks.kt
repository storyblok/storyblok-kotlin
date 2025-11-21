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

class Webhooks {

	/**
     * You can set some of the fields available in the webhook object, below we only list the properties in the example and the possible required fields.
     */
    @Test
    fun `Add a Webhook`() = runTest {

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
		
		val response = client.post("spaces/656/webhook_endpoints/") {
		    contentType(ContentType.Application.Json)
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
     */
    @Test
    fun `Delete a Webhook`() = runTest {

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
		
		val response = client.delete("spaces/656/webhook_endpoints/4573")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single webhook object by providing a specific numeric ID.
     */
    @Test
    fun `Retrieve a Single Webhook`() = runTest {

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
		
		val response = client.get("spaces/656/webhook_endpoints/4570")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of webhook objects
     */
    @Test
    fun `Retrieve Multiple Webhooks`() = runTest {

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
		
		val response = client.get("spaces/656/webhook_endpoints/")
		
		println(response.body<JsonElement>())
    }

	/**
     * You can update an existing webhook field using the numeric ID.
     */
    @Test
    fun `Update a Webhook`() = runTest {

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
		
		val response = client.put("spaces/656/webhook_endpoints/4570") {
		    contentType(ContentType.Application.Json)
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