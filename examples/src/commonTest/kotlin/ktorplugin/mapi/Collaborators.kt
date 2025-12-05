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

class Collaborators {

	/**
     * Add collaborators with specific roles and permissions available in the collaborator object
     * https://www.storyblok.com/docs/api/management/collaborators/add-a-collaborator
     */
    @Test
    fun `Add a Collaborator`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/collaborators/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "allow_multiple_roles_creation": false,
		      "email": "api.test@storyblok.com",
		      "permissions": [],
		      "role": "admin",
		      "space_role_id": "",
		      "space_role_ids": []
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Add collaborators with specific roles and permissions available in the collaborator object
     * https://www.storyblok.com/docs/api/management/collaborators/add-a-collaborator
     */
    @Test
    fun `Add a Collaborator 2`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/collaborators/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "allow_multiple_roles_creation": false,
		      "email": "api.test@storyblok.com",
		      "permissions": [],
		      "role": "62454",
		      "space_role_id": 62454,
		      "space_role_ids": []
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Add collaborators with specific roles and permissions available in the collaborator object
     * https://www.storyblok.com/docs/api/management/collaborators/add-a-collaborator
     */
    @Test
    fun `Add a Collaborator 3`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/collaborators/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "allow_multiple_roles_creation": true,
		      "email": "api.test@storyblok.com",
		      "permissions": [],
		      "role": "multi",
		      "space_role_ids": [
		        62454,
		        123123
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Add a user with SSO using the 
     * https://www.storyblok.com/docs/api/management/collaborators/add-a-user-with-sso
     */
    @Test
    fun `Add a User with SSO`() = runTest {

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
		
		val response = client.post("spaces/288868932106293/collaborators/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "collaborator": {
		        "email": "api@storyblok.com",
		        "role": "editor",
		        "space_role_id": 18,
		        "sso_id": "123456789"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a collaborator using their 
     * https://www.storyblok.com/docs/api/management/collaborators/delete-a-collaborator
     */
    @Test
    fun `Delete a Collaborator`() = runTest {

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
		
		val response = client.delete("spaces/288868932106293/collaborators/2362")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a paginated array of collaborator objects
     * https://www.storyblok.com/docs/api/management/collaborators/retrieve-multiple-collaborators
     */
    @Test
    fun `Retrieve Multiple Collaborators`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/collaborators/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a collaborator using all fields available in the collaborator object
     * https://www.storyblok.com/docs/api/management/collaborators/update-a-collaborator-roles-and-permissions
     */
    @Test
    fun `Update a Collaborator Roles and Permissions`() = runTest {

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
		
		val response = client.put("spaces/288868932106293/collaborators/2362") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "collaborator": {
		        "role": 49707,
		        "space_role_id": 49707
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}