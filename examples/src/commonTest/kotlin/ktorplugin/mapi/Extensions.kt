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

class Extensions {

	/**
     * This endpoint allows you to create an extension inside the organization or partner extensions.
     * https://www.storyblok.com/docs/api/management/extensions/create-an-extension
     */
    @Test
    fun `Create an Extension`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("org_apps") {
		    setBody("""{
		      "app": {
		        "name": "My extension",
		        "slug": "storyblok-gmbh@extension-1"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows to delete organization and partner extensions by using the numeric ID.
     * https://www.storyblok.com/docs/api/management/extensions/delete-an-extension
     */
    @Test
    fun `Delete an Extension`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("org_apps/123123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve all the plugins from organization or the partner portal.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-all-extensions
     */
    @Test
    fun `Retrieve all Extensions`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("org_apps/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve all the plugins from organization or the partner portal.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-all-extensions
     */
    @Test
    fun `Retrieve all Extensions 2`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("partner_apps/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a specific plugin from organization or the partner extensions.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-an-extension
     */
    @Test
    fun `Retrieve an Extension`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("org_apps/123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a specific plugin from organization or the partner extensions.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-an-extension
     */
    @Test
    fun `Retrieve an Extension 2`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("partner_apps/123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve settings of an extension by the numeric ID. To do so, obtain an OAuth token or a Personal Access Token. This endpoints gives both the app and app_provision objects in the response for the specific extension.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-settings-of-a-plugin
     */
    @Test
    fun `Retrieve Settings of an Installed Extension`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/app_provisions/123123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve settings of all extensions of a particular space. To do so, obtain an OAuth token or a Personal Access Token.
     * https://www.storyblok.com/docs/api/management/extensions/retrieve-settings-of-all-installed-extensions
     */
    @Test
    fun `Retrieve Settings of all Installed Extensions`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/app_provisions/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows you to update an extension, specifically the app object using the numeric ID.
     * https://www.storyblok.com/docs/api/management/extensions/update-an-extension
     */
    @Test
    fun `Update an Extension`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("org_apps/a8d372f8-5659-4f77-b549-0a82ff9c6e72") {
		    setBody("""{
		      "app": {
		        "enable_space_settings": true
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Update settings such as plugin properties inside Space Plugins and Tool Plugins. To do so, obtain an OAuth token or a Personal Access Token.
     * https://www.storyblok.com/docs/api/management/extensions/update-install-plugin-settings
     */
    @Test
    fun `Update Installed Extension Settings`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/app_provisions/a8d372f8-5659-4f77-b549-0a82ff9c6e72") {
		    setBody("""{
		      "app_provision": {
		        "space_level_settings": {
		          "any_setting_1": "hello",
		          "any_setting_2": 123456
		        }
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}