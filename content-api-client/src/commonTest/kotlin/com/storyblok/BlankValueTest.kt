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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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

    private val home = "11111111-1111-1111-1111-111111111111"
    private val other = "22222222-2222-2222-2222-222222222222"

    private fun story(uuid: String, content: String) = """
        {"id": 1, "uuid": "$uuid", "name": "Home", "slug": "home", "full_slug": "home",
         "created_at": "2026-01-01T00:00:00.000Z", "published_at": null, "first_published_at": null,
         "updated_at": null, "sort_by_date": null, "position": 0, "tag_list": [], "is_startpage": false,
         "parent_id": null, "meta_data": null, "group_id": "941f4176-cbe4-4c15-9dd4-4384e136ac53",
         "release_id": null, "lang": "default", "path": null, "alternates": [], "default_full_slug": null,
         "translated_slugs": null, "content": $content}
    """

    private fun event(extra: String = "") = """
        {"component": "event", "_uid": "e", "title": "", "note": "", "startsAt": "", "day": "", "localStart": "",
         "seats": "", "price": "", "venue": "", "capacity": "" $extra}
    """

    private fun client(body: String) = StoryblokClientImpl(
        apiBuilder = {},
        serializersModuleBuilder = {
            polymorphic(Component::class, Event::class, Event.serializer())
            polymorphic(Component::class, Listing::class, Listing.serializer())
            polymorphic(Component::class, RequiredDate::class, RequiredDate.serializer())
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
        client("""{"story": ${story(home, event(extra))}}""").story<Event>("home").first().content

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
        val listing = story(home, """{"component": "listing", "_uid": "l", "event": "$other"}""")
        val related = story(other, event())
        val client = client("""{"story": $listing, "rels": [$related]}""")

        val event = assertNotNull(client.story<Listing>("home").first().content.event).content

        assertNull(event.startsAt)
        assertNull(event.seats)
    }

    @Test
    fun `a blank required value still fails as a deserialization error`() = runTest {
        val client = client("""{"story": ${story(home, """{"component": "required_date", "_uid": "r", "startsAt": ""}""")}}""")

        assertFailsWith<SerializationException> { client.story<RequiredDate>("home").first() }
    }

    @Test
    fun `a value that does not parse fails as a deserialization error`() = runTest {
        val client = client("""{"story": ${story(home, event(""", "venue": "not-a-uuid""""))}}""")

        val exception = assertFailsWith<SerializationException> { client.story<Event>("home").first() }

        assertContains(assertNotNull(exception.message), "not-a-uuid")
    }
}
