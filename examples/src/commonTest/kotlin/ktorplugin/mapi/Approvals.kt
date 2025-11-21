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

class Approvals {

	/**
     * 
     */
    @Test
    fun `Create Approval`() = runTest {

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
		
		val response = client.post("spaces/606/approvals/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "approval": {
		        "approver_id": 1028,
		        "story_id": 1066
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * The Approval feature mentioned is exclusive to Storyblok v1 and discontinued in v2.
     */
    @Test
    fun `Create Release Approval`() = runTest {

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
		
		val response = client.post("spaces/606/approvals/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "approval": {
		        "approver_id": 1030,
		        "story_id": 1067
		      },
		      "release_id": 16
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an approval by using its numeric id.
     */
    @Test
    fun `Delete an Approval`() = runTest {

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
		
		val response = client.delete("spaces/606/approvals/5405")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single approval object with a specific numeric id.
     */
    @Test
    fun `Retrieve a Single Approval`() = runTest {

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
		
		val response = client.get("spaces/606/approvals/5405")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of approval objects. This endpoint can be filtered on the approver and is paged.
     */
    @Test
    fun `Retrieve Multiple Approvals`() = runTest {

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
		
		val response = client.get("spaces/606/approvals") {
		    url {
		        parameters.append("approver", "1028")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}