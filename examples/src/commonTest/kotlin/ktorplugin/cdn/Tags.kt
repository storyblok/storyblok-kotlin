package ktorplugin.cdn

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test

class Tags {

	/**
     * Retrieve tags used in a space.
     * https://www.storyblok.com/docs/api/content-delivery/v2/tags/retrieve-multiple-tags
     */
    @Test
    fun `Retrieve Multiple Tags`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("tags") {
		    url {
		        parameters.append("starts_with", "articles/")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}