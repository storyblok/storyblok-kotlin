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

class Stories {

    /**
     * This endpoint returns the story content, translated by AI.
     * https://www.storyblok.com/docs/api/management/stories/ai-translate
     */
    @Test
    fun `Translate a Story by AI`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
     * You can set most of the fields that are available in the story object, below we only list the properties in the example and the possible required fields. Stories are not published by default. If you want to create a published story add the parameter publish with the value true.
     * https://www.storyblok.com/docs/api/management/stories/create-a-story
     */
    @Test
    fun `Create a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/stories") {
            setBody(buildJsonObject {
                put("publish", true)
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/182794698822337") {
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
     * Retrieve the versions of a story.
     * https://www.storyblok.com/docs/api/management/stories/get-story-versions-new
     */
    @Test
    fun `Get Story Versions`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
    fun `Get Story Versions Legacy`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
    fun `Get Story Versions Legacy 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
     * Publishing a story (besides using the publish property via creation) can be done by sending a GET request for each story you want to publish with story_id using the following endpoint.Multiple language versions of a story can be published using the lang parameter (Publish translations individually has to be enabled in Settings &gt; Internationalization).
     * https://www.storyblok.com/docs/api/management/stories/publish-a-story
     */
    @Test
    fun `Publish a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
     * A paged endpoint that returns an array of story objects without content. Use supported parameters to filter stories.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories Management API`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/")
        
