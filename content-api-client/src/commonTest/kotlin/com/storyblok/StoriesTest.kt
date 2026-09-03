package com.storyblok

import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
import com.storyblok.cdn.query.FilterQueryBuilder
import com.storyblok.cdn.query.Is
import com.storyblok.cdn.query.SortAs
import com.storyblok.cdn.query.StoriesQuery
import kotlinx.serialization.json.JsonNames
import com.storyblok.cdn.NetworkRemoteMediator
import com.storyblok.cdn.PagedResponse
import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.query.Is.*
import com.storyblok.cdn.stories
import com.storyblok.cdn.story
import kotlinx.coroutines.flow.first
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.ktor.Api
import com.storyblok.ktor.Storyblok
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlin.uuid.Uuid

class StoriesTest {

    @Serializable @SerialName("article")
    class Article(val author: Story<Component>) : Component()

    @Serializable @SerialName("page")
    class Page(val title: String) : Component()

    @Serializable
    class Unnamed(val headline: String) : Component()

    @Serializable @SerialName("typed")
    class Typed(
        val count: Long,
        val ratio: Double,
        val label: String,
        val flag: Boolean,
        val at: Instant,
        val optional: String?,
        val tags: List<String>,
    ) : Component()

    @Serializable @SerialName("named_by_alias")
    class NamedByAlias(
        @JsonNames("release_date") val releaseDate: Instant,
        @JsonNames("hit_count") val hits: Long,
        @JsonNames("legacy_a", "legacy_b") val ambiguous: String,
        @JsonNames("plain") val other: String,
        val plain: String,
    ) : Component()

    @Serializable @SerialName("crossed")
    class Crossed(
        @SerialName("body") val richBody: String,
        @SerialName("body_html") val body: String,
    ) : Component()

    /** The component the DSL reference test queries; carries one field of every shape the operations accept. */
    @Serializable @SerialName("product")
    class Product(
        val headline: String,
        val categories: List<String>,
        /** A number field with no decimals. */
        val stock: Long,
        /** A number field with decimals. */
        val rating: Double,
        /** A number field modelled as text, which is how Storyblok serializes one. */
        val price: String,
        /** A field whose Storyblok name differs from the property, named with `@JsonNames`. */
        @JsonNames("release_date") val releaseDate: Instant,
    ) : Component()

    private inline fun <reified T : Component> query(
        noinline block: StoriesQuery<T>.() -> Unit,
    ): Map<String, String> = StoriesQuery<T>(typeInfo<Story<T>>()).apply(block).build()

    private fun productQuery(block: StoriesQuery<Product>.() -> Unit): Map<String, String> = query(block)

