package ktorplugin.mapi

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Api.Config.Management.AccessToken.OAuth
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test

class Releases {

	/**
     * This endpoint allows you to create a new release.
     * https://www.storyblok.com/docs/api/management/releases/create-a-release
     */
    @Test
    fun `Create a Release`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/releases/") {
		    setBody("""{
		      "release": {
		        "branches_to_deploy": [
		          123,
		          456
		        ],
		        "name": "Summer Special",
		        "release_at": "2025-01-01 01:01"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a release using its numeric id.
     * https://www.storyblok.com/docs/api/management/releases/delete-a-release
     */
    @Test
    fun `Delete a Release`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/releases/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single release object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/releases/retrieve-a-single-release
     */
    @Test
    fun `Retrieve a Single Release`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/releases/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of releases.
     * https://www.storyblok.com/docs/api/management/releases/retrieve-multiple-releases
     */
    @Test
    fun `Retrieve Multiple Releases`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/releases/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows you to update a release using the numeric ID.
     * https://www.storyblok.com/docs/api/management/releases/update-a-release
     */
    @Test
    fun `Update a Release`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/releases/123") {
		    setBody("""{
		      "do_release": true,
		      "release": {
		        "branches_to_deploy": [
		          123,
		          456
		        ],
		        "name": "Summer Special",
		        "release_at": "2025-01-01 01:01"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}