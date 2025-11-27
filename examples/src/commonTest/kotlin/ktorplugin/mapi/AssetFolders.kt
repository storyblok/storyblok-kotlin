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

class AssetFolders {

	/**
     * This endpoint allows you to create a new asset folder.
     * https://www.storyblok.com/docs/api/management/asset-folders/create-an-asset-folder
     */
    @Test
    fun `Create an Asset Folder`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/asset_folders/") {
		    contentType(ContentType.Application.Json)
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
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/606/asset_folders/41")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns a single, asset folder object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/asset-folders/retrieve-a-single-asset-folder
     */
    @Test
    fun `Retrieve a Single Asset Folder`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/asset_folders/41")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of asset folder objects.
     * https://www.storyblok.com/docs/api/management/asset-folders/retrieve-multiple-asset-folders
     */
    @Test
    fun `Retrieve Multiple Asset Folders`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/asset_folders/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows updating an existing asset folder using the numeric ID.
     * https://www.storyblok.com/docs/api/management/asset-folders/update-an-asset-folder
     */
    @Test
    fun `Update an Asset Folder`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/asset_folders/414142") {
		    contentType(ContentType.Application.Json)
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