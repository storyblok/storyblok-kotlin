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

class ComponentFolders {

	/**
     * Create a new component folder
     * https://www.storyblok.com/docs/api/management/component-folders/create-a-component-folder
     */
    @Test
    fun `Create a Component Folder`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/component_groups/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "component_group": {
		        "name": "Teasers",
		        "parent_id": "123123"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a component folder using its numeric ID
     * https://www.storyblok.com/docs/api/management/component-folders/delete-a-component-folder
     */
    @Test
    fun `Delete a Component Folder`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/component_groups/4123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single component folder object using its ID
     * https://www.storyblok.com/docs/api/management/component-folders/retrieve-a-single-component-folder
     */
    @Test
    fun `Retrieve a Single Component Folder`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/component_groups/4123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a paginated array of component folder objects
     * https://www.storyblok.com/docs/api/management/component-folders/retrieve-multiple-component-folders
     */
    @Test
    fun `Retrieve Multiple Component Folders`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/component_groups/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a specific component folder
     * https://www.storyblok.com/docs/api/management/component-folders/update-a-component-folder
     */
    @Test
    fun `Update a Component folder`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/component_groups/4123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "component_group": {
		        "name": "New Teaser Name",
		        "parent_id": 123123
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}