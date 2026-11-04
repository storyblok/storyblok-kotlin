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

class ExternalAccounts {

    /**
     * Use this endpoint to obtain details of the GitHub account connected to Storyblok.
     * https://www.storyblok.com/docs/api/management/external-accounts/github
     */
    @Test
    fun GitHub() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("v1/auth/github/me")
        
        println(response.body<JsonElement>())
    }

}