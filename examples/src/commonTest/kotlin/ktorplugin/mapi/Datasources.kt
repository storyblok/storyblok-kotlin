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

class Datasources {

	/**
     * Create a new datasource
     * https://www.storyblok.com/docs/api/management/datasources/create-a-datasource
     */
    @Test
    fun `Create a Datasource`() = runTest {

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
		
		val response = client.post("spaces/656/datasources/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "datasource": {
		        "name": "Labels for Website",
		        "slug": "labels"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Create a new datasource
     * https://www.storyblok.com/docs/api/management/datasources/create-a-datasource
     */
    @Test
    fun `Create a Datasource 2`() = runTest {

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
		
		val response = client.post("spaces/656/datasources/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "datasource": {
		        "dimensions_attributes": [
		          {
		            "entry_value": "es",
		            "name": "Spanish"
		          },
		          {
		            "entry_value": "de",
		            "name": "German"
		          }
		        ],
		        "name": "Labels for Website",
		        "slug": "label"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a datasource using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/delete-a-datasource
     */
    @Test
    fun `Delete a Datasource`() = runTest {

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
		
		val response = client.delete("spaces/656/datasources/91")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single datasource using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/retrieve-a-single-datasource
     */
    @Test
    fun `Retrieve a Single Datasource`() = runTest {

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
		
		val response = client.get("spaces/656/datasources/91")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a paginated array of datasource objects
     * https://www.storyblok.com/docs/api/management/datasources/retrieve-multiple-datasources
     */
    @Test
    fun `Retrieve Multiple Datasources`() = runTest {

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
		
		val response = client.get("spaces/656/datasources/") {
		    url {
		        parameters.append("search", "Labels for Website")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a datasource using it numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/update-a-datasource
     */
    @Test
    fun `Update a Datasource`() = runTest {

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
		
		val response = client.put("spaces/656/datasources/91") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "datasource": {
		        "name": "Labels for Website",
		        "slug": "labels_for_website"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a datasource using it numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/update-a-datasource
     */
    @Test
    fun `Update a Datasource 2`() = runTest {

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
		
		val response = client.put("spaces/656/datasources/91") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "datasource": {
		        "dimensions_attributes": [
		          {
		            "entry_value": "another_slug",
		            "name": "Another Name"
		          }
		        ],
		        "name": "Labels for Website",
		        "slug": "label"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}