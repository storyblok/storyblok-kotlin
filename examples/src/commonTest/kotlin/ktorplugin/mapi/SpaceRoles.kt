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
class SpaceRoles {

    /**
     * This endpoint allows you to create a new space role.
     * https://www.storyblok.com/docs/api/management/space-roles/create-a-space-role
     */
    @Test
    fun `Create a Space Role`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/space_roles/") {
            setBody(buildJsonObject {
                putJsonObject("space_role") {
                    putJsonArray("allowed_languages") {
                        add("default")
                        add("de")
                    }
                    putJsonArray("allowed_paths") {
                        add(43097198)
                        add(48581646)
                    }
                    putJsonArray("asset_folder_ids") {
                        add(56328)
                        add(29783)
                    }
                    putJsonArray("branch_ids") {
                        add(304011)
                    }
                    putJsonArray("component_ids") {
                        add(57584)
                        add(43743)
                        add(72760)
                        add(67535)
                    }
                    putJsonArray("datasource_ids") {
                        add(2189)
                    }
                    putJsonArray("field_permissions") {
                        add("article.title")
                        add("hero.image")
                    }
                    putJsonArray("permissions") {
                        add("manage_block_library")
                        add("deny_component_technical_name_update")
                        add("deny_component_fields_name_update")
                        add("edit_image")
                        add("delete_stories")
                        add("deploy_stories")
                        add("unpublish_stories")
                        add("unpublish_folders")
                        add("publish_stories")
                        add("publish_folders")
                        add("manage-non-translatable-fields")
                        add("manage_tags")
                    }
                    putJsonArray("readonly_field_permissions") {
                        add("hero.RichText_type")
                        add("hero.TextArea_type")
                    }
                    put("role", "Test role")
                    put("subtitle", "desc")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a space role using its numeric id.
     * https://www.storyblok.com/docs/api/management/space-roles/delete-a-space-role
     */
    @Test
    fun `Delete a Space Role`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/space_roles/18")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single, space role object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/space-roles/retrieve-a-single-space-role
     */
    @Test
    fun `Retrieve a Single Space Role`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/space_roles/18")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of space role objects.
     * https://www.storyblok.com/docs/api/management/space-roles/retrieve-multiple-space-roles
     */
    @Test
    fun `Retrieve Multiple Space Roles`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/space_roles/")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint allows you to update a space role by the numeric ID.
     * https://www.storyblok.com/docs/api/management/space-roles/update-a-space-role
     */
    @Test
    fun `Update a Space Role`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/space_roles/18") {
            setBody(buildJsonObject {
                putJsonObject("space_role") {
                    putJsonArray("allowed_languages") {
                        add("de")
                    }
                    putJsonArray("allowed_paths") {
                        add(430937198)
                    }
                    putJsonArray("asset_folder_ids") {
                        add(563628)
                    }
                    putJsonArray("branch_ids") {
                        add(30403)
                    }
                    putJsonArray("component_ids") {
                        add(5758347)
                    }
                    putJsonArray("datasource_ids") {
                        add(218499)
                    }
                    putJsonArray("field_permissions") {
                        add("a-new-blok.title")
                        add("A new comppppp.Text_type")
                        add("a-new-blok.image")
                        add("page.body")
                    }
                    putJsonArray("permissions") {
                        add("manage_block_library")
                        add("deny_component_technical_name_update")
                        add("deny_component_fields_name_update")
                        add("edit_image")
                        add("delete_stories")
                        add("deploy_stories")
                        add("unpublish_stories")
                        add("unpublish_folders")
                        add("publish_stories")
                        add("publish_folders")
                        add("manage-non-translatable-fields")
                    }
                    putJsonArray("readonly_field_permissions") {
                        add("A new comppppp.RichText_type")
                        add("A new comppppp.TextArea_type")
                        add("page.body")
                    }
                    put("role", "Another new space role")
                    put("subtitle", "new desc")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}