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

class Experiments {

    /**
     * Transition an experiment from draft or paused to running.
     * https://www.storyblok.com/docs/api/management/experiments/activate-an-experiment
     */
    @Test
    fun `Activate an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/experiments/176070002766742/activate") {
            url {
                parameters.append("end_date", "2026-08-20")
            }
            setBody(buildJsonObject { })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Complete an experiment and select a winning variant.
     * https://www.storyblok.com/docs/api/management/experiments/complete-an-experiment-with-a-winning-variant
     */
    @Test
    fun `Complete an Experiment with a Winning Variant`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.patch("spaces/288868932106293/experiments/176070002766742/complete_with_winner") {
            url {
                parameters.append("variant_id", "176070002762432")
            }
            setBody(buildJsonObject { })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Complete a running experiment without selecting a winning variant.
     * https://www.storyblok.com/docs/api/management/experiments/complete-an-experiment
     */
    @Test
    fun `Complete an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/experiments/176070002766742/complete") {
            setBody(buildJsonObject { })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Map a story to an experiment variant.
     * https://www.storyblok.com/docs/api/management/experiments/create-a-story-mapping
     */
    @Test
    fun `Create a Story Mapping`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/experiments/176070002766742/variants/1002/story_mappings") {
            setBody(buildJsonObject {
                putJsonObject("story_mapping") {
                    put("original_story_id", 176024833123843)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Create a new experiment in a space.
     * https://www.storyblok.com/docs/api/management/experiments/create-an-experiment
     */
    @Test
    fun `Create an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/experiments/") {
            setBody(buildJsonObject {
                putJsonObject("experiment") {
                    put("description", "A short description for a simple test")
                    put("display_name", "A simple test")
                    putJsonArray("experiment_variants_attributes") {
                        addJsonObject {
                            put("display_name", "Control")
                            put("is_control", true)
                            put("name", "control")
                            put("weight", 60)
                        }
                        addJsonObject {
                            put("display_name", "Test")
                            put("is_control", false)
                            put("name", "test")
                            put("weight", 40)
                        }
                    }
                    put("name", "a_simple_test")
                    putJsonArray("story_ids") {
                        add(176024833123843)
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Remove a story-to-variant mapping from an experiment variant.
     * https://www.storyblok.com/docs/api/management/experiments/delete-a-story-mapping
     */
    @Test
    fun `Delete a Story Mapping`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/experiments/176070002766742/variants/1002/story_mappings/176024833123843")
        
        println(response.body<JsonElement>())
    }

    /**
     * Delete an experiment.
     * https://www.storyblok.com/docs/api/management/experiments/delete-an-experiment
     */
    @Test
    fun `Delete an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/experiments/176070002766742")
        
        println(response.body<JsonElement>())
    }

    /**
     * Link a story to an experiment.
     * https://www.storyblok.com/docs/api/management/experiments/link-a-story
     */
    @Test
    fun `Link a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/experiments/176070002766742/stories") {
            setBody(buildJsonObject {
                putJsonObject("experiment_story") {
                    put("story_id", 176024833123843)
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Pause a running experiment.
     * https://www.storyblok.com/docs/api/management/experiments/pause-an-experiment
     */
    @Test
    fun `Pause an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/experiments/176070002766742/pause") {
            setBody(buildJsonObject { })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Push results to a single experiment.
     * https://www.storyblok.com/docs/api/management/experiments/push-experiment-results
     */
    @Test
    fun `Push Experiment Results`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.post("spaces/288868932106293/experiments/176070002766742/results") {
            setBody(buildJsonObject {
                putJsonArray("charts") {
                    addJsonObject {
                        put("body", "Variant A shows 95% confidence of improvement.")
                        put("kind", "bar")
                        putJsonArray("labels") {
                            add("Control")
                            add("Variant A")
                        }
                        putJsonArray("series") {
                            addJsonObject {
                                putJsonArray("data") {
                                    add(0.12)
                                    add(0.15)
                                }
                                put("label", "Conversion rate")
                            }
                        }
                        put("title", "Conversion Rate")
                        put("xLabel", "Variant")
                        put("yLabel", "Rate")
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve a single experiment by its numeric ID.
     * https://www.storyblok.com/docs/api/management/experiments/retrieve-a-single-experiment
     */
    @Test
    fun `Retrieve a Single Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/experiments/176070002766742")
        
        println(response.body<JsonElement>())
    }

    /**
     * Retrieve results for a single experiment.
     * https://www.storyblok.com/docs/api/management/experiments/retrieve-experiment-results
     */
    @Test
    fun `Retrieve Experiment Results`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/experiments/176070002766742/results")
        
        println(response.body<JsonElement>())
    }

    /**
     * Returns an array of experiments.
     * https://www.storyblok.com/docs/api/management/experiments/retrieve-multiple-experiments
     */
    @Test
    fun `Retrieve Multiple Experiments`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.get("spaces/288868932106293/experiments/")
        
        println(response.body<JsonElement>())
    }

    /**
     * Select a winning variant for a completed experiment.
     * https://www.storyblok.com/docs/api/management/experiments/select-a-winning-variant
     */
    @Test
    fun `Select a Winning Variant`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.patch("spaces/288868932106293/experiments/176070002766742/select_winner") {
            url {
                parameters.append("variant_id", "176070002791237")
            }
            setBody(buildJsonObject { })
        }
        
        println(response.body<JsonElement>())
    }

    /**
     * Unlink a story from an experiment.
     * https://www.storyblok.com/docs/api/management/experiments/unlink-a-story
     */
    @Test
    fun `Unlink a Story`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.delete("spaces/288868932106293/experiments/176070002766742/stories/176024833123843")
        
        println(response.body<JsonElement>())
    }

    /**
     * Update an existing experiment.
     * https://www.storyblok.com/docs/api/management/experiments/update-an-experiment
     */
    @Test
    fun `Update an Experiment`() = runTest {

        val client = HttpClient {
            install(Storyblok(MAPI)) {
                accessToken = Personal("YOUR_PERSONAL_ACCESS_TOKEN")
            }
            expectSuccess = false
        }
        
        val response = client.put("spaces/288868932106293/experiments/176070002766742") {
            setBody(buildJsonObject {
                putJsonObject("experiment") {
                    put("description", "Updated description")
                    put("display_name", "A simple test (updated)")
                    putJsonArray("experiment_variants_attributes") {
                        addJsonObject {
                            put("id", 1001)
                            put("weight", 70)
                        }
                        addJsonObject {
                            put("id", 1002)
                            put("weight", 40)
                        }
                    }
                }
            })
        }
        
        println(response.body<JsonElement>())
    }

}