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

class Webhooks {

    /**
     * Create a new webhook in the specified space.
     * https://www.storyblok.com/docs/api/management/webhooks/add-a-webhook
     */
    @Test
    fun `Add a Webhook`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/webhook_endpoints/") {
            setBody(buildJsonObject {
                putJsonObject("webhook_endpoint") {
                    putJsonArray("actions") {
                        add("story.published")
                    }
                    put("activated", true)
                    put("endpoint", "https://example.com")
                    put("name", "Rebuild Website")
                    put("secret", "")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a webhook by providing its numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/delete-a-webhook
     */
    @Test
    fun `Delete a Webhook`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/webhook_endpoints/4573")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single webhook object by providing a specific numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/retrieve-a-single-webhook
     */
    @Test
    fun `Retrieve a Single Webhook`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/webhook_endpoints/4570")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of webhook objects.
     * https://www.storyblok.com/docs/api/management/webhooks/retrieve-multiple-webhooks
     */
    @Test
    fun `Retrieve Multiple Webhooks`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/webhook_endpoints/")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of event actions that can be subscribed to for webhooks.
     * https://www.storyblok.com/docs/api/management/webhooks/retrieve-webhook-actions
     */
    @Test
    fun `Retrieve Webhook Actions`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/webhook_endpoints/allowed_actions")
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an existing webhook by providing its numeric ID.
     * https://www.storyblok.com/docs/api/management/webhooks/update-a-webhook
     */
    @Test
    fun `Update a Webhook`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/webhook_endpoints/4570") {
            setBody(buildJsonObject {
                putJsonObject("webhook_endpoint") {
                    putJsonArray("actions") {
                        add("story.published")
                        add("story.unpublished")
                    }
                    put("activated", true)
                    put("endpoint", "https://example.com")
                    put("name", "Rebuild Website")
                    put("secret", "HelloSecret")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}