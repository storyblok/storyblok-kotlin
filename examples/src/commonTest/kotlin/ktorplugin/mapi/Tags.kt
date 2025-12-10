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
class Tags {

	/**
     * You can create a tag, and optionally add it to a story.
     * https://www.storyblok.com/docs/api/management/tags/create-a-tag
     */
    @Test
    fun `Create a Tag`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/tags") {
		    setBody(buildJsonObject {
		        putJsonObject("tag") {
		            put("name", "Editor's Choice")
		            put("story_id", 202)
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a tag from a space.
     * https://www.storyblok.com/docs/api/management/tags/delete-a-tag
     */
    @Test
    fun `Delete a Tag`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.delete("spaces/288868932106293/stories/2141")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of tag objects from a space.
     * https://www.storyblok.com/docs/api/management/tags/retrieve-multiple-tags
     */
    @Test
    fun `Retrieve Multiple Tags`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/tags/") {
		    url {
		        parameters.append("search", "article")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of tag objects from a space.
     * https://www.storyblok.com/docs/api/management/tags/retrieve-multiple-tags
     */
    @Test
    fun `Retrieve Multiple Tags 2`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.get("spaces/288868932106293/stories/") {
		    url {
		        parameters.append("all_tags", "true")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint is used to add a tag to multiple stories at once.
     * https://www.storyblok.com/docs/api/management/tags/tag-bulk-association
     */
    @Test
    fun `Tag Bulk Association`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.post("spaces/288868932106293/tags/bulk_association") {
		    setBody(buildJsonObject {
		        putJsonObject("tags") {
		            putJsonArray("stories") {
		                addJsonObject {
		                    put("story_id", 69934114531566)
		                    putJsonArray("tag_list") {
		                        add("Editor's Choice")
		                        add("Featured")
		                    }
		                }
		            }
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to edit the name of a tag.
     * https://www.storyblok.com/docs/api/management/tags/update-a-tag
     */
    @Test
    fun `Update a Tag`() = runTest {

        val client = HttpClient {
		    install(Storyblok(MAPI)) {
		        accessToken = OAuth("YOUR_OAUTH_TOKEN")
		    }
			expectSuccess = false
		}
		
		val response = client.put("spaces/288868932106293/stories/2141") {
		    setBody(buildJsonObject {
		        put("id", "Editor's Choice")
		        putJsonObject("tag") {
		            put("name", "Editorial")
		        }
		    })
		}
		
		println(response.body<JsonElement>())
    }

}