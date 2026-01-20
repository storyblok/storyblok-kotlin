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
class Pipelines {

    /**
     * This endpoint creates a new branch.
     * https://www.storyblok.com/docs/api/management/pipelines/create-a-branch
     */
    @Test
    fun `Create a Branch`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/branches/") {
            setBody(buildJsonObject {
                putJsonObject("branch") {
                    put("name", "A new branch")
                    put("position", 2)
                    put("source_id", 12332)
                    put("url", "https://new_domain.com")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a branch using its numeric ID.
     * https://www.storyblok.com/docs/api/management/pipelines/delete-a-branch
     */
    @Test
    fun `Delete a Branch`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/branches/14")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single branch object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/pipelines/retrieve-a-single-branch
     */
    @Test
    fun `Retrieve a Single Branch`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/branches/14")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of  branch objects
     * https://www.storyblok.com/docs/api/management/pipelines/retrieve-multiple-branches
     */
    @Test
    fun `Retrieve Multiple Branches`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/branches/")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint updates a branch using its numeric ID.
     * https://www.storyblok.com/docs/api/management/pipelines/update-a-branch
     */
    @Test
    fun `Update a Branch`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/branches/14") {
            setBody(buildJsonObject {
                putJsonObject("branch") {
                    put("name", "Branche 123")
                    put("position", 7)
                    put("source_id", 12345)
                    put("url", "https://new_url.com/")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}