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
class Stories {

    /**
     * This endpoint returns the story content, translated by AI.
     * https://www.storyblok.com/docs/api/management/stories/ai-translate
     */
    @Test
    fun `Translate a Story by AI`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/536503907/ai_translate") {
            setBody(buildJsonObject {
                put("code", "fr")
                put("lang", "fr")
                put("overwrite", true)
                put("release_id", 0)
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * With this endpoint you can compare the changes between two versions of a story in Storyblok. You need to provide the story ID and version ID in the request to retrieve the comparison results.
     * https://www.storyblok.com/docs/api/management/stories/compare-a-story-version
     */
    @Test
    fun `Compare a Story Version`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/267/compare") {
            url {
                parameters.append("version", "151")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * You can set most of the fields that are available in the story object, below we only list the properties in the example and the possible required fields. Stories are not published by default. If you want to create a published story add the parameter publish with the value 1.
     * https://www.storyblok.com/docs/api/management/stories/create-a-story
     */
    @Test
    fun `Create a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories") {
            setBody(buildJsonObject {
                put("publish", 1)
                putJsonObject("story") {
                    putJsonObject("content") {
                        putJsonArray("body") { }
                        put("component", "page")
                    }
                    put("name", "Story Name")
                    put("slug", "story-name")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Use the Story endpoint to create and manage content folders.
     * https://www.storyblok.com/docs/api/management/stories/create-and-manage-folders
     */
    @Test
    fun `Create and Manage Folders`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories") {
            setBody(buildJsonObject {
                putJsonObject("story") {
                    put("is_folder", true)
                    put("name", "A new folder")
                    put("parent_id", 0)
                    put("slug", "a-new-folder")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Use the Story endpoint to create and manage content folders.
     * https://www.storyblok.com/docs/api/management/stories/create-and-manage-folders
     */
    @Test
    fun `Create and Manage Folders 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories") {
            setBody(buildJsonObject {
                putJsonObject("story") {
                    put("default_root", "article")
                    put("is_folder", true)
                    put("name", "A new folder")
                    put("parent_id", 0)
                    put("slug", "a-new-folder")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Use the Story endpoint to create and manage content folders.
     * https://www.storyblok.com/docs/api/management/stories/create-and-manage-folders
     */
    @Test
    fun `Create and Manage Folders 3`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories") {
            setBody(buildJsonObject {
                putJsonObject("story") {
                    putJsonObject("content") {
                        putJsonArray("content_types") {
                            add("category")
                        }
                        put("lock_subfolders_content_types", false)
                    }
                    put("is_folder", true)
                    put("name", "Categories")
                    put("parent_id", 0)
                    put("slug", "categories")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a content entry by using its numeric id.
     * https://www.storyblok.com/docs/api/management/stories/delete-a-story
     */
    @Test
    fun `Delete a Story`() = runTest {

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
     * This endpoint can be used to duplicate a story into another folder.
     * https://www.storyblok.com/docs/api/management/stories/duplicate-a-story
     */
    @Test
    fun `Duplicate a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/531458099/duplicate") {
            setBody(buildJsonObject {
                put("same_path", true)
                putJsonObject("story") {
                    put("group_id", "4f77133f-bb1c-4799-a54d-b6217107247f")
                }
                put("target_dimension", 531452775)
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Exporting a story can be done using a GET request for each story you want to export.
     * https://www.storyblok.com/docs/api/management/stories/export-a-story
     */
    @Test
    fun `Export a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.json") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("export_lang", "true")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Exporting a story can be done using a GET request for each story you want to export.
     * https://www.storyblok.com/docs/api/management/stories/export-a-story
     */
    @Test
    fun `Export a Story 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.json") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("export_lang", "true")
                parameters.append("version", "2")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve the versions of a story.
     * https://www.storyblok.com/docs/api/management/stories/get-story-versions-new
     */
    @Test
    fun `Get Story Versions`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/story_versions") {
            url {
                parameters.append("by_story_id", "174957")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This allows you to retrieve the versions of a story and the corresponding author information. You can also filter the results based on pagination using the page parameter. This can be done with a GET request on the story version you wish to retrieve.
     * https://www.storyblok.com/docs/api/management/stories/get-story-versions
     */
    @Test
    fun `Get Story Versions (Legacy)`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/123/versions")
        
        println(response.body<JsonElement>())
    }

    /**
     * This allows you to retrieve the versions of a story and the corresponding author information. You can also filter the results based on pagination using the page parameter. This can be done with a GET request on the story version you wish to retrieve.
     * https://www.storyblok.com/docs/api/management/stories/get-story-versions
     */
    @Test
    fun `Get Story Versions (Legacy) 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/123/versions") {
            url {
                parameters.append("page", "2")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint is used to get unpublished dependencies of a story.
     * https://www.storyblok.com/docs/api/management/stories/get-unpublished-dependencies
     */
    @Test
    fun `Get Unpublished Dependencies`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories/unpublished_dependencies") {
            setBody(buildJsonObject {
                putJsonArray("story_ids") {
                    add(522672112)
                    add(534980620)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Importing a story can be done using a PUT request for each story you want to import.
     * https://www.storyblok.com/docs/api/management/stories/import-a-story
     */
    @Test
    fun `Import a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.json") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("import_lang", "true")
            }
            setBody(buildJsonObject {
                putJsonObject("story") {
                    putJsonArray("alternates") { }
                    putJsonArray("breadcrumbs") { }
                    put("can_not_view", null)
                    putJsonObject("content") {
                        put("_uid", "98cccd01-f807-4494-996d-c6b0de2045a5")
                        put("component", "your_content_type")
                    }
                    put("created_at", "2023-05-29T09:53:40.231Z")
                    put("default_root", null)
                    put("deleted_at", null)
                    put("disble_fe_editor", false)
                    put("expire_at", null)
                    putJsonArray("favourite_for_user_ids") { }
                    put("first_published_at", "2023-06-06T08:47:05.426Z")
                    put("full_slug", "home")
                    put("group_id", "fb33b858-277f-4690-81fb-e0a080bd39ac")
                    put("id", 314931981)
                    put("imported_at", "2024-02-08T11:26:42.505Z")
                    put("is_folder", false)
                    put("is_scheduled", null)
                    put("is_startpage", false)
                    putJsonObject("last_author") {
                        put("friendly_name", "Storyblok")
                        put("id", 39821)
                        put("userid", "storyblok")
                    }
                    putJsonArray("localized_paths") {
                        addJsonObject { }
                    }
                    put("meta_data", null)
                    put("name", "Home")
                    put("parent", null)
                    put("parent_id", 0)
                    put("path", null)
                    put("pinned", false)
                    put("position", 0)
                    putJsonObject("preview_token") {
                        put("timestamp", "1545530576")
                        put("token", "279395174a25be38b702f9ec90d08a960e1a5a84")
                    }
                    put("publish_at", null)
                    put("published", true)
                    put("published_at", "2023-08-30T09:16:42.066Z")
                    put("scheduled_dates", null)
                    put("slug", "home")
                    put("sort_by_date", null)
                    putJsonArray("space_role_ids") { }
                    putJsonArray("tag_list") { }
                    putJsonArray("translated_slugs") {
                        addJsonObject { }
                    }
                    putJsonArray("translated_stories") { }
                    put("unpublished_changes", true)
                    put("updated_at", "2024-02-08T11:26:42.514Z")
                    putJsonArray("user_ids") {
                        add(12345)
                    }
                    put("uuid", "2497c493-168a-443f-bbb1-ccfd6340d319")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Publishing a story (besides using the publish property via creation) can be done by sending a GET request for each story you want to publish with story_id using the following endpoint.Multiple language versions of a story can be published using the lang parameter (Publish translations individually has to be enabled in Settings > Internationalization).
     * https://www.storyblok.com/docs/api/management/stories/publish-a-story
     */
    @Test
    fun `Publish a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/2141/publish") {
            url {
                parameters.append("lang", "de")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint lets you restore a story to a specific version.
     * https://www.storyblok.com/docs/api/management/stories/restore-a-story-version
     */
    @Test
    fun `Restore a Story Version`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/623949938/restore_with") {
            url {
                parameters.append("version", "55648825")
                parameters.append("versions_v2", "true")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/") {
            url {
                parameters.append("text_search", "My fulltext search")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories 3`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/") {
            url {
                parameters.append("by_uuids", "fb3afwa58-277f-4690-81fb-e0a080bd39ac,81fb81fb-e9fa-42b5-b952-c7d96ab6099d")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint returns a single, fully loaded story object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-one-story
     */
    @Test
    fun `Retrieve One Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/369689")
        
        println(response.body<JsonElement>())
    }

    /**
     * Unpublishing a story (besides using the unpublish action in visual editor or in content viewer) can be done by using a GET request for each story you want to unpublish. Multiple language versions of a story can be unpublished using the lang parameter (Publish translations individually has to be enabled in Settings > Internationalization).
     * https://www.storyblok.com/docs/api/management/stories/unpublish-a-story
     */
    @Test
    fun `Unpublish a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/2141/unpublish")
        
        println(response.body<JsonElement>())
    }

    /**
     * Use this endpoint for migrations, updates (new component structure, and more), or bulk actions
     * https://www.storyblok.com/docs/api/management/stories/update-a-story
     */
    @Test
    fun `Update a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/2141") {
            setBody(buildJsonObject {
                put("force_update", 1)
                put("publish", 1)
                putJsonObject("story") {
                    putJsonObject("content") {
                        putJsonArray("body") { }
                        put("component", "page")
                    }
                    put("id", 2141)
                    put("name", "Updated Story Name")
                    put("slug", "story-name")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}