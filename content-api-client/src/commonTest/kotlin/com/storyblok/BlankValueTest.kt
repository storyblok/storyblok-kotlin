package com.storyblok

import com.storyblok.cdn.StoryblokClientImpl
import com.storyblok.cdn.fileCacheStorage
import com.storyblok.cdn.httpCacheStorage
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Story
import com.storyblok.cdn.story
import com.storyblok.serializers.StoryblokDateTimeSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.modules.polymorphic
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Storyblok sends a date, number or story option left blank as `""`. Decoded with the client's default settings, which
 * require every property without a default to be present.
 */
class BlankValueTest {

    @BeforeTest
    fun disableFileCache() { httpCacheStorage = { _ -> null } }

    @AfterTest
    fun restoreFileCache() { httpCacheStorage = ::fileCacheStorage }

    @Serializable @SerialName("event")
    class Event(
        val title: String,
        val note: String?,
        val startsAt: Instant?,
        val day: LocalDate?,
        @Serializable(with = StoryblokDateTimeSerializer::class) val localStart: LocalDateTime?,
        val seats: Int?,
        val price: Double?,
        val venue: Uuid?,
        val capacity: Int = 10,
        @JsonNames("doors_open") val doorsOpen: Instant? = null,
        val sessions: List<Component> = emptyList(),
    ) : Component()

    @Serializable @SerialName("listing")
    class Listing(val event: Story<Event>?) : Component()

    @Serializable @SerialName("required_date")
    class RequiredDate(val startsAt: Instant) : Component()

    @Serializable @SerialName("venues")
    class Venues(val venues: List<@Contextual Uuid?>) : Component()

    @Serializable
    class Range(val from: Instant?, val to: Instant?)

    @Serializable @JvmInline
    value class Window(val range: Range)

    @Serializable @SerialName("booking")
    class Booking(val window: Window) : Component()

    @Serializable @SerialName("featured")
    class Featured(val event: Story<Event> = FALLBACK) : Component()

    @Serializable @SerialName("alias_after")
    class AliasAfter(val title: String, @JsonNames("title") val seats: Int? = null) : Component()

    @Serializable @SerialName("alias_before")
    class AliasBefore(@JsonNames("title") val seats: Int? = null, val title: String) : Component()

    private companion object {
        const val HOME = "11111111-1111-1111-1111-111111111111"
        const val OTHER = "22222222-2222-2222-2222-222222222222"

        fun story(uuid: String, content: String) = """
            {"id": 1, "uuid": "$uuid", "name": "Home", "slug": "home", "full_slug": "home",
             "created_at": "2026-01-01T00:00:00.000Z", "published_at": null, "first_published_at": null,
             "updated_at": null, "sort_by_date": null, "position": 0, "tag_list": [], "is_startpage": false,
             "parent_id": null, "meta_data": null, "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
             "release_id": null, "lang": "default", "path": null, "alternates": [], "default_full_slug": null,
             "translated_slugs": null, "content": $content}
        """

        fun event(extra: String = "") = """
            {"component": "event", "_uid": "e", "title": "", "note": "", "startsAt": "", "day": "", "localStart": "",
             "seats": "", "price": "", "venue": "", "capacity": "" $extra}
        """

        /** The story a [Featured] falls back on: an event with nothing blank. */
        val FALLBACK: Story<Event> = Json { ignoreUnknownKeys = true }.decodeFromString(
            Story.serializer(Event.serializer()),
            story(OTHER, """
                {"component": "event", "_uid": "f", "title": "Fallback", "note": null, "startsAt": null, "day": null,
                 "localStart": null, "seats": null, "price": null, "venue": null}
            """),
        )
    }

    private fun client(body: String) = StoryblokClientImpl(
        apiBuilder = {},
        serializersModuleBuilder = {
            polymorphic(Component::class, Event::class, Event.serializer())
            polymorphic(Component::class, Listing::class, Listing.serializer())
            polymorphic(Component::class, RequiredDate::class, RequiredDate.serializer())
            polymorphic(Component::class, Venues::class, Venues.serializer())
            polymorphic(Component::class, Booking::class, Booking.serializer())
            polymorphic(Component::class, Featured::class, Featured.serializer())
            polymorphic(Component::class, AliasAfter::class, AliasAfter.serializer())
            polymorphic(Component::class, AliasBefore::class, AliasBefore.serializer())
            contextual(Uuid::class, Uuid.serializer())
        },
        // As StoryblokClient(lenientJsonParsing = false) configures it.
        jsonBuilder = {
            explicitNulls = true
            coerceInputValues = false
            ignoreUnknownKeys = false
        },
        http = HttpClient(MockEngine { respond(body, headers = headersOf(HttpHeaders.ContentType, "application/json")) }),
    )

