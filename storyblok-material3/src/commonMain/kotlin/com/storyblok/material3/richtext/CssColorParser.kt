package com.storyblok.material3.richtext

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

internal object CssColorParser {

    private val rgbRegex =
        Regex("""rgb\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*\)""", RegexOption.IGNORE_CASE)

    private val rgbaRegex =
        Regex("""rgba\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*([0-9.]+)\s*\)""", RegexOption.IGNORE_CASE)

    private val hexRegex =
        Regex("""^#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})$""", RegexOption.IGNORE_CASE)

    private val namedColors = mapOf(
        "black" to Color(0xFF000000),
        "white" to Color(0xFFFFFFFF),
        "red" to Color(0xFFFF0000),
        "green" to Color(0xFF00FF00),
        "blue" to Color(0xFF0000FF),
        "gray" to Color(0xFF808080),
        "grey" to Color(0xFF808080),
        "yellow" to Color(0xFFFFFF00),
        "cyan" to Color(0xFF00FFFF),
        "magenta" to Color(0xFFFF00FF),
        "transparent" to Color(0x00000000)
    )

    fun parse(input: String?): Color? {
        if (input.isNullOrBlank()) return null

        val color = input.trim().lowercase()

        return when {
            color.startsWith("#") -> parseHex(color)
            color.startsWith("rgba") -> parseRgba(color)
            color.startsWith("rgb") -> parseRgb(color)
            namedColors.containsKey(color) -> namedColors[color]
            else -> null
        }
    }

    private fun parseHex(hex: String): Color? {
        if (!hexRegex.matches(hex)) return null

        val clean = hex.removePrefix("#")

        return when (clean.length) {
            3 -> {
                val r = "${clean[0]}${clean[0]}".toInt(16)
                val g = "${clean[1]}${clean[1]}".toInt(16)
                val b = "${clean[2]}${clean[2]}".toInt(16)
                Color(r, g, b)
            }
            6 -> {
                val r = clean.substring(0, 2).toInt(16)
                val g = clean.substring(2, 4).toInt(16)
                val b = clean.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                val a = clean.substring(0, 2).toInt(16)
                val r = clean.substring(2, 4).toInt(16)
                val g = clean.substring(4, 6).toInt(16)
                val b = clean.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    }

    private fun parseRgb(rgb: String): Color? {
        val match = rgbRegex.matchEntire(rgb) ?: return null
        val (r, g, b) = match.destructured

        return Color(
            r.toInt().coerceIn(0, 255),
            g.toInt().coerceIn(0, 255),
            b.toInt().coerceIn(0, 255)
        )
    }

    private fun parseRgba(rgba: String): Color? {
        val match = rgbaRegex.matchEntire(rgba) ?: return null
        val (r, g, b, aRaw) = match.destructured

        val alpha = aRaw.toFloatOrNull() ?: return null

        val a = if (alpha <= 1f) {
            (alpha * 255).roundToInt()
        } else {
            alpha.roundToInt()
        }.coerceIn(0, 255)

        return Color(
            r.toInt().coerceIn(0, 255),
            g.toInt().coerceIn(0, 255),
            b.toInt().coerceIn(0, 255),
            a
        )
    }
}