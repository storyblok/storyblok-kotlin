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

class Stories {

	/**
     * This endpoint returns the story content, translated by AI.
     */
    @Test
    fun `Translate a Story by AI`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/stories/536503907/ai_translate") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "code": "fr",
		      "lang": "fr",
		      "overwrite": true,
		      "release_id": 0
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * With this endpoint you can compare the changes between two versions of a story in Storyblok. You need to provide the story ID and version ID in the request to retrieve the comparison results.
     */
    @Test
    fun `Compare a Story Version`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/185/stories/267/compare") {
		    url {
		        parameters.append("version", "151")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * You can set most of the fields that are available in the story object, below we only list the properties in the example and the possible required fields. Stories are not published by default. If you want to create a published story add the parameter publish with the value 1.
     */
    @Test
    fun `Create a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/stories") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "publish": 1,
		      "story": {
		        "content": {
		          "body": [],
		          "component": "page"
		        },
		        "name": "Story Name",
		        "slug": "story-name"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Use the Story endpoint to create and manage content folders.
     */
    @Test
    fun `Create and Manage Folders`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/stories") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "story": {
		        "is_folder": true,
		        "name": "A new folder",
		        "parent_id": 0,
		        "slug": "a-new-folder"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Use the Story endpoint to create and manage content folders.
     */
    @Test
    fun `Create and Manage Folders 2`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/606/stories") {
		    contentType(ContentType.Application.Json)
		    setBody(""""{\n  story: {\n    default_root: \"article\",\n    is_folder: true,\n    name: \"A new folder\",\n    parent_id: 0,\n    slug: \"a-new-folder\",\n  },\n}"""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Use the Story endpoint to create and manage content folders.
     */
    @Test
    fun `Create and Manage Folders 3`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/stories") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "story": {
		        "content": {
		          "content_types": [
		            "category"
		          ],
		          "lock_subfolders_content_types": false
		        },
		        "is_folder": true,
		        "name": "Categories",
		        "parent_id": 0,
		        "slug": "categories"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Delete a content entry by using its numeric id.
     */
    @Test
    fun `Delete a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/606/stories/2141")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint can be used to duplicate a story into another folder.
     */
    @Test
    fun `Duplicate a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/296898/stories/531458099/duplicate") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "same_path": true,
		      "story": {
		        "group_id": "4f77133f-bb1c-4799-a54d-b6217107247f"
		      },
		      "target_dimension": 531452775
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Exporting a story can be done using a GET request for each story you want to export.
     */
    @Test
    fun `Export a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/233027/stories/314931981/export.json") {
		    url {
		        parameters.append("lang_code", "pt-br")
		        parameters.append("export_lang", "true")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Exporting a story can be done using a GET request for each story you want to export.
     */
    @Test
    fun `Export a Story 2`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/233027/stories/314931981/export.json") {
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
     */
    @Test
    fun `Get Story Versions`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/302787/story_versions") {
		    url {
		        parameters.append("by_story_id", "174957")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This allows you to retrieve the versions of a story and the corresponding author information. You can also filter the results based on pagination using the page parameter. This can be done with a GET request on the story version you wish to retrieve.
     */
    @Test
    fun `Get Story Versions (Legacy)`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/123/versions")
		
		println(response.body<JsonElement>())
    }

	/**
     * This allows you to retrieve the versions of a story and the corresponding author information. You can also filter the results based on pagination using the page parameter. This can be done with a GET request on the story version you wish to retrieve.
     */
    @Test
    fun `Get Story Versions (Legacy) 2`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/123/versions") {
		    url {
		        parameters.append("page", "2")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint is used to get unpublished dependencies of a story.
     */
    @Test
    fun `Get Unpublished Dependencies`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.post("spaces/296898/stories/unpublished_dependencies") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "story_ids": [
		        522672112,
		        534980620
		      ]
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Importing a story can be done using a PUT request for each story you want to import.
     */
    @Test
    fun `Import a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/233027/stories/314931981/import.json") {
		    url {
		        parameters.append("lang_code", "pt-br")
		        parameters.append("import_lang", "true")
		    }
		    contentType(ContentType.Application.Json)
		    setBody("""{"story":{"alternates":[],"breadcrumbs":[],"can_not_view":null,"content":{"_uid":"98cccd01-f807-4494-996d-c6b0de2045a5","component":"your_content_type"},"created_at":"2023-05-29T09:53:40.231Z","default_root":null,"deleted_at":null,"disble_fe_editor":false,"expire_at":null,"favourite_for_user_ids":[],"first_published_at":"2023-06-06T08:47:05.426Z","full_slug":"home","group_id":"fb33b858-277f-4690-81fb-e0a080bd39ac","id":314931981,"imported_at":"2024-02-08T11:26:42.505Z","is_folder":false,"is_scheduled":null,"is_startpage":false,"last_author":{"friendly_name":"Storyblok","id":39821,"userid":"storyblok"},"localized_paths":[{}],"meta_data":null,"name":"Home","parent":null,"parent_id":0,"path":null,"pinned":false,"position":0,"preview_token":{"timestamp":"1545530576","token":"279395174a25be38b702f9ec90d08a960e1a5a84"},"publish_at":null,"published":true,"published_at":"2023-08-30T09:16:42.066Z","scheduled_dates":null,"slug":"home","sort_by_date":null,"space_role_ids":[],"tag_list":[],"translated_slugs":[{}],"translated_stories":[],"unpublished_changes":true,"updated_at":"2024-02-08T11:26:42.514Z","user_ids":[12345],"uuid":"2497c493-168a-443f-bbb1-ccfd6340d319"}}""")
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Publishing a story (besides using the publish property via creation) can be done by sending a GET request for each story you want to publish with story_id using the following endpoint.Multiple language versions of a story can be published using the lang parameter (Publish translations individually has to be enabled in Settings > Internationalization).
     */
    @Test
    fun `Publish a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/2141/publish") {
		    url {
		        parameters.append("lang", "de")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint lets you restore a story to a specific version.
     */
    @Test
    fun `Restore a Story Version`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/325428/stories/623949938/restore_with") {
		    url {
		        parameters.append("version", "55648825")
		        parameters.append("versions_v2", "true")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     */
    @Test
    fun `Retrieve Multiple Stories`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     */
    @Test
    fun `Retrieve Multiple Stories 2`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/") {
		    url {
		        parameters.append("text_search", "My fulltext search")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns an array of story objects without content. Stories can be filtered with the parameters below. The response is paged.
     */
    @Test
    fun `Retrieve Multiple Stories 3`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/") {
		    url {
		        parameters.append("by_uuids", "fb3afwa58-277f-4690-81fb-e0a080bd39ac,81fb81fb-e9fa-42b5-b952-c7d96ab6099d")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint returns a single, fully loaded story object by providing a specific numeric id.
     */
    @Test
    fun `Retrieve One Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/369689")
		
		println(response.body<JsonElement>())
    }

	/**
     * Unpublishing a story (besides using the unpublish action in visual editor or in content viewer) can be done by using a GET request for each story you want to unpublish. Multiple language versions of a story can be unpublished using the lang parameter (Publish translations individually has to be enabled in Settings > Internationalization).
     */
    @Test
    fun `Unpublish a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.get("spaces/606/stories/2141/unpublish")
		
		println(response.body<JsonElement>())
    }

	/**
     * Use this endpoint for migrations, updates (new component structure, and more), or bulk actions
     */
    @Test
    fun `Update a Story`() = runTest {

        val client = HttpClient {
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.put("spaces/606/stories/2141") {
		    contentType(ContentType.Application.Json)
		    setBody("""{
		      "force_update": 1,
		      "publish": 1,
		      "story": {
		        "content": {
		          "body": [],
		          "component": "page"
		        },
		        "id": 2141,
		        "name": "Updated Story Name",
		        "slug": "story-name"
		      }
		    }""")
		}
		
		println(response.body<JsonElement>())
    }

}