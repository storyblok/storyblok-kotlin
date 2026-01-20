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
class AccessTokens {

    /**
     * Create an access token for a particular space.
     * https://www.storyblok.com/docs/api/management/access-tokens/create-an-access-token
     */
    @Test
    fun `Create an Access Token`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/api_keys/") {
            setBody(buildJsonObject {
                putJsonObject("api_key") {
                    put("access", "public")
                    put("name", "My public Access token")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete an access token using its numeric ID.
     * https://www.storyblok.com/docs/api/management/access-tokens/delete-an-access-token
     */
    @Test
    fun `Delete an Access Token`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/api_keys/2345")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of access token objects. The response of this endpoint is not paginated and you will retrieve all tokens.
     * https://www.storyblok.com/docs/api/management/access-tokens/retrieve-multiple-access-tokens
     */
    @Test
    fun `Retrieve Multiple Access Tokens`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/api_keys/")
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an access token with the numeric ID.
     * https://www.storyblok.com/docs/api/management/access-tokens/update-an-access-token
     */
    @Test
    fun `Update an Access Token`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/api_keys/123123") {
            setBody(buildJsonObject {
                putJsonObject("api_key") {
                    put("access", "private")
                    put("name", "My updated token")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}