        println(response.body<JsonElement>())
    }

    /**
     * A paged endpoint that returns an array of story objects without content. Use supported parameters to filter stories.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories Management API 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
     * A paged endpoint that returns an array of story objects without content. Use supported parameters to filter stories.
     * https://www.storyblok.com/docs/api/management/stories/retrieve-multiple-stories
     */
    @Test
    fun `Retrieve Multiple Stories Management API 3`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
    fun `Retrieve a Single Story Management API`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/369689")
        
        println(response.body<JsonElement>())
    }

    /**
     * Unpublishing a story (besides using the unpublish action in visual editor or in content viewer) can be done by using a GET request for each story you want to unpublish. Multiple language versions of a story can be unpublished using the lang parameter (Publish translations individually has to be enabled in Settings &gt; Internationalization).
     * https://www.storyblok.com/docs/api/management/stories/unpublish-a-story
     */
    @Test
    fun `Unpublish a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
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
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/2141") {
            setBody(buildJsonObject {
                put("force_update", 1)
                put("publish", true)
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

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/88751/stories/64181051491446/export.json")
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.json") {
            url {
                parameters.append("lang_code", "sw")
                parameters.append("export_lang", "true")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON 3`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.json") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("export_lang", "true")
                parameters.append("version", "1")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON 4`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.json") {
            setBody(buildJsonObject {
                put("data", "{\"a5a68c3e-876d-4d5e-b08f-e000efafe41d:feature:name\":\"HiHallo\",\"d31919a3-a807-4274-9820-0bf87f7ba03c:page:text\":\"Blog Posts\",\"ffa153ea-8f63-467f-b370-65d5dd3d710f:paragraph:@richtext:richtext/content[0:paragraph].content[0].text\":\"\\\"Hark, what light through yonder editor breaks?\\\" Just our AI generating your next very epic storyline. \",\"ffa153ea-8f63-467f-b370-65d5dd3d710f:paragraph:@richtext:richtext/content[0:paragraph].content[2].text\":\"Speed up asset creation and balance gameplay, all with a few clicks. Give your small studio big-story power. #indiegamedev #ai #gamedevelopment\",\"language\":\"default\",\"page\":\"314931981\",\"text_nodes\":0,\"url\":\"home\"}")
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON 5`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.json") {
            url {
                parameters.append("lang_code", "es")
                parameters.append("import_lang", "true")
            }
            setBody(buildJsonObject {
                put("data", "{\"59ea90a7-c548-4d96-ae11-3d4ed40a5176:dp_richtext:richtext:content/content[0].content[0].text\":\"Translated content\",\"language\":\"es\",\"page\":\"314931981\",\"text_nodes\":1,\"url\":\"home\"}")
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as JSON.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-json-examples
     */
    @Test
    fun `Examples Export Import a story JSON 6`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.json") {
            url {
                parameters.append("version", "1")
            }
            setBody(buildJsonObject {
                put("data", "{\"a5a68c3e-876d-4d5e-b08f-e000efafe41d:feature:name\":\"Hallo! Release\",\"ffa153ea-8f63-467f-b370-65d5dd3d710f:paragraph:@richtext:richtext\":\"{\\\"type\\\":\\\"doc\\\",\\\"content\\\":[{\\\"type\\\":\\\"paragraph\\\",\\\"attrs\\\":{\\\"textAlign\\\":null},\\\"content\\\":[{\\\"text\\\":\\\"\\\\\\\"Hark, what light through yonder editor breaks?\\\\\\\" Just our AI generating your next very epic storyline. \\\",\\\"type\\\":\\\"text\\\"},{\\\"type\\\":\\\"emoji\\\",\\\"attrs\\\":{\\\"name\\\":\\\"wink\\\",\\\"emoji\\\":\\\"😉\\\",\\\"fallbackImage\\\":\\\"https://cdn.jsdelivr.net/npm/emoji-datasource-apple/img/apple/64/1f609.png\\\"}},{\\\"text\\\":\\\" Speed up asset creation and balance gameplay, all with a few clicks. Give your small studio big-story power. #indiegamedev #ai #gamedevelopment2\\\",\\\"type\\\":\\\"text\\\"}]}]}\",\"d31919a3-a807-4274-9820-0bf87f7ba03c:page:text\":\"Blog Posts\",\"page\":\"623949938\",\"language\":\"default\",\"url\":\"home\",\"text_nodes\":0}")
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.xml")
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML 2`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.xml") {
            url {
                parameters.append("lang_code", "it")
                parameters.append("export_lang", "true")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML 3`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/stories/314931981/export.xml") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("export_lang", "true")
                parameters.append("version", "1")
            }
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML 4`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.xml") {
            setBody(buildJsonObject {
                put("data", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<page text_nodes=\"0\" filename=\"home\" url=\"home\" id=\"314931981\" language=\"default\">\n<name>Home</name>\n<tags>\n  <tag id=\"ebf2eb36-2a3b-4231-b039-7a64178f50c2:feature:title\" type=\"STRING\">\n    <text><![CDATA[Home Page Title]]></text>\n  </tag>\n</tags>\n</page>")
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML 5`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.xml") {
            url {
                parameters.append("lang_code", "pt-br")
                parameters.append("import_lang", "true")
            }
            setBody(buildJsonObject {
                put("data", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<page text_nodes=\"0\" filename=\"home\" url=\"home\" id=\"314931981\" language=\"pt-br\">\n<name>Home</name>\n<tags>\n  <tag id=\"ebf2eb36-2a3b-4231-b039-7a64178f50c2:feature:title\" type=\"STRING\">\n    <text><![CDATA[Título traduzido]]></text>\n  </tag>\n  <tag id=\"ebf2eb36-2a3b-4231-b039-7a64178f50c2:feature:subtitle\" type=\"STRING\">\n    <text><![CDATA[Subtítulo traduzido]]></text>\n  </tag>\n</tags>\n</page>")
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Examples for exporting and importing stories as XML.
     * https://www.storyblok.com/docs/api/management/stories/examples/export-import-xml-examples
     */
    @Test
    fun `Examples Export Import a story XML 6`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/stories/314931981/import.xml") {
            url {
                parameters.append("version", "1")
            }
            setBody(buildJsonObject {
                put("data", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<page filename=\"home\" id=\"314931981\" language=\"default\">\n<name>Home</name>\n<tags>\n  <tag id=\"richtext\" type=\"STRING\">\n    <text><![CDATA[{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"text\":\"Example richtext content.\",\"type\":\"text\"}]}]}]]></text>\n  </tag>\n</tags>\n</page>")
            })
        }
        
        println(response.body<JsonElement>())
    }

}