    /**
     * Exercises every member of the query DSL, so that the expected parameters below double as a reference for the
     * syntax and for what each one sends.
     */
    @Test
    fun `query DSL maps to the expected parameters`() {
        val params = productQuery {
            // Scalars.
            startsWith = "articles/"
            searchTerm = "space"
            isStartpage = false
            fromRelease = "12345"

            // Collections. `+=` works too, since these are read-only Lists behind a var.
            bySlugs = listOf("articles/*", "news/*")
            excludingSlugs = listOf("articles/draft-*")
            byUuids = listOf(Uuid.parse("11111111-1111-1111-1111-111111111111"))
            byUuidsOrdered = listOf(Uuid.parse("22222222-2222-2222-2222-222222222222"))
            excludingIds = listOf(1L, 2L)
            excludingFields = listOf("body", "sidebar")
            withTag = listOf("featured", "space")

            // Story date ranges.
            firstPublishedAtGreaterThan = Instant.parse("2022-01-01T00:00:00Z")
            firstPublishedAtLessThan = Instant.parse("2022-06-01T00:00:00Z")
            publishedAtGreaterThan = Instant.parse("2023-01-01T00:00:00Z")
            publishedAtLessThan = Instant.parse("2023-06-01T00:00:00Z")
            publishedAtGreaterThanOrEqual = Instant.parse("2023-02-01T00:00:00Z")
            publishedAtLessThanOrEqual = Instant.parse("2023-05-01T00:00:00Z")
            updatedAtGreaterThan = Instant.parse("2024-01-01T00:00:00Z")
            updatedAtLessThan = Instant.parse("2024-06-01T00:00:00Z")

            // Sorting: content fields by property, story attributes through Story<*>. The field's type picks the
            // ordering — whole numbers :int, decimals :float, everything else as text — unless a SortAs states it,
            // which is how a number Storyblok holds in a text field is ordered numerically.
            sortBy(Product::headline)
            sortByDescending(Product::rating)
            sortBy(Product::stock)
            sortByDescending(Product::price)
            sortBy(Product::price, SortAs.WholeNumber)
            sortByDescending(Product::price, SortAs.DecimalNumber)
            sortBy(Story<*>::createdAt)
            sortByDescending(Story<*>::publishedAt)

            // Every filter operation, in the order the API documents them.
            filter {
                Product::headline like "*space*"
                Product::headline notLike "*draft*"
                Product::categories `is` NotEmptyArray
                Product::headline isIn listOf("Spaceship", "Rocket")
                Product::headline notIn listOf("Paper")
                Product::categories allIn listOf("solar-system", "space-exploration")
                Product::categories anyIn listOf("solar-system", "mars")
                Product::stock greaterThan 10          // Long   -> gt_int
                Product::stock lessThan 500            // Long   -> lt_int
                Product::rating greaterThan 4.5        // Double -> gt_float
                Product::rating lessThan 5.0           // Double -> lt_float
                Product::releaseDate greaterThan Instant.parse("2020-01-01T00:00:00Z")
                Product::releaseDate lessThan Instant.parse("2030-01-01T00:00:00Z")
            }

            // Anything else, including overriding a parameter the SDK would send.
            parameter("some_future_param", "value")
        }

        assertEquals(
            mapOf(
                "starts_with" to "articles/",
                "search_term" to "space",
                "content_type" to "product",          // derived from Product, not set above
                "is_startpage" to "false",
                "by_slugs" to "articles/*,news/*",
                "excluding_slugs" to "articles/draft-*",
                "by_uuids" to "11111111-1111-1111-1111-111111111111",
                "by_uuids_ordered" to "22222222-2222-2222-2222-222222222222",
                "excluding_ids" to "1,2",
                "with_tag" to "featured,space",
                "excluding_fields" to "body,sidebar",
                "from_release" to "12345",
                "first_published_at_gt" to "2022-01-01T00:00:00Z",
                "first_published_at_lt" to "2022-06-01T00:00:00Z",
                "published_at_gt" to "2023-01-01T00:00:00Z",
                "published_at_lt" to "2023-06-01T00:00:00Z",
                "published_at_gte" to "2023-02-01T00:00:00Z",
                "published_at_lte" to "2023-05-01T00:00:00Z",
                "updated_at_gt" to "2024-01-01T00:00:00Z",
                "updated_at_lt" to "2024-06-01T00:00:00Z",
                "sort_by" to "content.headline:asc,content.rating:desc:float,content.stock:asc:int," +
                    "content.price:desc,content.price:asc:int,content.price:desc:float," +
                    "created_at:asc,published_at:desc",
                "filter_query[headline][like]" to "*space*",
                "filter_query[headline][not_like]" to "*draft*",
                "filter_query[categories][is]" to "not_empty_array",
                "filter_query[headline][in]" to "Spaceship,Rocket",
                "filter_query[headline][not_in]" to "Paper",
                "filter_query[categories][all_in_array]" to "solar-system,space-exploration",
                "filter_query[categories][any_in_array]" to "solar-system,mars",
                "filter_query[stock][gt_int]" to "10",
                "filter_query[stock][lt_int]" to "500",
                "filter_query[rating][gt_float]" to "4.5",
                "filter_query[rating][lt_float]" to "5.0",
                "filter_query[release_date][gt_date]" to "2020-01-01T00:00:00Z",
                "filter_query[release_date][lt_date]" to "2030-01-01T00:00:00Z",
                "some_future_param" to "value",
            ),
            params,
        )
    }

