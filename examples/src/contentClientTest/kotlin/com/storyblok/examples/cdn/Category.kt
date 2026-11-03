package com.storyblok.examples.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Fixture component for the filter-query examples that address a field of the example space's `category` content type. */
@Serializable
@SerialName("category")
class Category(val name: String? = null) : Component()
