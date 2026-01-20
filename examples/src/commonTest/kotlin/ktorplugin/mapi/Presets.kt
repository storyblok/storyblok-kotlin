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
class Presets {

    /**
     * This endpoint can be used to create new presets.
     * https://www.storyblok.com/docs/api/management/presets/create-a-preset
     */
    @Test
    fun `Create a Preset`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/presets/") {
            setBody(buildJsonObject {
                putJsonObject("preset") {
                    put("component_id", 62)
                    put("name", "Teaser with filled headline")
                    putJsonObject("preset") {
                        put("headline", "This is a default value for the preset!")
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a preset by using its numeric id.
     * https://www.storyblok.com/docs/api/management/presets/delete-a-preset
     */
    @Test
    fun `Delete a Preset`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/presets/1814")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single preset object with a specific numeric id.
     * https://www.storyblok.com/docs/api/management/presets/retrieve-a-single-preset
     */
    @Test
    fun `Retrieve a Single Preset`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/presets/1814")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of preset objects.
     * https://www.storyblok.com/docs/api/management/presets/retrieve-multiple-presets
     */
    @Test
    fun `Retrieve Multiple Presets`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/presets")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint can be used to update presets using the numeric ID.
     * https://www.storyblok.com/docs/api/management/presets/update-a-preset
     */
    @Test
    fun `Update a Preset`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/presets/1814") {
            setBody(buildJsonObject {
                putJsonObject("preset") {
                    put("component_id", 62)
                    put("name", "Teaser with headline and image")
                    putJsonObject("preset") {
                        put("headline", "This is a default value for the preset!")
                        put("image", "//a.storyblok.com/f/606/...")
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}