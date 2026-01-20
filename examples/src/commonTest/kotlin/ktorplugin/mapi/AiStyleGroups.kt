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
class AiStyleGroups {

    /**
     * Creates a new AI style group for the organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/create-ai-style-group-organization
     */
    @Test
    fun `Create an AI Style Group in an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("orgs/me/ai_style_groups") {
            setBody(buildJsonObject {
                putJsonArray("ai_output_rule_ids") {
                    add(123456789012347)
                    add(123456789012348)
                }
                putJsonObject("ai_style_group") {
                    put("description", "Organization-level guidelines for all content")
                    put("name", "Company-wide Style Guide")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete an AI style group from the organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/delete-ai-style-group-organization
     */
    @Test
    fun `Delete an AI Style Group from an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("orgs/me/ai_style_groups/123456")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve AI style groups currently set as default for the organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/retrieve-default-ai-style-groups-organization
     */
    @Test
    fun `Retrieve Default AI Style Groups in an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me/default_ai_style_groups")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve all AI style groups available in the specified organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/retrieve-multiple-ai-style-groups-organization
     */
    @Test
    fun `Retrieve Multiple AI Style Groups in an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me/ai_style_groups")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieves a single AI style group available in the specified organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/retrieve-single-ai-style-group-organization
     */
    @Test
    fun `Retrieve a Single AI Style Group in an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("orgs/me/ai_style_groups/123456")
        
        println(response.body<JsonElement>())
    }

    /**
     * Set the default AI style groups for the specified organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/set-default-ai-style-groups-organization
     */
    @Test
    fun `Set Default AI Style Groups for an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("orgs/me/default_ai_style_groups") {
            setBody(buildJsonObject {
                putJsonArray("ai_style_group_ids") {
                    add(123456)
                    add(123457)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an existing AI style group in the specified organization
     * https://www.storyblok.com/docs/api/management/ai-style-groups/organizations/update-ai-style-group-organization
     */
    @Test
    fun `Update an AI Style Group in an Organization`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("orgs/me/ai_style_groups/123456") {
            setBody(buildJsonObject {
                putJsonArray("ai_output_rule_ids") {
                    add(123456789012349)
                    add(123456789012350)
                }
                putJsonObject("ai_style_group") {
                    put("description", "Updated organization-level guidelines")
                    put("name", "Updated Company Style Guide")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Create a new AI style group in the specified space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/create-ai-style-group-space
     */
    @Test
    fun `Create an AI Style Group in a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/ai_style_groups") {
            setBody(buildJsonObject {
                putJsonArray("ai_output_rule_ids") {
                    add(123456789012345)
                    add(123456789012346)
                }
                putJsonObject("ai_style_group") {
                    put("description", "Brand guidelines for marketing content creation")
                    put("name", "Marketing Style Guide")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete an AI style group from the specified space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/delete-ai-style-group-space
     */
    @Test
    fun `Delete an AI Style Group from a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/ai_style_groups/67499417567240")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve AI style groups currently set as default for the space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/retrieve-default-ai-style-groups-space
     */
    @Test
    fun `Retrieve Default AI Style Groups in a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/default_ai_style_groups")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve all AI style groups available in the specified space, including space-specific groups and inherited organization groups.
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/retrieve-multiple-ai-style-groups-space
     */
    @Test
    fun `Retrieve Multiple AI Style Groups in a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/ai_style_groups")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve a single AI style group available in the specified space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/retrieve-single-ai-style-group-space
     */
    @Test
    fun `Retrieve a Single AI Style Group in a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/ai_style_groups/67499417567240")
        
        println(response.body<JsonElement>())
    }

    /**
     * Set the default AI style groups for the specified space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/set-default-ai-style-groups-space
     */
    @Test
    fun `Set Default AI Style Groups for a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/default_ai_style_groups") {
            setBody(buildJsonObject {
                putJsonArray("ai_style_group_ids") {
                    add(68844418605065)
                    add(68844418605067)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an existing AI style group in the specified space
     * https://www.storyblok.com/docs/api/management/ai-style-groups/spaces/update-ai-style-group-space
     */
    @Test
    fun `Update an AI Style Group in a Space`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = OAuth("YOUR_OAUTH_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/ai_style_groups/67499417567240") {
            setBody(buildJsonObject {
                putJsonArray("ai_output_rule_ids") {
                    add(123456789012345)
                    add(123456789012348)
                }
                putJsonObject("ai_style_group") {
                    put("description", "Updated brand guidelines for marketing content creation")
                    put("name", "Updated Marketing Style Guide")
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}