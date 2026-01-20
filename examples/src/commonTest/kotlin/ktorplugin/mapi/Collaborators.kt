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
class Collaborators {

    /**
     * Add collaborators with specific roles and permissions available in the collaborator object
     * https://www.storyblok.com/docs/api/management/collaborators/add-a-collaborator
     */
    @Test
    fun `Add a Collaborator`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/collaborators/") {
            setBody(buildJsonObject {
                put("allow_multiple_roles_creation", false)
                put("email", "api.test@storyblok.com")
                putJsonArray("permissions") { }
                put("role", "admin")
                put("space_role_id", null)
                putJsonArray("space_role_ids") { }
            })
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/collaborators/") {
            setBody(buildJsonObject {
                put("allow_multiple_roles_creation", false)
                put("email", "api.test@storyblok.com")
                putJsonArray("permissions") { }
                put("role", "62454")
                put("space_role_id", 62454)
                putJsonArray("space_role_ids") { }
            })
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/collaborators/") {
            setBody(buildJsonObject {
                put("allow_multiple_roles_creation", true)
                put("email", "api.test@storyblok.com")
                putJsonArray("permissions") { }
                put("role", "multi")
                put("space_role_id", null)
                putJsonArray("space_role_ids") {
                    add(62454)
                    add(123123)
                }
            })
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/collaborators/") {
            setBody(buildJsonObject {
                putJsonObject("collaborator") {
                    put("email", "api@storyblok.com")
                    put("role", "editor")
                    put("space_role_id", 18)
                    put("sso_id", "123456789")
                }
            })
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
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
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/collaborators/2362") {
            setBody(buildJsonObject {
                putJsonObject("collaborator") {
                    put("role", 49707)
                    put("space_role_id", 49707)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}