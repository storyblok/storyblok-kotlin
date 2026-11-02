package com.storyblok

import com.storyblok.cdn.decodeEdit
import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.storyblokJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Decoding the content of an edit the Visual Editor pushes, which a fetched story's content shares. */
class EditDecodingTest {

    @Serializable @SerialName("event")
    class Event(val startsAt: Instant?, val venue: Uuid?) : Component()

    private val json = storyblokJson({}, {})

    private fun content(startsAt: String, venue: String): JsonObject =
        Json.parseToJsonElement("""{"component": "event", "_uid": "e", "startsAt": "$startsAt", "venue": "$venue"}""").jsonObject

    @Test
    fun `an edit with blank values decodes them as null`() {
        val event = assertNotNull(json.decodeEdit(Event.serializer(), content(startsAt = "", venue = "")))

        assertNull(event.startsAt)
        assertNull(event.venue)
    }

    @Test
    fun `an edit with a value that does not parse is dropped`() {
        assertNull(json.decodeEdit(Event.serializer(), content(startsAt = "2026-01-01T00:00:00Z", venue = "not-a-uuid")))
    }
}
