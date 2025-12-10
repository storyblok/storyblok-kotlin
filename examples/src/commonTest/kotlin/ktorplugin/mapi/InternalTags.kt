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
class InternalTags {

	/**
     * This endpoint allows creating an internal tag inside a particular space.
     * https://www.storyblok.com/docs/api/management/internal-tags/create-an-internal-tag
     */
    @Test
    fun `Create an Internal Tag`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/internal_tags") {
		    setBody(buildJsonObject {
		        putJsonObject("internal_tag") {
		            put("name", "New Release")
		            put("object_type", "component")
		        }
		    })
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
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
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
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
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
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/internal_tags/123") {
		    setBody(buildJsonObject {
		        putJsonObject("internal_tag") {
		            put("name", "Updated Tag name")
		            put("object_type", "asset")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

}