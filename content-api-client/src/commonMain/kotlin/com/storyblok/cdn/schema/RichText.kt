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
        public val content: List<Text>
    ) : RichText() {

        public val level: Int get() = attributes.level
        public val textAlign: String? get() = attributes.textAlign

        @Serializable
        internal class Attributes(val level: Int, val textAlign: String? = null)
    }

    @Serializable
    @SerialName("bullet_list")
    public class BulletList internal constructor(public val content: List<ListItem>) : RichText()

    @Serializable
    @SerialName("ordered_list")
    public class OrderedList internal constructor(
        public val content: List<ListItem>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText() {
        public val order: Int? get() = attributes?.order
        @Serializable
        internal class Attributes(val order: Int? = null)
    }

    @Serializable
    @SerialName("image")
    public class Image internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes
    ) : RichText() {
        public val src: String get() = attributes.src
        public val alt: String? get() = attributes.alt
        public val title: String? get() = attributes.title
        @Serializable
        internal class Attributes(val src: String, val alt: String? = null, val title: String? = null)
    }

    @Serializable
    @SerialName("code_block")
    public class CodeBlock internal constructor(
        public val content: List<Text>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText() {
        public val language: String? get() = attributes?.language
        public val clazz: String? get() = attributes?.clazz
        @Serializable
        internal class Attributes(
            val language: String? = null,
            @SerialName("class") val clazz: String? = null
        )
    }

    @Serializable
    @SerialName("blockquote")
    public class Blockquote internal constructor(public val content: List<RichText>) : RichText()

    // Horizontal rule node
    @Serializable
    @SerialName("horizontal_rule")
    public class HorizontalRule : RichText()

    @Serializable
    @SerialName("table")
    public class Table internal constructor(public val content: List<TableRow>) : RichText()

    @Serializable
    @SerialName("table_row")
    public class TableRow internal constructor(public val content: List<TableCell>) : RichText()

    @Serializable
    @SerialName("tableHeader")
    public class TableHeader internal constructor(
        public val content: List<RichText>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText() {
        public val colspan: Int? get() = attributes?.colspan
        public val rowspan: Int? get() = attributes?.rowspan
        public val colwidth: List<Int>? get() = attributes?.colwidth
        @Serializable
        internal class Attributes(
            val colspan: Int? = null,
            val rowspan: Int? = null,
            val colwidth: List<Int>? = null
        )
    }

    @Serializable
    @SerialName("tableCell")
    public class TableCell internal constructor(
        public val content: List<RichText>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText() {
        public val columnSpan: Int? get() = attributes?.colspan
        public val rowSpan: Int? get() = attributes?.rowspan
        public val columnWidth: List<Int>? get() = attributes?.colwidth
        public val backgroundColor: String? get() = attributes?.backgroundColor
        @Serializable
        internal class Attributes(
            val colspan: Int? = null,
            val rowspan: Int? = null,
            val colwidth: List<Int>? = null,
            val backgroundColor: String? = null
        )
    }

    @Serializable
    @SerialName("paragraph")
    public class Paragraph internal constructor(
        public val content: List<RichText>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null,
    ) : RichText() {

        public val textAlign: String? get() = attributes?.textAlign

        @Serializable
        internal class Attributes(val textAlign: String? = null)
    }
    
    @Serializable
    @SerialName("text")
    public class Text(public val text: String = "", public val marks: List<Mark> = emptyList()) : RichText()

    @Serializable
    public sealed class Mark : RichText() {
        @Serializable
        @SerialName("bold")
        public class Bold : Mark()

        @Serializable
        @SerialName("italic")
        public class Italic : Mark()

        @Serializable
        @SerialName("underline")
        public class Underline : Mark()

        @Serializable
        @SerialName("strike")
        public class Strike : Mark()

        @Serializable
        @SerialName("code")
        public class Code : Mark()

        @Serializable
        @SerialName("subscript")
        public class Subscript : Mark()

        @Serializable
        @SerialName("superscript")
        public class Superscript : Mark()

        @Serializable
        @SerialName("link")
        public class Link internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes
        ): Mark() {
            public val href: String get() = attributes.href
            public val uuid: String? get() = attributes.uuid
            public val anchor: String? get() = attributes.anchor
            public val custom: String? get() = attributes.custom
            public val target: String? get() = attributes.target
            public val linktype: String get() = attributes.linktype

            @Serializable
            internal class Attributes(
                val href: String,
                val uuid: String? = null,
                val anchor: String? = null,
                val custom: String? = null,
                val target: String? = null,
                val linktype: String
            )
        }

        @Serializable
        @SerialName("textStyle")
        public class TextStyle internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes,
        ) : Mark() {
            public val color: String? get() = attributes.color.ifEmpty { null }

            @Serializable
            internal class Attributes(val color: String)
        }

        @Serializable
        @SerialName("highlight")
        public class Highlight internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes
        ) : Mark() {
            public val color: String? get() = attributes.color.ifEmpty { null }
            @Serializable
            internal class Attributes(val color: String)
        }
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

    @Serializable
    @SerialName("list_item")
    public class ListItem internal constructor(public val content: List<RichText>) : RichText()

    @Serializable
    @SerialName("emoji")
    public class Emoji internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes
    ) : RichText() {
        public val name: String get() = attributes.name
        public val emoji: String get() = attributes.emoji
        public val fallbackImage: String get() = attributes.fallbackImage

        @Serializable
        internal class Attributes(
            val name: String,
            val emoji: String,
            val fallbackImage: String
        )
    }

    @Serializable
    @SerialName("hard_break")
    public class HardBreak(
        public val marks: List<Mark> = emptyList()
    ) : RichText()
}