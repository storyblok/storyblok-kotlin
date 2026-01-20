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
class Datasources {

    /**
     * Create a new datasource
     * https://www.storyblok.com/docs/api/management/datasources/create-a-datasource
     */
    @Test
    fun `Create a Datasource`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/datasources/") {
            setBody(buildJsonObject {
                putJsonObject("datasource") {
                    put("name", "Labels for Website")
                    put("slug", "labels")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Create a new datasource
     * https://www.storyblok.com/docs/api/management/datasources/create-a-datasource
     */
    @Test
    fun `Create a Datasource 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/datasources/") {
            setBody(buildJsonObject {
                putJsonObject("datasource") {
                    putJsonArray("dimensions_attributes") {
                        addJsonObject {
                            put("entry_value", "es")
                            put("name", "Spanish")
                        }
                        addJsonObject {
                            put("entry_value", "de")
                            put("name", "German")
                        }
                    }
                    put("name", "Labels for Website")
                    put("slug", "label")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a datasource using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/delete-a-datasource
     */
    @Test
    fun `Delete a Datasource`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/datasources/91")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve a single datasource using its numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/retrieve-a-single-datasource
     */
    @Test
    fun `Retrieve a Single Datasource`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/datasources/91")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a paginated array of datasource objects
     * https://www.storyblok.com/docs/api/management/datasources/retrieve-multiple-datasources
     */
    @Test
    fun `Retrieve Multiple Datasources`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/datasources/") {
            url {
                parameters.append("search", "Labels for Website")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Update a datasource using it numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/update-a-datasource
     */
    @Test
    fun `Update a Datasource`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/datasources/91") {
            setBody(buildJsonObject {
                putJsonObject("datasource") {
                    put("name", "Labels for Website")
                    put("slug", "labels_for_website")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Update a datasource using it numeric ID
     * https://www.storyblok.com/docs/api/management/datasources/update-a-datasource
     */
    @Test
    fun `Update a Datasource 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/datasources/91") {
            setBody(buildJsonObject {
                putJsonObject("datasource") {
                    putJsonArray("dimensions_attributes") {
                        addJsonObject {
                            put("entry_value", "another_slug")
                            put("name", "Another Name")
                        }
                    }
                    put("name", "Labels for Website")
                    put("slug", "label")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}