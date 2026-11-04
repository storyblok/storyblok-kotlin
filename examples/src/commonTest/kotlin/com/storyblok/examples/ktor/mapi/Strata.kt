package com.storyblok.examples.ktor.mapi

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Api.Config.Management.AccessToken.Personal
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.Test

class Strata {

    /**
     * Returns a ranked list of stories whose content matches a search term by meaning.
     * https://www.storyblok.com/docs/api/management/strata/search
     */
    @Test
    fun `Search Management API`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/strata/search") {
            url {
                parameters.append("q", "how do I return an order")
                parameters.append("version", "published")
                parameters.append("limit", "5")
            }
        }
        
        println(response.body<JsonElement>())
    }

}