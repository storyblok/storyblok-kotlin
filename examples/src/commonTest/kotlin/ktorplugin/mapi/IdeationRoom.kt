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
class IdeationRoom {

    /**
     * This endpoint is to create an Idea in the Ideation Room. In the request body, passing name in the idea object is a minimum requirement.
     * https://www.storyblok.com/docs/api/management/ideation-room/create-an-idea
     */
    @Test
    fun `Create an Idea`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/ideas") {
            setBody(buildJsonObject {
                putJsonObject("idea") {
                    put("assignee", null)
                    putJsonObject("author") {
                        put("avatar", "avatars/67891/838dcb304c/avatar.jpg")
                        put("friendly_name", "Jon Doe")
                        put("id", 67891)
                        put("userid", "test@email.com")
                    }
                    putJsonArray("bookmarks") { }
                    putJsonObject("content") { }
                    put("deleted_at", null)
                    put("description", "First idea")
                    putJsonArray("internal_tag_ids") {
                        add("12345")
                    }
                    putJsonArray("internal_tags_list") {
                        addJsonObject {
                            put("id", 12345)
                            put("name", "docs")
                        }
                    }
                    put("is_private", true)
                    put("name", "My first idea")
                    put("status", "draft")
                    putJsonArray("stories") { }
                    putJsonArray("story_ids") { }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint allows the deletion of an idea using the uuid.
     * https://www.storyblok.com/docs/api/management/ideation-room/delete-an-idea
     */
    @Test
    fun `Delete an Idea`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/ideas/123ab45c-6d78-9101-11ef-213gh1i4j1k5")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint allows restoring an idea using the uuid. Use deleted idea's id value for idea_id. This endpoint also restores the idea's discussion comments.
     * https://www.storyblok.com/docs/api/management/ideation-room/restore-an-idea
     */
    @Test
    fun `Restore an Idea`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/ideas/123ab45c-6d78-9101-11ef-213gh1i4j1k5")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns discussions in an idea.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-discussions-in-idea
     */
    @Test
    fun `Retrieve Discussions in Idea`() = runTest {

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
     * Returns an array of idea objects.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-multiple-ideas
     */
    @Test
    fun `Retrieve Multiple Ideas`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/ideas/")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single idea object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/ideation-room/retrieve-one-idea
     */
    @Test
    fun `Retrieve One Idea`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/ideas/1a2b3456-c7d8-9ef1-gh01-11i2jk13l14m")
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an idea using an idea uuid. In the request body, it's required to pass the idea object.
     * https://www.storyblok.com/docs/api/management/ideation-room/update-an-idea
     */
    @Test
    fun `Update an Idea`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/ideas/ab123cd4-5e6f-7gh8-9ij1-01k112l13m1n") {
            setBody(buildJsonObject {
                putJsonObject("idea") {
                    put("assignee", null)
                    putJsonObject("author") {
                        put("avatar", "avatars/67891/838dcb304c/avatar.jpg")
                        put("friendly_name", "Jon Doe")
                        put("id", 67891)
                        put("userid", "test@email.com")
                    }
                    putJsonArray("bookmarks") { }
                    putJsonObject("content") { }
                    put("deleted_at", null)
                    put("description", "First idea")
                    putJsonArray("internal_tag_ids") {
                        add("12345")
                    }
                    putJsonArray("internal_tags_list") {
                        addJsonObject {
                            put("id", 12345)
                            put("name", "docs")
                        }
                    }
                    put("is_private", true)
                    put("name", "My first idea")
                    put("status", "draft")
                    putJsonArray("stories") { }
                    putJsonArray("story_ids") { }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}