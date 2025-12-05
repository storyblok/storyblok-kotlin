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

class DatasourceEntries {

	/**
     * Create a datasource entry in a specific datasource
     * https://www.storyblok.com/docs/api/management/datasource-entries/create-a-datasource-entry
     */
    @Test
    fun `Create a Datasource Entry`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/datasource_entries") {
		    setBody("""{
		      "datasource_entry": {
		        "datasource_id": 12345,
		        "name": "newsletter_text",
		        "value": "Subscribe to our newsletter."
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a datasource entry using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasource-entries/delete-a-datasource-entry
     */
    @Test
    fun `Delete a Datasource Entry`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/datasource_entries/52")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single datasource entry object with a specific numeric ID
     * https://www.storyblok.com/docs/api/management/datasource-entries/retrieve-a-single-datasource-entry
     */
    @Test
    fun `Retrieve a Single Datasource Entry`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/datasource_entries/52")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a paginated array of datasource entry objects
     * https://www.storyblok.com/docs/api/management/datasource-entries/retrieve-multiple-datasource-entries
     */
    @Test
    fun `Retrieve Multiple Datasource Entries`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/datasource_entries/") {
		    url {
		        parameters.append("datasource_id", "123")
		        parameters.append("dimension", "456")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a datasource entry using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasource-entries/update-a-datasource-entry
     */
    @Test
    fun `Update a Datasource Entry`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/datasource_entries/52") {
		    setBody("""{
		      "datasource_entry": {
		        "name": "updated_newsletter_text",
		        "value": "Update: Subscribe to our updated newsletter."
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Update a datasource entry using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasource-entries/update-a-datasource-entry
     */
    @Test
    fun `Update a Datasource Entry 2`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/datasource_entries/52") {
		    setBody("""{
		      "datasource_entry": {
		        "dimension_value": "Changed the value in the dimension",
		        "name": "updated_newsletter_text",
		        "value": "Update: Sign up to our updated newsletter."
		      },
		      "dimension_id": 70466
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}