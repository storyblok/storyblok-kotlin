@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok

import com.storyblok.cdn.toJsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals

/** Reading a JavaScript value into a [kotlinx.serialization.json.JsonElement] tree. */
class JsonElementsTest {

    @Test
    fun `a story reads into a JsonObject`() {
        val json = storyValue().toJsonElement(resolveLevel = 1).jsonObject

        assertEquals("post1", json["name"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, json["published_at"])
        assertEquals(false, json["is_startpage"]!!.jsonPrimitive.content.toBoolean())
        assertEquals(listOf("a", "b"), json["tag_list"]!!.jsonArray.map { it.jsonPrimitive.content })
        assertEquals("x", json["content"]!!.jsonObject["nested"]!!.jsonObject["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun `a whole number keeps its integer form`() {
        val json = storyValue().toJsonElement(resolveLevel = 1).jsonObject

        // JavaScript has one number type; an id written as 1.51935015482904E14 would not decode as a Long.
        assertEquals("151935015482904", json["id"]!!.jsonPrimitive.content)
        assertEquals("0", json["position"]!!.jsonPrimitive.content)
        assertEquals("1.5", json["readTimeMinutes"]!!.jsonPrimitive.content)
    }

    @Test
    fun `a value that refers back to itself is cut where it repeats`() {
        val json = circularValue().toJsonElement(resolveLevel = 1).jsonObject

        // The cycle is cut, not the story: everything beside it still reads.
        assertEquals("a", json["name"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, json["self"])
    }

    @Test
    fun `a higher resolve level follows the cycle one step further`() {
        val json = circularValue().toJsonElement(resolveLevel = 2).jsonObject

        assertEquals("a", json["self"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, json["self"]!!.jsonObject["self"])
    }

    @Test
    fun `resolve level 0 still reads the story it is given`() {
        // Level 0 means relations are not resolved, not that nothing is read. Nothing here resolves
        // a relation in the first place — the editor inlines the ones it was asked for before the
        // payload ever arrives — so a level of 0 has a whole story to read just as level 1 does.
        val json = storyValue().toJsonElement(resolveLevel = 0).jsonObject

        assertEquals("post1", json["name"]!!.jsonPrimitive.content)
        assertEquals("x", json["content"]!!.jsonObject["nested"]!!.jsonObject["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun `resolve level 0 still cuts a cycle`() {
        val json = circularValue().toJsonElement(resolveLevel = 0).jsonObject

        assertEquals("a", json["name"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, json["self"])
    }

    @Test
    fun `the same value reached twice side by side is not a cycle`() {
        // Sharing one value between two fields is a tree, not a cycle: both sides read.
        val json = sharedValue().toJsonElement(resolveLevel = 1).jsonObject

        assertEquals("1", json["left"]!!.jsonObject["n"]!!.jsonPrimitive.content)
        assertEquals("1", json["right"]!!.jsonObject["n"]!!.jsonPrimitive.content)
    }
}

private fun storyValue(): JsAny = js(
    """({
        id: 151935015482904,
        name: 'post1',
        published_at: null,
        position: 0,
        readTimeMinutes: 1.5,
        is_startpage: false,
        tag_list: ['a', 'b'],
        content: { component: 'post', nested: { title: 'x' } }
    })"""
)

private fun circularValue(): JsAny = js("{ var a = { name: 'a' }; a.self = a; return a; }")

private fun sharedValue(): JsAny = js("{ var one = { n: 1 }; return { left: one, right: one }; }")
