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

class Components {

	/**
     * Create a component with properties available in the collaborator object
     */
    @Test
    fun `Create a Component`() = runTest {

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
		
		val response = client.post("spaces/606/components/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "component": {
		        "display_name": null,
		        "is_nestable": true,
		        "is_root": false,
		        "name": "banner_section",
		        "schema": {
		          "headline": {
		            "description": "This field is used to render a title",
		            "pos": 0,
		            "translatable": true,
		            "type": "text"
		          }
		        }
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a component using its ID
     */
    @Test
    fun `Delete a Component`() = runTest {

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
		
		val response = client.delete("spaces/656/components/4321")
		
		println(response.body<JsonElement>())
    }

	/**
     * Restores a component to a saved version
     */
    @Test
    fun `Restore a Component Version`() = runTest {

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
		
		val response = client.put("spaces/656/versions/279820276") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "model": "components",
		      "model_id": 6826721
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve the schema details of a component version
     */
    @Test
    fun `Retrieve a Single Component Version`() = runTest {

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
		
		val response = client.get("spaces/656/components/6826721/component_versions/279820267")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single component object using its ID
     */
    @Test
    fun `Retrieve a Single Component`() = runTest {

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
		
		val response = client.get("spaces/656/components/4123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a paginated array of component versions
     */
    @Test
    fun `Retrieve Component Versions`() = runTest {

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
		
		val response = client.get("spaces/656/versions") {
		    url {
		        parameters.append("model", "components")
		        parameters.append("model_id", "6826721")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve an array of component objects
     */
    @Test
    fun `Retrieve Multiple Components`() = runTest {

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
		
		val response = client.get("spaces/656/components/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update the values of a component
     */
    @Test
    fun `Update a Component`() = runTest {

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
		
		val response = client.put("spaces/656/components/4123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "component": {
		        "display_name": null,
		        "id": 4123,
		        "is_nestable": true,
		        "is_root": false,
		        "name": "banner_section",
		        "schema": {
		          "headline": {
		            "description": "Use this field for the title",
		            "pos": 0,
		            "translatable": true,
		            "type": "text"
		          }
		        }
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}