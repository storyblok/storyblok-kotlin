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

class FieldPlugins {

	/**
     * This endpoint allows you to create a field type plugin.
     * https://www.storyblok.com/docs/api/management/field-plugins/create-a-field-plugin
     */
    @Test
    fun `Create a Field Plugin`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("field_types/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "field_type": {
		        "name": "my-geo-selector"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a field plugin by using its numeric id.
     * https://www.storyblok.com/docs/api/management/field-plugins/delete-a-field-plugin
     */
    @Test
    fun `Delete a Field Plugin`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("field_types/1")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single field-type object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-a-single-field-plugin
     */
    @Test
    fun `Retrieve a Single Field Plugin`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("field_types/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single field-type object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-a-single-field-plugin
     */
    @Test
    fun `Retrieve a Single Field Plugin 2`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("org_field_types/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single field-type object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-a-single-field-plugin
     */
    @Test
    fun `Retrieve a Single Field Plugin 3`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("partner_field_types/124")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of field plugin objects. This endpoint is paged.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-multiple-field-plugins
     */
    @Test
    fun `Retrieve Multiple Field Plugins`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("field_types/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of field plugin objects. This endpoint is paged.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-multiple-field-plugins
     */
    @Test
    fun `Retrieve Multiple Field Plugins 2`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("org_field_types/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of field plugin objects. This endpoint is paged.
     * https://www.storyblok.com/docs/api/management/field-plugins/retrieve-multiple-field-plugins
     */
    @Test
    fun `Retrieve Multiple Field Plugins 3`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("partner_field_types/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to perform updates to a field type plugin.
     * https://www.storyblok.com/docs/api/management/field-plugins/update-a-field-plugin
     */
    @Test
    fun `Update a Field Plugin`() = runTest {

        val client = HttpClient {
			expectSuccess = false
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("field_types/123123") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "field_type": {
		        "body": "const Fieldtype = {}",
		        "compiled_body": ""
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}