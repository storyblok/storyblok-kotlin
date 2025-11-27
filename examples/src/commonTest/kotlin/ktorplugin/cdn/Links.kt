package ktorplugin.cdn

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

class Links {

	/**
     * Retrieve a concise representation of stories and folders using the links endpoint in the Content Delivery API.
     * https://www.storyblok.com/docs/api/content-delivery/v2/links/retrieve-multiple-links
     */
    @Test
    fun `Retrieve Multiple Links`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://api.storyblok.com/v2/cdn/")
		            parameters.append("token", "krcV6QGxWORpYLUWt12xKQtt")
		        }
		    }
		}
		
		val response = client.get("links") {
		    url {
		        parameters.append("version", "published")
		        parameters.append("starts_with", "articles")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}