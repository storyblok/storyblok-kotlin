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

class Presets {

	/**
     * This endpoint can be used to create new presets.
     */
    @Test
    fun `Create a Preset`() = runTest {

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
		
		val response = client.post("spaces/606/presets/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "preset": {
		        "component_id": 62,
		        "name": "Teaser with filled headline",
		        "preset": {
		          "headline": "This is a default value for the preset!"
		        }
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a preset by using its numeric id.
     */
    @Test
    fun `Delete a Preset`() = runTest {

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
		
		val response = client.delete("spaces/606/presets/1814")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single preset object with a specific numeric id.
     */
    @Test
    fun `Retrieve a Single Preset`() = runTest {

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
		
		val response = client.get("spaces/606/presets/1814")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of preset objects.
     */
    @Test
    fun `Retrieve Multiple Presets`() = runTest {

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
		
		val response = client.get("spaces/606/presets")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to update presets using the numeric ID.
     */
    @Test
    fun `Update a Preset`() = runTest {

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
		
		val response = client.put("spaces/606/presets/1814") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "preset": {
		        "component_id": 62,
		        "name": "Teaser with headline and image",
		        "preset": {
		          "headline": "This is a default value for the preset!",
		          "image": "//a.storyblok.com/f/606/..."
		        }
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}