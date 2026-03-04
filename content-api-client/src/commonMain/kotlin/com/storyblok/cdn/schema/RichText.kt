@file:OptIn(ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
public sealed class RichText {

    public val type: String = ""

    @Serializable
    @SerialName("doc")
    public open class Document internal constructor(public val content: List<RichText>) : RichText()

    @Serializable
    @SerialName("heading")
    public class Heading internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes,
        internal val content: List<Text>
    ) : RichText() {

        public val level: Int get() = attributes.level
        public val text: String get() = content.single().text

        @Serializable
        internal class Attributes(val level: Int, val textAlign: String? = null)
    }

    @Serializable
    @SerialName("bullet_list")
    public class BulletList internal constructor(public val content: List<ListItem>) : RichText() {
    }

    @Serializable
    @SerialName("list_item")
    public class ListItem(public val content: List<RichText>) : RichText() {
    }

    @Serializable
    @SerialName("paragraph")
    public class Paragraph internal constructor(
        public val content: List<Text>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null,
    ) : RichText() {

        public val textAlign: String? get() = attributes?.textAlign

        @Serializable
        internal class Attributes(val textAlign: String? = null)
    }

    @Serializable
    @SerialName("text")
    public class Text(public val text: String, public val marks: List<Mark> = emptyList()) : RichText()

    @Serializable
    public sealed class Mark : RichText()

    @Serializable
    @SerialName("bold")
    public class BoldMark : Mark()

    @Serializable
    @SerialName("textStyle")
    public class TextStyleMark internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes,
    ) : Mark() {

        public val color: String get() = attributes.color

        @Serializable
        internal class Attributes(val color: String)
    }

    @Serializable
    @SerialName("blok")
    public class Blok internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes,
    ) : RichText() {

        public val body: List<Component> get() = attributes.body
        @Serializable
        internal class Attributes(val id: String, val body: List<Component>)
    }
}