    @Test
    fun `a concrete component sets its own content type`() {
        assertEquals(mapOf("content_type" to "product"), productQuery { })
    }

    @Test
    fun `a parameter replaces the one it would otherwise generate`() {
        assertEquals(
            mapOf("starts_with" to "articles/", "content_type" to "page"),
            productQuery {
                startsWith = "articles/"
                parameter("content_type", "page")
            },
        )
    }

    @Test
    fun `an abstract component sets no content type and an empty query sends nothing`() {
        assertEquals(emptyMap(), query<Component> { })
    }

    @Test
    fun `a component without SerialName derives the name kotlinx generated`() {
        // Not second-guessed: the class is expected to carry @SerialName, and the API answers an unknown content
        // type with an empty result rather than an error.
        assertEquals(
            mapOf("content_type" to "com.storyblok.StoriesTest.Unnamed"),
            query<Unnamed> { },
        )
    }

    @Test
    fun `a sole JsonNames alias names the field without a SerialName`() {
        assertEquals(
            mapOf(
                "content_type" to "named_by_alias",
                "sort_by" to "content.hit_count:desc:int",
                "filter_query[release_date][gt_date]" to "2023-01-01T00:00:00Z",
                "filter_query[plain][like]" to "*x*",
            ),
            query<NamedByAlias> {
                sortByDescending(NamedByAlias::hits)
                filter {
                    NamedByAlias::releaseDate greaterThan Instant.parse("2023-01-01T00:00:00Z")
                    NamedByAlias::plain like "*x*"
                }
            },
        )
    }

    @Test
    fun `several JsonNames aliases name nothing so the serial name stands`() {
        assertEquals(
            mapOf("content_type" to "named_by_alias", "filter_query[ambiguous][like]" to "*x*"),
            query<NamedByAlias> {
                filter { NamedByAlias::ambiguous like "*x*" }
            },
        )
    }

    @Test
    fun `an alias declared by one field does not name another`() {
        // Aliases are looked up by the property that declares them: `other`'s alias does not redirect `plain`, which
        // still names itself. Asserted separately because both end up naming the same field, and a request carries
        // one value per name.
        assertEquals(
            mapOf("content_type" to "named_by_alias", "filter_query[plain][like]" to "*x*"),
            query<NamedByAlias> {
                filter { NamedByAlias::plain like "*x*" }
            },
        )
        assertEquals(
            mapOf("content_type" to "named_by_alias", "filter_query[plain][like]" to "*y*"),
            query<NamedByAlias> {
                filter { NamedByAlias::other like "*y*" }
            },
        )
    }

