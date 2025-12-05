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

class AssetFolders {

	/**
     * This endpoint allows you to create a new asset folder.
     * https://www.storyblok.com/docs/api/management/asset-folders/create-an-asset-folder
     */
    @Test
    fun `Create an Asset Folder`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/asset_folders/") {
		    setBody("""{
		      "asset_folder": {
		        "name": "Header Images",
		        "parent_id": 123123
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an asset folder by using its numeric id.
     * https://www.storyblok.com/docs/api/management/asset-folders/delete-an-asset-folder
     */
    @Test
    fun `Delete an Asset Folder`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/asset_folders/41")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns a single, asset folder object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/asset-folders/retrieve-a-single-asset-folder
     */
    @Test
    fun `Retrieve a Single Asset Folder`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/asset_folders/41")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of asset folder objects.
     * https://www.storyblok.com/docs/api/management/asset-folders/retrieve-multiple-asset-folders
     */
    @Test
    fun `Retrieve Multiple Asset Folders`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/asset_folders/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows updating an existing asset folder using the numeric ID.
     * https://www.storyblok.com/docs/api/management/asset-folders/update-an-asset-folder
     */
    @Test
    fun `Update an Asset Folder`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/asset_folders/414142") {
		    setBody("""{
		      "asset_folder": {
		        "name": "Updated folder",
		        "parent_id": 288983
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}