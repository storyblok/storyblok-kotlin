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
class WorkflowStage {

    /**
     * This endpoint allows you to create a workflow stage.
     * https://www.storyblok.com/docs/api/management/workflow-stage/create-a-workflow-stage
     */
    @Test
    fun `Create a Workflow Stage`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/space_id/workflow_stages") {
            setBody(buildJsonObject {
                putJsonObject("workflow_stage") {
                    put("after_publish_id", 561398)
                    put("allow_admin_change", true)
                    put("allow_admin_publish", true)
                    put("allow_all_stages", false)
                    put("allow_all_users", false)
                    put("allow_editor_change", false)
                    put("allow_publish", true)
                    put("color", "#2d3v22")
                    put("is_default", false)
                    put("name", "testb")
                    put("position", 3)
                    putJsonArray("space_role_ids") {
                        add(111111)
                        add(222222)
                    }
                    putJsonArray("user_ids") {
                        add(123123)
                    }
                    put("workflow_id", 43112)
                    putJsonArray("workflow_stage_ids") {
                        add(561398)
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete a workflow stage using its numeric ID.
     * https://www.storyblok.com/docs/api/management/workflow-stage/delete-a-workflow-stage
     */
    @Test
    fun `Delete a Workflow Stage`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/workflow_stages/18")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns a single workflow stage object by providing a specific numeric id.
     * https://www.storyblok.com/docs/api/management/workflow-stage/retrieve-a-single-workflow-stage
     */
    @Test
    fun `Retrieve a Single Workflow Stage`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/workflow_stages/18")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of workflow stages.
     * https://www.storyblok.com/docs/api/management/workflow-stage/retrieve-multiple-workflow-stages
     */
    @Test
    fun `Retrieve Multiple Workflow Stages`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/workflow_stages/")
        
        println(response.body<JsonElement>())
    }

    /**
     * This endpoint can be used to update a workflow stage using the numeric ID.
     * https://www.storyblok.com/docs/api/management/workflow-stage/update-a-workflow-stage
     */
    @Test
    fun `Update a Workflow Stage`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/space_id/workflow_stages/18") {
            setBody(buildJsonObject {
                putJsonObject("workflow_stage") {
                    put("after_publish_id", 561398)
                    put("allow_admin_change", true)
                    put("allow_admin_publish", false)
                    put("allow_all_stages", false)
                    put("allow_all_users", false)
                    put("allow_editor_change", true)
                    put("allow_publish", true)
                    put("color", "#fff")
                    put("is_default", true)
                    put("name", "an updated stage ")
                    put("position", 2)
                    putJsonArray("space_role_ids") {
                        add(232323)
                    }
                    putJsonArray("user_ids") {
                        add(343434)
                    }
                    putJsonArray("workflow_stage_ids") {
                        add(561398)
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Create a workflow stage change. It is important to pass a story ID along with the object.
     * https://www.storyblok.com/docs/api/management/workflow-stage-changes/create-a-workflow-stage-change
     */
    @Test
    fun `Create a Workflow Stage Change`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/space_id/workflow_stage_changes/") {
            setBody(buildJsonObject {
                putJsonObject("workflow_stage_change") {
                    put("story_id", 123)
                    put("workflow_stage_id", 123)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of workflow stage change objects.
     * https://www.storyblok.com/docs/api/management/workflow-stage-changes/retrieve-multiple-workflow-stage-changes
     */
    @Test
    fun `Retrieve Multiple Workflow Stage Changes`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/workflow_stage_changes") {
            url {
                parameters.append("with_story", "123")
            }
        }
        
        println(response.body<JsonElement>())
    }

}