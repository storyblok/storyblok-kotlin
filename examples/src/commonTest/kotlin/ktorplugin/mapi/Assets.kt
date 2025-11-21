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

class Assets {

	/**
     * This endpoint allows moving multiple assets using their IDs to a specific folder.
     */
    @Test
    fun `Bulk Moving of Assets`() = runTest {

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
		
		val response = client.post("spaces/123123/assets/bulk_update") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "asset_folder_id": 299783,
		      "ids": [
		        15904978,
		        15878980
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * To bulk restoration of deleted assets, pass bulk_restore after assets in the endpoint. Inside of the array from the payload should contain the asset IDs that you want to restore.
     */
    @Test
    fun `Bulk Restoration of Deleted Assets`() = runTest {

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
		
		val response = client.post("spaces/656/assets/bulk_restore") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "ids": [
		        13941914
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete an asset by using its numeric id.
     */
    @Test
    fun `Delete an Asset`() = runTest {

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
		
		val response = client.delete("spaces/606/assets/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete multiple assets by using their numeric IDs.
     */
    @Test
    fun `Delete Multiple Assets`() = runTest {

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
		
		val response = client.post("spaces/606/assets/bulk_destroy") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "ids": [
		        20142579,
		        20142580
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Validates an uploaded asset and returns a minimal asset object. See upload and replace assets for further information.
     */
    @Test
    fun `Finish Upload`() = runTest {

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
		
		val response = client.get("spaces/123123/assets/89062407031871/finish_upload")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a signed response to be used to upload the asset. See upload and replace assets for further information.
     */
    @Test
    fun `Get a Signed Response Object`() = runTest {

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
		
		val response = client.post("spaces/123123/assets/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "asset_folder_id": 638352,
		      "filename": "123.jpg",
		      "id": 89293614204583,
		      "size": "",
		      "validate_upload": 1
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of asset objects. This endpoint is paginated.
     */
    @Test
    fun `Retrieve Multiple Assets`() = runTest {

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
		
		val response = client.get("spaces/606/assets/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single asset object by providing a specific numeric id.
     */
    @Test
    fun `Retrieve One Asset`() = runTest {

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
		
		val response = client.get("spaces/606/assets/14")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update an asset using the the numeric ID of the asset.
     */
    @Test
    fun `Update Asset`() = runTest {

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
		
		val response = client.put("spaces/123123/assets/656565") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "asset_folder_id": 123123,
		      "internal_tag_ids": [
		        1111
		      ],
		      "is_private": true,
		      "locked": false,
		      "meta_data": {
		        "alt": "Asset ALT",
		        "copyright": "Custom Text",
		        "source": "Asset Source",
		        "title": "Asset Title"
		      },
		      "publish_at": "2024-05-31T11:52:00.000Z"
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}