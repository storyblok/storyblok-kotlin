package ktorplugin.cdn

import com.storyblok.ktor.Api.*
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
     * Retrieve a single datasource by ID using Storyblok's Content Delivery API to access key-value pairs for options and settings.
     * https://www.storyblok.com/docs/api/content-delivery/v2/datasources/retrieve-a-single-datasource
     */
    @Test
    fun `Retrieve a Single Datasource`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("datasources/product-labels")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve multiple datasource entries with filtering by datasource and dimension using Storyblok's Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/datasources/retrieve-multiple-datasource-entries
     */
    @Test
    fun `Retrieve Multiple Datasource Entries`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("datasource_entries/") {
            url {
                parameters.append("datasource", "product-labels")
                parameters.append("dimension", "de")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve all datasources from your Storyblok space with pagination support using the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/datasources/retrieve-multiple-datasources
     */
    @Test
    fun `Retrieve Multiple Datasources`() = runTest {

        val client = HttpClient {
            install(Storyblok(CDN)) {
                accessToken = "ask9soUkv02QqbZgmZdeDAtt"
            }
        }
        
        val response = client.get("datasources")
        
        println(response.body<JsonElement>())
    }

}