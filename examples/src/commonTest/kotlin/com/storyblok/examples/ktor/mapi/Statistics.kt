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

class Statistics {

    /**
     * Returns API traffic usage statistics for the organization.
     * https://www.storyblok.com/docs/api/management/statistics/retrieve-api-traffic-statistics
     */
    @Test
    fun `Retrieve API Traffic Statistics`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me/statistics/all_traffic")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns assets traffic usage statistics for the organization.
     * https://www.storyblok.com/docs/api/management/statistics/retrieve-assets-traffic-statistics
     */
    @Test
    fun `Retrieve Assets Traffic Statistics`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me/statistics/assets_traffic")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of statistics for a specific month.
     * https://www.storyblok.com/docs/api/management/statistics/retrieve-statistics-by-date
     */
    @Test
    fun `Retrieve Statistics by Date`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/44203/statistics/2026-03-01")
        
        println(response.body<JsonElement>())
    }

}