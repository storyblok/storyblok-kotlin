package ktorplugin.cdn

import com.storyblok.cdn.schema.Component
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Fixture component for the price filter-query examples (`client.stories<Product> { Product::price ... }`). Storyblok
 * serializes number fields as strings, so `price` is a String.
 */
@Serializable
@SerialName("product")
class Product(val price: String? = null) : Component()
