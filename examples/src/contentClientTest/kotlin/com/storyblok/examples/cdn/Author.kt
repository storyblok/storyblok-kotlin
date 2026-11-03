package com.storyblok.examples.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fixture component for the filter-query examples that address a field of the example space's `author` content type. */
@Serializable
@SerialName("author")
class Author(val name: String? = null) : Component()
