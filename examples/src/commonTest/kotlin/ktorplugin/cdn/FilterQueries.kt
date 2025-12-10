package ktorplugin.cdn

import com.storyblok.ktor.Api.*
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test

@OptIn(ExperimentalSerializationApi::class)
class FilterQueries {

	/**
     * Learn how to use filter queries with field-level translation in Storyblok by extending field keys with i18n and language codes for multilingual content filtering.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/field-level-translation
     */
    @Test
    fun `Filter Queries with Field-level Translation`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories") {
		    url {
		        parameters.append("filter_query[headline__i18n__es_co][in]", "Sinfonía de la Tierra: Navegar por las maravillas y los desafíos de nuestro oasis azul")
		        parameters.append("version", "published")
		        parameters.append("language", "es-co")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Use filter queries to target nestable bloks and fields.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/nested-blocks-and-fields
     */
    @Test
    fun `Filter Queries with Nestable Blocks and Fields`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories") {
		    url {
		        parameters.append("filter_query[body.0.name][in]", "This is a nested blok")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter stories by checking if a field contains all of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-all-in-array
     */
    @Test
    fun all_in_array() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[topics][all_in_array]", "solar-system,space-exploration")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter stories by checking if a field contains any of the values provided in the query.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-any-in-array
     */
    @Test
    fun any_in_array() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[topics][any_in_array]", "solar-system,space-exploration")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a date field value greater than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-date
     */
    @Test
    fun gt_date() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[scheduled][gt_date]", "2023-12-31 09:00")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a float field value greater than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-float
     */
    @Test
    fun gt_float() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[price][gt_float]", "1199.99")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with an integer field value greater than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-gt-int
     */
    @Test
    fun gt_int() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[price][gt_int]", "1200")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a field value matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-in
     */
    @Test
    fun `in`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[categories][in]", "space-exploration,solar-system")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a field value of a specific type.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-is
     */
    @Test
    fun `is`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[author][is]", "not_empty_array")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a field value matching a specific pattern.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-like
     */
    @Test
    fun like() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[headline][like]", "*space*")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a date field value less than the provided date.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-date
     */
    @Test
    fun lt_date() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[scheduled][lt_date]", "2023-12-31 09:00")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a float field value less than the provided float.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-float
     */
    @Test
    fun lt_float() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[price][lt_float]", "1199.99")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with an integer field value less than the provided integer.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-lt-int
     */
    @Test
    fun lt_int() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[price][lt_int]", "1200")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a field value not matching any of the provided values.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-in
     */
    @Test
    fun not_in() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[categories][not_in]", "space-exploration,culture")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Filter to return stories with a field value not matching any of the provided patterns.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/operation-not-like
     */
    @Test
    fun not_like() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("filter_query[headline][not_like]", "*Mysteries*")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Example showing how to filter stories by boolean field values using the 'in' operation in Storyblok's filter queries.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-a-boolean-value
     */
    @Test
    fun `Filtering Stories by a Boolean Value`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("starts_with", "articles/")
		        parameters.append("filter_query[highlighted][in]", "true")
		    }
		}
		
		println(response.body<JsonElement>())
    }

	/**
     * Learn how to filter stories within a specific value range using gt_float and lt_float for price filtering and similar use cases.
     * https://www.storyblok.com/docs/api/content-delivery/v2/filter-queries/examples/filtering-stories-by-defining-a-value-range
     */
    @Test
    fun `Filtering Stories by Defining a Value Range`() = runTest {

        val client = HttpClient {
		    install(Storyblok(CDN)) {
		        accessToken = "ask9soUkv02QqbZgmZdeDAtt"
		    }
		}
		
		val response = client.get("stories/") {
		    url {
		        parameters.append("starts_with", "products/")
		        parameters.append("filter_query[price][lt_float]", "300")
		        parameters.append("filter_query[price][gt_float]", "100")
		    }
		}
		
		println(response.body<JsonElement>())
    }

}