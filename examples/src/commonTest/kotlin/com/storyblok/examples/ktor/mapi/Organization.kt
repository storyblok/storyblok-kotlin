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

class Organization {

    /**
     * Returns the organization object for the currently authenticated user, including users, spaces, invitations, and pricing plan details.
     * https://www.storyblok.com/docs/api/management/organization/retrieve-current-organization
     */
    @Test
    fun `Retrieve Current Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me")
        
        println(response.body<JsonElement>())
    }

}