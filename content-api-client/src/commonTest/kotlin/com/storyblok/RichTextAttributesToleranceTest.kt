package com.storyblok

import com.storyblok.cdn.schema.RichText
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A rich text node's `attrs` is Storyblok's to extend, and a Draft client parses strictly — only
 * [Version.Published][com.storyblok.cdn.Version.Published] turns `ignoreUnknownKeys` on. So a key
 * added on Storyblok's side fails the whole story in the Visual Editor, where every preview runs
 * Draft, however deeply the node carrying it is nested.
 *
 * Every attributes type therefore has to tolerate keys it does not model. This walks the schema
 * rather than listing the types, so a node added later is held to the same rule.
 */
class RichTextAttributesToleranceTest {

    @Test
    fun `every attributes type tolerates keys it does not model`() {
        val attributes = serializer<RichText>().descriptor.attributeDescriptors()

        // Guards the walk itself: a traversal that quietly reaches nothing would pass every
        // assertion below. Raise this alongside any attributes type added to the schema.
        assertEquals(14, attributes.size, "walked the schema and reached ${attributes.keys.sorted()}")

        val strict = attributes
            .filterValues { descriptor -> descriptor.annotations.none { it is JsonIgnoreUnknownKeys } }
            .keys

        assertTrue(
            strict.isEmpty(),
            "these attributes types reject keys Storyblok may add, failing the whole story: $strict",
        )
    }

    /**
     * Keyed by name to collapse the several paths that reach the same type, and cycle-broken per
     * path rather than globally: rich text nests itself, and a descriptor marked seen once for the
     * whole walk cuts off every sibling branch that would reach the rest of the schema through it.
     *
     * Names are unwrapped from their nullable form — `attrs` is absent often enough that most of
     * these are declared nullable, and `Attributes?` is what the descriptor is called.
     */
    private fun SerialDescriptor.attributeDescriptors(
        path: Set<String> = emptySet(),
    ): Map<String, SerialDescriptor> {
        val name = serialName.removeSuffix("?")
        if (name in path) return emptyMap()
        val nested = elementDescriptors
            .fold(emptyMap<String, SerialDescriptor>()) { found, element ->
                found + element.attributeDescriptors(path + name)
            }
        val isAttributes = name.substringAfterLast('.').endsWith("Attributes")
        return if (isAttributes) nested + (name.substringAfter("RichText.") to this) else nested
    }
}
