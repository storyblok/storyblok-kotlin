@file:OptIn(ExperimentalWasmJsInterop::class)

package com.storyblok.cdn

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.JsBoolean
import js.collections.JsMap
import js.objects.Object
import js.reflect.Reflect
import kotlin.js.JsName
import kotlin.js.JsNumber
import kotlin.js.JsString
import kotlin.js.get
import kotlin.js.length
import kotlin.js.toBoolean
import kotlin.js.toDouble
import kotlin.js.toInt
import kotlin.js.toJsNumber

/*
 * Reading a JavaScript value into a [JsonElement] tree.
 *
 * Kotlin/JS has `Json.decodeFromDynamic`, which decodes a JS value straight into a Kotlin object. It
 * takes a `dynamic`, a type Kotlin/Wasm does not have, so it is absent here
 * ([kotlinx.serialization#3129](https://github.com/Kotlin/kotlinx.serialization/issues/3129) asks
 * for a `decodeFromJsAny`). Until that exists, converting to a [JsonElement] and decoding from it
 * reaches the same place through public API only, without serializing to a string on the way.
 */

/** `Array`, for telling a list apart from an object — `typeof` calls both of them `"object"`. */
@JsName("Array")
private external object JsArrays {
    fun isArray(value: JsAny?): Boolean
}

/**
 * This value as a [JsonElement], with [depths] counting how many times each value on the path to it
 * is already being read, so that a cycle is cut rather than followed forever.
 *
 * [resolveLevel] is how many times a value on that path may be re-entered, so a level above 1
 * follows a cycle that much further. It is never below 1: a story has to be entered before any of
 * it can be read.
 */
internal fun JsAny?.toJsonElement(resolveLevel: Int, depths: JsMap<JsAny, JsNumber> = JsMap()): JsonElement {
    // JavaScript's `null` and `undefined` both arrive as Kotlin's `null`.
    if (this == null) return JsonNull

    return when (this) {
        is JsBoolean -> JsonPrimitive(toBoolean())
        // JavaScript has one number type. Whole values are written without a fractional part, as
        // `JSON.stringify` writes them, so that they still decode as the integers the schema declares.
        is JsNumber -> toDouble().let { number ->
            if (number % 1.0 == 0.0 && number.isFinite()) JsonPrimitive(number.toLong()) else JsonPrimitive(number)
        }
        is JsString -> JsonPrimitive(toString())
        // Everything else is an object or an array. A function or a symbol would land here too, but
        // neither survives the structured clone `postMessage` hands the story over by.
        else -> {
            val depth = depths.get(this)?.toInt() ?: 0
            // At least one, whatever the level: a value has to be entered before there is anything
            // to read it into, and a level of 0 means relations are left unresolved rather than
            // that nothing is read. Nothing here resolves a relation in any case — the editor
            // inlines the ones it was asked for before the payload arrives — so the level only sets
            // how far a value that refers back to itself is followed.
            if (depth >= resolveLevel.coerceAtLeast(1)) return JsonNull
            depths.set(this, (depth + 1).toJsNumber())
            try {
                if (JsArrays.isArray(this)) {
                    @Suppress("UNCHECKED_CAST")
                    val values = this as JsArray<JsAny?>
                    JsonArray((0 until values.length).map { values[it].toJsonElement(resolveLevel, depths) })
                } else {
                    val keys = Object.keys(this)
                    JsonObject(
                        (0 until keys.length).associate { index ->
                            val key = keys[index]!!.toString()
                            key to Reflect.get(this, key).toJsonElement(resolveLevel, depths)
                        }
                    )
                }
            } finally {
                // Only the path counts: the same value reached twice side by side is not a cycle.
                depths.delete(this)
            }
        }
    }
}