    @Test
    fun `property references renamed by SerialName are rejected`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            query<Crossed> { filter { Crossed::richBody like "*x*" } }
        }
        assertTrue("richBody" in failure.message.orEmpty(), failure.message.orEmpty())
        assertTrue("@JsonNames" in failure.message.orEmpty(), failure.message.orEmpty())
    }

    @Test
    fun `stories sort by their own attributes as well as by content fields`() {
        assertEquals(
            mapOf(
                "content_type" to "product",
                "sort_by" to "created_at:asc,published_at:desc,content.headline:asc",
            ),
            productQuery {
                sortBy(Story<*>::createdAt)
                sortByDescending(Story<*>::publishedAt)
                sortBy(Product::headline)
            },
        )
    }

    @Test
    fun `fields inherited from Component resolve too`() {
        assertEquals(
            mapOf("content_type" to "product", "sort_by" to "content._uid:asc,content.component:asc"),
            productQuery {
                sortBy(Component::uid)
                sortBy(Component::component)
            },
        )
    }

    @Test
    fun `an attribute the API cannot sort by is passed through for the API to reject`() {
        // Which attributes are sortable is not something the SDK tries to know: `slug` and `path` are, `full_slug`
        // and `lang` are not, and that list is the API's to change. An unsortable one is sent and answered with
        // `Not sortable by this column`.
        assertEquals(
            mapOf("content_type" to "product", "sort_by" to "full_slug:asc,uuid:asc"),
            productQuery {
                sortBy(Story<*>::fullSlug)
                sortBy(Story<*>::uuid)
            },
        )
    }

    @Test
    fun `the field type selects the sort type`() {
        assertEquals(
            mapOf(
                "content_type" to "typed",
                "sort_by" to "content.count:asc:int,content.ratio:desc:float,content.label:asc,content.at:desc",
            ),
            query<Typed> {
                sortBy(Typed::count)
                sortByDescending(Typed::ratio)
                sortBy(Typed::label)
                sortByDescending(Typed::at)
            },
        )
    }

    @Test
    fun `a stated sort type overrides the one the field type implies`() {
        assertEquals(
            mapOf(
                "content_type" to "product",
                // price is a number Storyblok holds in a text field, so its property type orders it as text.
                "sort_by" to "content.price:asc:int,content.price:desc:float,content.stock:asc,content.price:asc",
            ),
            productQuery {
                sortBy(Product::price, SortAs.WholeNumber)
                sortByDescending(Product::price, SortAs.DecimalNumber)
                sortBy(Product::stock, SortAs.Text)      // a number field forced back to text ordering
                sortBy(Product::price)                   // unstated, so text ordering as before
            },
        )
    }

    @Test
    fun `an array value type the field cannot hold is rejected`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            productQuery { filter { Product::headline `is` NotEmptyArray } }
        }
        assertContains(failure.message.orEmpty(), "headline")
    }

    @Test
    fun `a stated sort type the field cannot hold is rejected`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            productQuery { sortBy(Product::categories, SortAs.WholeNumber) }
        }
        assertContains(failure.message.orEmpty(), "categories")
    }

    @Test
    fun `the operand type selects the wire operation`() {
        val at = Instant.parse("2023-01-01T00:00:00Z")
        assertEquals(
            mapOf(
                "content_type" to "typed",
                "filter_query[count][gt_int]" to "3",
                "filter_query[ratio][lt_float]" to "1.5",
                "filter_query[at][gt_date]" to "2023-01-01T00:00:00Z",
            ),
            query<Typed> {
                filter {
                    Typed::count greaterThan 3
                    Typed::ratio lessThan 1.5
                    Typed::at greaterThan at
                }
            },
        )
    }

    @Test
    fun `the multi-value operations accept a list field`() {
        assertEquals(
            mapOf(
                "content_type" to "typed",
                "filter_query[tags][all_in_array]" to "a,b",
                "filter_query[tags][any_in_array]" to "a,b",
                // `in` applies to both shapes, so it is not restricted.
                "filter_query[tags][in]" to "a",
                "filter_query[label][in]" to "a",
            ),
            query<Typed> {
                filter {
                    Typed::tags allIn listOf("a", "b")
                    Typed::tags anyIn listOf("a", "b")
                    Typed::tags isIn listOf("a")
                    Typed::label isIn listOf("a")
                }
            },
        )
    }

    @Test
    fun `a text field accepts every operand`() {
        assertEquals(
            mapOf(
                "content_type" to "typed",
                "filter_query[label][gt_int]" to "3",
                "filter_query[label][lt_float]" to "1.5",
                "filter_query[label][gt_date]" to "2023-01-01T00:00:00Z",
                "filter_query[optional][gt_int]" to "7",
            ),
            query<Typed> {
                filter {
                    Typed::label greaterThan 3
                    Typed::label lessThan 1.5
                    Typed::label greaterThan Instant.parse("2023-01-01T00:00:00Z")
                    Typed::optional greaterThan 7L
                }
            },
        )
    }

    @Test
    fun `an operand that the field type cannot hold is rejected`() {
        val mismatches: List<Pair<String, FilterQueryBuilder<Typed>.() -> Unit>> = listOf(
            "count" to { Typed::count greaterThan 1.5 },
            "ratio" to { Typed::ratio lessThan 1L },
            "flag" to { Typed::flag greaterThan 1L },
            "at" to { Typed::at greaterThan 1L },
            // all_in_array / any_in_array match nothing at all on a single-value field.
            "label" to { Typed::label allIn listOf("a") },
            "count" to { Typed::count anyIn listOf("a") },
        )
        for ((field, block) in mismatches) {
            val failure = assertFailsWith<IllegalArgumentException> {
                query<Typed> { filter(block) }
            }
            assertTrue(field in failure.message.orEmpty(), failure.message.orEmpty())
        }
    }

    @Test
    fun `a name moved onto another property by SerialName is not detected`() {
        // Documented limitation: Crossed::body serializes as body_html, but `body` is a name the component does have
        // (it belongs to richBody), so the reference passes the check and addresses the wrong field. Resolving this
        // needs the @SerialName values, which are absent from the descriptor at runtime.
        assertEquals(
            mapOf("content_type" to "crossed", "filter_query[body][like]" to "*x*"),
            query<Crossed> { filter { Crossed::body like "*x*" } },
        )
    }

    @Test
    fun `whole float operands are formatted the same on every platform`() {
        assertEquals(
            mapOf(
                "content_type" to "product",
                "filter_query[price][gt_float]" to "100.0",
                "filter_query[price][lt_float]" to "0.5",
            ),
            productQuery { filter { Product::price greaterThan 100.0; Product::price lessThan 0.5 } },
        )
    }

    @Test
    fun `an untyped query can still filter`() {
        // stories<Component> carries a polymorphic content descriptor, which names no fields, so a reference cannot
        // be checked against one and is taken as given rather than rejected.
        assertEquals(
            mapOf("filter_query[component][like]" to "*x*"),
            query<Component> { filter { Component::component like "*x*" } },
        )
    }

    @Test
    fun `parameters owned by the SDK can be overridden`() {
        assertEquals(
            mapOf("content_type" to "product", "per_page" to "5"),
            productQuery { parameter("per_page", 5) },
        )
    }

    @Test
    fun `stories deserializes a page and resolves relations`() = runTest {
        val authorUuid = "11111111-1111-1111-1111-111111111111"
        val article = storyJson(
            id = 1,
            uuid = "22222222-2222-2222-2222-222222222222",
            content = """{"_uid":"c1","component":"article","author":"$authorUuid"}""",
        )
        val author = storyJson(
            id = 99,
            uuid = authorUuid,
            content = """{"_uid":"c2","component":"page","title":"Jane"}""",
        )
        val payload = """{"stories":[$article],"rels":[$author],"cv":123}"""

        var lastUrl: Url? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = {
                polymorphic(Component::class, Article::class, Article.serializer())
                polymorphic(Component::class, Page::class, Page.serializer())
            },
            jsonBuilder = {
                ignoreUnknownKeys = true
                explicitNulls = false
                coerceInputValues = true
            },
            http = HttpClient(MockEngine { request ->
                lastUrl = request.url
                respond(
                    content = payload,
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "Total" to listOf("1"),
                        "Per-Page" to listOf("25"),
                    ),
                )
            }),
        )

        val snapshot = client.stories<Article>(PagingConfig(pageSize = 25)) {
            startsWith = "articles/"
            sortByDescending(Article::author)
            filter { Article::author `is` Is.NotEmpty }
        }.asSnapshot()

        assertEquals(1, snapshot.size)
        assertEquals("Jane", (snapshot[0].content.author.content as Page).title)

        val params = lastUrl!!.parameters
        assertEquals("articles/", params["starts_with"])
        assertEquals("1", params["page"])
        assertEquals("25", params["per_page"])
        assertEquals("article.author", params["resolve_relations"])
        assertEquals("article", params["content_type"])
        assertEquals("content.author:desc", params["sort_by"])
        assertEquals("not_empty", params["filter_query[author][is]"])
    }

    @Test
    fun `an abstract query names an inherited field by its alias`() {
        // The content descriptor is polymorphic, so there are no fields to check the reference against — but the
        // fields every component inherits are still addressable, and Component::uid is `_uid` on the wire.
        assertEquals(
            mapOf("sort_by" to "content._uid:asc"),
            query<Component> { sortBy(Component::uid) },
        )
    }

    @Test
    fun `the mediator appends after the last loaded page rather than its own last fetch`() = runTest {
        val requested = mutableListOf<Int>()
        val mediator = NetworkRemoteMediator<String>(
            fetch = { page ->
                requested += page
                PagedResponse(listOf("a", "b"), total = 10, perPage = 2, page = page)
            },
            invalidate = {},
        )

        // Three pages are loaded and on screen; a loaded page carries its predecessor as prevKey.
        val loaded = (1..3).map { page ->
            PagingSource.LoadResult.Page(
                data = listOf("x", "y"),
                prevKey = if (page <= 1) null else page - 1,
                nextKey = page + 1,
            )
        }
        val state = PagingState(loaded, anchorPosition = 5, config = PagingConfig(pageSize = 2), leadingPlaceholderCount = 0)

        // The refresh is what used to desync the two: it sent the mediator back to page 1 while the list stayed
        // where it was, so the append that followed re-fetched page 2 instead of carrying on at page 4.
        mediator.load(LoadType.REFRESH, state)
        mediator.load(LoadType.APPEND, state)

        assertEquals(listOf(1, 4), requested)
    }

    @Test
    fun `paging continues when the response carries no Total header`() = runTest {
        val requested = mutableListOf<String>()
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true },
            http = HttpClient(MockEngine { request ->
                val page = request.url.parameters["page"]!!.toInt()
                requested += page.toString()
                // Three full pages of two, then nothing — and no Total header to say so in advance.
                val ids = if (page <= 3) listOf((page - 1) * 2 + 1, (page - 1) * 2 + 2) else emptyList()
                val stories = ids.joinToString(",") {
                    storyJson(it.toLong(), "00000000-0000-0000-0000-00000000000$it",
                        """{"_uid":"c$it","component":"page","title":"t$it"}""")
                }
                respond(
                    content = """{"stories":[$stories],"rels":[],"cv":1}""",
                    headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
                )
            }),
        )

        val snapshot = client.stories<Page>(PagingConfig(pageSize = 2)).asSnapshot { scrollTo(3) }

        // A full page with no Total header must not be read as the only page: falling back to the item count would
        // make it one page long and stop after the first.
        assertEquals(6, snapshot.size, "requested pages: $requested")
    }

    @Test
    fun `per_page is the configured page size`() = runTest {
        var url: Url? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true },
            http = HttpClient(MockEngine {
                url = it.url
                respond(
                    content = """{"stories":[],"rels":[],"cv":1}""",
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "Total" to listOf("0"),
                        "Per-Page" to listOf("40"),
                    ),
                )
            }),
        )

        client.stories<Page>(PagingConfig(pageSize = 40)).asSnapshot()

        // Not silently reduced: a size the endpoint rejects is the API's to refuse, and Paging's prefetchDistance
        // and initialLoadSize are derived from this same number.
        assertEquals("40", url!!.parameters["per_page"])
    }

    @Test
    fun `an overridden parameter replaces the client's own on the wire`() = runTest {
        var lastUrl: Url? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true },
            http = HttpClient(MockEngine { request ->
                lastUrl = request.url
                respond(
                    content = """{"stories":[],"rels":[],"cv":123}""",
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "Total" to listOf("0"),
                        "Per-Page" to listOf("25"),
                    ),
                )
            }),
        )

        client.stories<Page>(PagingConfig(pageSize = 25)) { parameter("per_page", 5) }.asSnapshot()

        val params = lastUrl!!.parameters
        assertEquals(listOf("5"), params.getAll("per_page"), "the override must replace, not join")
        assertEquals("page", params["content_type"])
    }

    @Test
    fun `disabling alternative names cannot break the schema's own field names`() = runTest {
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            // The schema names its fields with @JsonNames, which this setting would otherwise switch off.
            jsonBuilder = {
                ignoreUnknownKeys = true
                explicitNulls = false
                coerceInputValues = true
                useAlternativeNames = false
            },
            http = HttpClient(MockEngine {
                respond(
                    content = """{"stories":[${storyJson(1, "22222222-2222-2222-2222-222222222222",
                        """{"_uid":"c1","component":"page","title":"Jane"}""")}],"rels":[],"cv":1}""",
                    headers = headersOf(
                        HttpHeaders.ContentType to listOf("application/json"),
                        "Total" to listOf("1"),
                        "Per-Page" to listOf("25"),
                    ),
                )
            }),
        )

        val snapshot = client.stories<Page>(PagingConfig(pageSize = 25)).asSnapshot()

        // full_slug and created_at are only reachable through @JsonNames.
        assertEquals("articles/story-1", snapshot[0].fullSlug)
        assertEquals(Instant.parse("2025-07-09T14:35:26.851Z"), snapshot[0].createdAt)
    }

    @Test
    fun `the plugin's language applies to every request and parameter overrides it`() = runTest {
        var url: Url? = null
        val engine = MockEngine {
            url = it.url
            respond(
                content = """{"stories":[],"rels":[],"cv":1}""",
                headers = headersOf(
                    HttpHeaders.ContentType to listOf("application/json"),
                    "Total" to listOf("0"),
                    "Per-Page" to listOf("25"),
                ),
            )
        }
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true },
            // The plugin has to be installed for its configuration to reach the request at all.
            http = HttpClient(engine) {
                install(Storyblok(Api.CDN)) {
                    accessToken = "t"
                    language = "de"
                    fallbackLanguage = "en"
                }
            },
        )

        client.stories<Page>(PagingConfig(pageSize = 25)).asSnapshot()
        assertEquals(listOf("de"), url!!.parameters.getAll("language"))
        assertEquals(listOf("en"), url!!.parameters.getAll("fallback_lang"))

        client.stories<Page>(PagingConfig(pageSize = 25)) { parameter("language", "fr") }.asSnapshot()
        assertEquals(listOf("fr"), url!!.parameters.getAll("language"), "a query parameter must replace the default")
        assertEquals(listOf("en"), url!!.parameters.getAll("fallback_lang"))
    }

    @Test
    fun `story takes the parameters the single-story endpoint accepts`() = runTest {
        var url: Url? = null
        val client = StoryblokClientImpl(
            apiBuilder = {},
            serializersModuleBuilder = { polymorphic(Component::class, Page::class, Page.serializer()) },
            jsonBuilder = { ignoreUnknownKeys = true; explicitNulls = false; coerceInputValues = true },
            http = HttpClient(MockEngine {
                url = it.url
                respond(
                    content = """{"story":${storyJson(1, "22222222-2222-2222-2222-222222222222",
                        """{"_uid":"c1","component":"page","title":"Jane"}""")},"rels":[],"cv":1}""",
                    headers = headersOf(HttpHeaders.ContentType to listOf("application/json")),
                )
            }),
        )

        client.story<Page>("home") {
            excludingFields = listOf("body", "sidebar")
            fromRelease = "12345"
            // Response-shaping parameters have no entry point yet; the escape hatch still reaches them.
            parameter("resolve_links", "url")
            parameter("resolve_assets", 1)
        }.first()

        val params = url!!.parameters
        assertEquals("body,sidebar", params["excluding_fields"])
        assertEquals("12345", params["from_release"])
        assertEquals("url", params["resolve_links"])
        assertEquals("1", params["resolve_assets"])
        // Set-selecting parameters are not on StoryQuery at all, so none can leak in.
        assertEquals(null, params["sort_by"])
        assertEquals(null, params["starts_with"])
    }

    private fun storyJson(id: Long, uuid: String, content: String): String = """
        {
          "id": $id,
          "uuid": "$uuid",
          "name": "Story $id",
          "content": $content,
          "slug": "story-$id",
          "full_slug": "articles/story-$id",
          "created_at": "2025-07-09T14:35:26.851Z",
          "position": 0,
          "tag_list": [],
          "is_startpage": false,
          "group_id": "57350688-5a28-49d1-b5a9-086ae0d4c0d2",
          "lang": "default",
          "alternates": []
        }
    """.trimIndent()
}
