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
class Discussions {

    /**
     * This endpoint allows the creation of a comment in a particular discussion using the ID.
     * https://www.storyblok.com/docs/api/management/discussions/create-a-comment
     */
    @Test
    fun `Create a Comment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/discussions/456/comments") {
            setBody(buildJsonObject {
                putJsonObject("comment") {
                    putJsonArray("message_json") {
                        addJsonObject {
                            put("text", "Hello new comment")
                            put("type", "text")
                        }
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint creates a new discussion.
     * https://www.storyblok.com/docs/api/management/discussions/create-a-discussion
     */
    @Test
    fun `Create a Discussion`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories/12367/discussions") {
            setBody(buildJsonObject {
                putJsonObject("discussion") {
                    put("block_uid", "f7bd92e3-b309-4441-a8a0-654e499fefc8")
                    putJsonObject("comment") {
                        putJsonArray("message_json") {
                            addJsonObject {
                                put("text", "this is a comment ")
                                put("type", "text")
                            }
                            addJsonObject {
                                putJsonObject("attrs") {
                                    put("id", 99734)
                                    put("label", "Fortune Ikechi")
                                }
                                put("type", "mention")
                            }
                        }
                    }
                    put("component", "feature")
                    put("fieldname", "name")
                    put("lang", "default")
                    put("title", "Name")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint allows deletion of a comment using the numeric ID.
     * https://www.storyblok.com/docs/api/management/discussions/delete-a-comment
     */
    @Test
    fun `Delete a Comment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/discussions/456/comments/789")
        
        println(response.body<JsonElement>())
    }

    /**
     * Resolves a comment in a discussion.
     * https://www.storyblok.com/docs/api/management/discussions/resolve-a-discussion
     */
    @Test
    fun `Resolve a Discussion`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/discussions/49468") {
            setBody(buildJsonObject {
                putJsonObject("discussion") {
                    put("solved_at", "2024-02-06T22:07:04.729Z")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Get a specific discussion.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-a-specific-discussion
     */
    @Test
    fun `Retrieve a Specific Discussion`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/discussions/49473")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns comments for specific idea discussions from the Ideation Room.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-idea-discussions-comments
     */
    @Test
    fun `Retrieve Idea Discussions Comments`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/ideas/1a2b3456-c7d8-9ef1-gh01-11i2jk13l14m/discussions")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve multiple comments from a specific discussion
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-multiple-comments-from-a-specific-discussion
     */
    @Test
    fun `Retrieve Multiple Comments from a Specific Discussion`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/discussions/49471/comments")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of discussion objects present inside a particular story. This endpoint is paged and can be filtered by using page=1 , status and per_page=1 for retrieving discussions per page.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-multiple-discussions
     */
    @Test
    fun `Retrieve Multiple Discussions`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/1234/discussions") {
            url {
                parameters.append("per_page", "1")
                parameters.append("page", "1")
                parameters.append("by_status", "unsolved")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve your mentioned discussions. The response is paged.
     * https://www.storyblok.com/docs/api/management/discussions/retrieve-my-discussions
     */
    @Test
    fun `Retrieve My Discussions`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/mentioned_discussions/me")
        
        println(response.body<JsonElement>())
    }

    /**
     * Update comments in a particular discussion using the discussion ID and comment ID
     * https://www.storyblok.com/docs/api/management/discussions/update-a-comment
     */
    @Test
    fun `Update a Comment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/discussions/2345/comments/456") {
            setBody(buildJsonObject {
                putJsonObject("comment") {
                    putJsonArray("message_json") {
                        addJsonObject {
                            put("text", "Updated Comment ")
                            put("type", "text")
                        }
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}