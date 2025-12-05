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

class Activities {

	/**
     * Returns a single activity object with a specific numeric ID. Every response contains two extra keys, one called trackable, that contains data about the changed object and the other called user that contains the user information.
     * https://www.storyblok.com/docs/api/management/activities/retrieve-a-single-activity
     */
    @Test
    fun `Retrieve a Single Activity`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/activities/1234312323")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of activity objects, along with trackable and user objects. Can be filtered on date ranges and is paged.
     * https://www.storyblok.com/docs/api/management/activities/retrieve-multiple-activities
     */
    @Test
    fun `Retrieve Multiple Activities`() = runTest {

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
		
		val response = client.get("spaces/288868932106293/activities/") {
		    url {
		        parameters.append("created_at_gte", "2018-12-14")
		        parameters.append("created_at_lte", "2018-12-18")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}