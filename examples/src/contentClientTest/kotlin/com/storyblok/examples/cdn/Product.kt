package com.storyblok.examples.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fixture component for the filter-query examples that address a field of the example space's `product` content type
 * (`client.stories<Product> { Product::price ... }`). Storyblok serializes number fields as strings, so `price` is a
 * String.
 */
@Serializable
@SerialName("product")
class Product(
    val name: String? = null,
    val price: String? = null,
    val featured: Boolean? = null,
    val description: String? = null,
) : Component()
