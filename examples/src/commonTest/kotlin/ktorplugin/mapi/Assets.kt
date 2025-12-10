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
class Assets {

	/**
     * This endpoint allows moving multiple assets using their IDs to a specific folder.
     * https://www.storyblok.com/docs/api/management/assets/bulk-moving-of-assets
     */
    @Test
    fun `Bulk Moving of Assets`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/assets/bulk_update") {
		    setBody(buildJsonObject {
		        put("asset_folder_id", 299783)
		        putJsonArray("ids") {
		            add(15904978)
		            add(15878980)
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * To bulk restoration of deleted assets, pass bulk_restore after assets in the endpoint. Inside of the array from the payload should contain the asset IDs that you want to restore.
     * https://www.storyblok.com/docs/api/management/assets/bulk-restoration-of-deleted-assets
     */
    @Test
    fun `Bulk Restoration of Deleted Assets`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/assets/bulk_restore") {
		    setBody(buildJsonObject {
		        putJsonArray("ids") {
		            add(13941914)
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an asset by using its numeric id.
     * https://www.storyblok.com/docs/api/management/assets/delete-an-asset
     */
    @Test
    fun `Delete an Asset`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/assets/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete multiple assets by using their numeric IDs.
     * https://www.storyblok.com/docs/api/management/assets/delete-multiple-assets
     */
    @Test
    fun `Delete Multiple Assets`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/assets/bulk_destroy") {
		    setBody(buildJsonObject {
		        putJsonArray("ids") {
		            add(20142579)
		            add(20142580)
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Validates an uploaded asset and returns a minimal asset object. See upload and replace assets for further information.
     * https://www.storyblok.com/docs/api/management/assets/finish-upload
     */
    @Test
    fun `Finish Upload`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/assets/89062407031871/finish_upload")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a signed response to be used to upload the asset. See upload and replace assets for further information.
     * https://www.storyblok.com/docs/api/management/assets/get-signed-response
     */
    @Test
    fun `Get a Signed Response Object`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/assets/") {
		    setBody(buildJsonObject {
		        put("asset_folder_id", 638352)
		        put("filename", "123.jpg")
		        put("id", 89293614204583)
		        put("size", "")
		        put("validate_upload", 1)
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of asset objects. This endpoint is paginated.
     * https://www.storyblok.com/docs/api/management/assets/retrieve-multiple-assets
     */
    @Test
    fun `Retrieve Multiple Assets`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/assets/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single asset object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/assets/retrieve-one-asset
     */
    @Test
    fun `Retrieve One Asset`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/assets/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update an asset using the the numeric ID of the asset.
     * https://www.storyblok.com/docs/api/management/assets/update-asset
     */
    @Test
    fun `Update Asset`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/assets/656565") {
		    setBody(buildJsonObject {
		        put("asset_folder_id", 123123)
		        putJsonArray("internal_tag_ids") {
		            add(1111)
		        }
		        put("is_private", true)
		        put("locked", false)
		        putJsonObject("meta_data") {
		            put("alt", "Asset ALT")
		            put("copyright", "Custom Text")
		            put("source", "Asset Source")
		            put("title", "Asset Title")
		        }
		        put("publish_at", "2024-05-31T11:52:00.000Z")
		    })
		}
		
		println(response.body<JsonElement>())
    }

}