    private suspend fun blankEvent(extra: String = ""): Event =
        client("""{"story": ${story(HOME, event(extra))}}""").story<Event>("home").first().content

    @Test
    fun `blank dates decode as null`() = runTest {
        val event = blankEvent()

        assertNull(event.startsAt)
        assertNull(event.day)
        assertNull(event.localStart)
    }

    @Test
    fun `blank numbers decode as null`() = runTest {
        val event = blankEvent()

        assertNull(event.seats)
        assertNull(event.price)
    }

    @Test
    fun `a blank story option decodes as a null uuid`() = runTest {
        assertNull(blankEvent().venue)
    }

    @Test
    fun `a blank value takes the property's default`() = runTest {
        assertEquals(10, blankEvent().capacity)
    }

    @Test
    fun `a blank text field keeps its blank value`() = runTest {
        val event = blankEvent()

        assertEquals("", event.title)
        assertEquals("", event.note)
    }

    @Test
    fun `blank values are read in nested components and by alternative names`() = runTest {
        val event = blankEvent(""", "doors_open": "", "sessions": [${event()}]""")

        assertNull(event.doorsOpen)
        val session = event.sessions.single() as Event
        assertNull(session.startsAt)
        assertNull(session.venue)
    }

    @Test
    fun `blank values in a resolved relation decode as null`() = runTest {
        val listing = story(HOME, """{"component": "listing", "_uid": "l", "event": "$OTHER"}""")
        val related = story(OTHER, event())
        val client = client("""{"story": $listing, "rels": [$related]}""")

        val event = assertNotNull(client.story<Listing>("home").first().content.event).content

        assertNull(event.startsAt)
        assertNull(event.seats)
    }

    @Test
    fun `a blank required value still fails as a deserialization error`() = runTest {
        val client = client("""{"story": ${story(HOME, """{"component": "required_date", "_uid": "r", "startsAt": ""}""")}}""")

        assertFailsWith<SerializationException> { client.story<RequiredDate>("home").first() }
    }

    @Test
    fun `a value that does not parse fails as a deserialization error`() = runTest {
        val client = client("""{"story": ${story(HOME, event(""", "venue": "not-a-uuid""""))}}""")

        val exception = assertFailsWith<SerializationException> { client.story<Event>("home").first() }

        assertContains(assertNotNull(exception.message), "not-a-uuid")
    }

    @Test
    fun `a blank entry in a list of nullable contextual values decodes as null`() = runTest {
        val content = """{"component": "venues", "_uid": "v", "venues": ["", "$OTHER"]}"""
        val venues = client("""{"story": ${story(HOME, content)}}""").story<Venues>("home").first().content.venues

        assertEquals(listOf(null, Uuid.parse(OTHER)), venues)
    }

    @Test
    fun `blank values in an inline class are read by the value it wraps`() = runTest {
        val content = """{"component": "booking", "_uid": "b", "window": {"from": "", "to": "2026-01-01T00:00:00Z"}}"""
        val window = client("""{"story": ${story(HOME, content)}}""").story<Booking>("home").first().content.window

        assertNull(window.range.from)
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), window.range.to)
    }

    @Test
    fun `a blank relation takes the property's default`() = runTest {
        val content = """{"component": "featured", "_uid": "f", "event": ""}"""
        val featured = client("""{"story": ${story(HOME, content)}, "rels": []}""").story<Featured>("home").first().content

        assertEquals(FALLBACK.uuid, featured.event.uuid)
    }

    @Test
    fun `a property's own name takes precedence over another's alternative name`() = runTest {
        for (component in listOf("alias_after", "alias_before")) {
            val content = """{"component": "$component", "_uid": "a", "title": "", "seats": 5}"""
            val story = client("""{"story": ${story(HOME, content)}}""").story<Component>("home").first().content

            when (story) {
                is AliasAfter -> assertEquals("" to 5, story.title to story.seats)
                is AliasBefore -> assertEquals("" to 5, story.title to story.seats)
                else -> error("unexpected $story")
            }
        }
    }
}
