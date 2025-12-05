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
     * https://www.storyblok.com/docs/api/management/components/create-a-component
     */
    @Test
    fun `Create a Component`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/components/") {
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
     * https://www.storyblok.com/docs/api/management/components/delete-a-component
     */
    @Test
    fun `Delete a Component`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/components/4321")
		
		println(response.body<JsonElement>())
    }

	/**
     * Restores a component to a saved version
     * https://www.storyblok.com/docs/api/management/components/restore-a-component-version
     */
    @Test
    fun `Restore a Component Version`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/versions/279820276") {
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
     * https://www.storyblok.com/docs/api/management/components/retrieve-a-single-component-version
     */
    @Test
    fun `Retrieve a Single Component Version`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/components/6826721/component_versions/279820267")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single component object using its ID
     * https://www.storyblok.com/docs/api/management/components/retrieve-a-single-component
     */
    @Test
    fun `Retrieve a Single Component`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/components/4123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a paginated array of component versions
     * https://www.storyblok.com/docs/api/management/components/retrieve-component-versions
     */
    @Test
    fun `Retrieve Component Versions`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/versions") {
		    url {
		        parameters.append("model", "components")
		        parameters.append("model_id", "6826721")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve an array of component objects
     * https://www.storyblok.com/docs/api/management/components/retrieve-multiple-components
     */
    @Test
    fun `Retrieve Multiple Components`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/components/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update the values of a component
     * https://www.storyblok.com/docs/api/management/components/update-a-component
     */
    @Test
    fun `Update a Component`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/components/4123") {
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