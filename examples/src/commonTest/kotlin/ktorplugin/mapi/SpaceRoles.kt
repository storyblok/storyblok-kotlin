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

class SpaceRoles {

	/**
     * This endpoint allows you to create a new space role.
     * https://www.storyblok.com/docs/api/management/space-roles/create-a-space-role
     */
    @Test
    fun `Create a Space Role`() = runTest {

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
		
		val response = client.post("spaces/606/space_roles/") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"space_role":{"allowed_languages":["default","de"],"allowed_paths":[43097198,48581646],"asset_folder_ids":[56328,29783],"branch_ids":[304011],"component_ids":[57584,43743,72760,67535],"datasource_ids":[2189],"field_permissions":["article.title","hero.image"],"permissions":["manage_block_library","deny_component_technical_name_update","deny_component_fields_name_update","edit_image","delete_stories","deploy_stories","unpublish_stories","unpublish_folders","publish_stories","publish_folders","manage-non-translatable-fields","manage_tags"],"readonly_field_permissions":["hero.RichText_type","hero.TextArea_type"],"role":"Test role","subtitle":"desc"}}""")
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
			expectSuccess = true
		    install(ContentNegotiation) { json() }
		    install(DefaultRequest) {
		        url {
		            takeFrom("https://mapi.storyblok.com/v1/")
		            headers.append("Authorization", "YOUR_OAUTH_TOKEN")
		        }
		    }
		}
		
		val response = client.delete("spaces/606/space_roles/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns a single, space role object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/space-roles/retrieve-a-single-space-role
     */
    @Test
    fun `Retrieve a Single Space Role`() = runTest {

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
		
		val response = client.get("spaces/606/space_roles/18")
		
		println(response.body<JsonElement>())
    }

	/**
     * Returns an array of space role objects.
     * https://www.storyblok.com/docs/api/management/space-roles/retrieve-multiple-space-roles
     */
    @Test
    fun `Retrieve Multiple Space Roles`() = runTest {

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
		
		val response = client.get("spaces/606/space_roles/")
		
		println(response.body<JsonElement>())
    }

	/**
     * This endpoint allows you to update a space role by the numeric ID.
     * https://www.storyblok.com/docs/api/management/space-roles/update-a-space-role
     */
    @Test
    fun `Update a Space Role`() = runTest {

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
		
		val response = client.put("spaces/606/space_roles/18") {
		    contentType(ContentType.Application.Json)
		    setBody("""{"space_role":{"allowed_languages":["de"],"allowed_paths":[430937198],"asset_folder_ids":[563628],"branch_ids":[30403],"component_ids":[5758347],"datasource_ids":[218499],"field_permissions":["a-new-blok.title","A new comppppp.Text_type","a-new-blok.image","page.body"],"permissions":["manage_block_library","deny_component_technical_name_update","deny_component_fields_name_update","edit_image","delete_stories","deploy_stories","unpublish_stories","unpublish_folders","publish_stories","publish_folders","manage-non-translatable-fields"],"readonly_field_permissions":["A new comppppp.RichText_type","A new comppppp.TextArea_type","page.body"],"role":"Another new space role","subtitle":"new desc"}}""")
		}
		
		println(response.body<JsonElement>())
    }

}