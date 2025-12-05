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

class InternalTags {

	/**
     * This endpoint allows creating an internal tag inside a particular space.
     * https://www.storyblok.com/docs/api/management/internal-tags/create-an-internal-tag
     */
    @Test
    fun `Create an Internal Tag`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/internal_tags") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "internal_tag": {
		        "name": "New Release",
		        "object_type": "component"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows deleting an internal tag using the numeric ID.
     * https://www.storyblok.com/docs/api/management/internal-tags/delete-an-internal-tag
     */
    @Test
    fun `Delete an Internal Tag`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/internal_tags/123")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows retrieving multiple internal tags of a particular space.
     * https://www.storyblok.com/docs/api/management/internal-tags/retrieve-multiple-internal-tags
     */
    @Test
    fun `Retrieve Multiple Internal Tags`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/internal_tags")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows updating an internal tag using the numeric ID.
     * https://www.storyblok.com/docs/api/management/internal-tags/update-an-internal-tag
     */
    @Test
    fun `Update an Internal Tag`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/internal_tags/123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "internal_tag": {
		        "name": "Updated Tag name",
		        "object_type": "asset"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}