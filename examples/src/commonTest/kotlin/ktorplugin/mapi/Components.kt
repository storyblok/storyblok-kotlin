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
class Components {

	/**
     * Create a component with properties available in the collaborator object
     * https://www.storyblok.com/docs/api/management/components/create-a-component
     */
    @Test
    fun `Create a Component`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/components/") {
		    setBody(buildJsonObject {
		        putJsonObject("component") {
		            put("display_name", null)
		            put("is_nestable", true)
		            put("is_root", false)
		            put("name", "banner_section")
		            putJsonObject("schema") {
		                putJsonObject("headline") {
		                    put("description", "This field is used to render a title")
		                    put("pos", 0)
		                    put("translatable", true)
		                    put("type", "text")
		                }
		            }
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a component using its ID
     * https://www.storyblok.com/docs/api/management/components/delete-a-component
     */
    @Test
    fun `Delete a Component`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/components/4321")
		
		println(response.body<JsonElement>())
    }

	/**
     * Restores a component to a saved version
     * https://www.storyblok.com/docs/api/management/components/restore-a-component-version
     */
    @Test
    fun `Restore a Component Version`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/versions/279820276") {
		    setBody(buildJsonObject {
		        put("model", "components")
		        put("model_id", 6826721)
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve the schema details of a component version
     * https://www.storyblok.com/docs/api/management/components/retrieve-a-single-component-version
     */
    @Test
    fun `Retrieve a Single Component Version`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/components/6826721/component_versions/279820267")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a single component object using its ID
     * https://www.storyblok.com/docs/api/management/components/retrieve-a-single-component
     */
    @Test
    fun `Retrieve a Single Component`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/components/4123")
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve a paginated array of component versions
     * https://www.storyblok.com/docs/api/management/components/retrieve-component-versions
     */
    @Test
    fun `Retrieve Component Versions`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/versions") {
		    url {
		        parameters.append("model", "components")
		        parameters.append("model_id", "6826721")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Retrieve an array of component objects
     * https://www.storyblok.com/docs/api/management/components/retrieve-multiple-components
     */
    @Test
    fun `Retrieve Multiple Components`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/components/")
		
		println(response.body<JsonElement>())
    }

	/**
     * Update the values of a component
     * https://www.storyblok.com/docs/api/management/components/update-a-component
     */
    @Test
    fun `Update a Component`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/components/4123") {
		    setBody(buildJsonObject {
		        putJsonObject("component") {
		            put("display_name", null)
		            put("id", 4123)
		            put("is_nestable", true)
		            put("is_root", false)
		            put("name", "banner_section")
		            putJsonObject("schema") {
		                putJsonObject("headline") {
		                    put("description", "Use this field for the title")
		                    put("pos", 0)
		                    put("translatable", true)
		                    put("type", "text")
		                }
		            }
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

}