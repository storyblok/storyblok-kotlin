@file:OptIn(ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Base class for Storyblok rich text nodes.
 *
 * Represents the hierarchical structure of rich text content from the Storyblok editor.
 */
@Serializable
@JsonClassDiscriminator("type")
public sealed class RichText {

    /** Technical name of the node type. */
    public val type: String = ""

    /** Text alignment options for paragraph and heading nodes. */
    public enum class TextAlign {
        /** Left-aligned text. */
        @SerialName("left")
        Left,
        /** Right-aligned text. */
        @SerialName("right")
        Right,
        /** Center-aligned text. */
        @SerialName("center")
        Center,
    }

    /**
     * Interface for rich text nodes that contain child nodes.
     */
    public sealed interface Composite {
        /** Child nodes contained within this element. */
        public val content: List<RichText>

        /** Recursively flattens all descendant nodes into a sequence. */
        public fun flatten(): Sequence<RichText> = content.asSequence()
            .flatMap { if(it is Composite) it.flatten() else sequenceOf(it) }
    }

    /** Root document node containing all rich text content. */
    @Serializable
    @SerialName("doc")
    public open class Document internal constructor(public override val content: List<RichText>) : RichText(), Composite

    /** Heading node with configurable level (1-6). */
    @Serializable
    @SerialName("heading")
    public class Heading internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes,
        public override val content: List<RichText>
    ) : RichText(), Composite {

        /** Heading level (1-6). */
        public val level: Int get() = attributes.level
        /** Optional text alignment. */
        public val textAlign: TextAlign? get() = attributes.textAlign

        @Serializable
        internal class Attributes(val level: Int, val textAlign: TextAlign? = null)
    }

    /** Unordered (bullet) list node. */
    @Serializable
    @SerialName("bullet_list")
    public class BulletList internal constructor(public override val content: List<ListItem>) : RichText(), Composite

    /** Ordered (numbered) list node. */
    @Serializable
    @SerialName("ordered_list")
    public class OrderedList internal constructor(
        public override val content: List<ListItem>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText(), Composite {
        /** Starting number for the list. */
        public val order: Int? get() = attributes?.order
        @Serializable
        internal class Attributes(val order: Int? = null)
    }

    /** Image node with source and metadata. */
    @Serializable
    @SerialName("image")
    public class Image internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes
    ) : RichText() {
        /** Unique identifier for the image. */
        public val id: String get() = attributes.id
        /** Image source URL. */
        public val src: String get() = attributes.src
        /** Alternative text for accessibility. */
        public val alt: String? get() = attributes.alt
        /** Image title. */
        public val title: String? get() = attributes.title
        /** Source or origin of the image. */
        public val source: String? get() = attributes.source
        /** Copyright information. */
        public val copyright: String? get() = attributes.copyright
        /** Custom metadata key-value pairs. */
        public val metadata: Map<String, String>? get() = attributes.metadata

        @Serializable
        internal class Attributes(
            val id: String,
            val src: String,
            val alt: String? = null,
            val title: String? = null,
            val source: String? = null,
            val copyright: String? = null,
            @SerialName("meta_data")
            val metadata: Map<String, String>? = null,
        )
    }

    /** Code block node with optional language hint. */
    @Serializable
    @SerialName("code_block")
    public class CodeBlock internal constructor(
        public override val content: List<RichText>,
        @SerialName("attrs")
        internal val attributes: Attributes? = null
    ) : RichText(), Composite {
        /** Programming language for syntax highlighting. */
        public val language: String? get() = attributes?.language
        /** CSS class name. */
        public val clazz: String? get() = attributes?.clazz
        @Serializable
        internal class Attributes(
            val language: String? = null,
            @SerialName("class") val clazz: String? = null
        )
    }

    /** Block quote node. */
    @Serializable
    @SerialName("blockquote")
    public class Blockquote internal constructor(public override val content: List<RichText>) : RichText(), Composite

    /** Horizontal rule (divider) node. */
    @Serializable
    @SerialName("horizontal_rule")
    public class HorizontalRule : RichText()

    /** Table container node. */
    @Serializable
    @SerialName("table")
    public class Table internal constructor(public override val content: List<TableRow>) : RichText(), Composite

    /** Table row node. */
    @Serializable
    @SerialName("table_row")
    public class TableRow internal constructor(public override val content: List<TableElement>) : RichText(), Composite

    /** Base class for table cells (header and data cells). */
    @Serializable
    public sealed class TableElement : RichText(), Composite {
        /** Child nodes contained within this cell. */
        public override val content: List<RichText> = emptyList()
        internal abstract val attributes: Attributes?
        /** Number of columns this cell spans. */
        public val columnSpan: Int? get() = attributes?.colspan
        /** Number of rows this cell spans. */
        public val rowSpan: Int? get() = attributes?.rowspan
        /** Column width values in pixels. */
        public val columnWidth: List<Int>? get() = attributes?.colwidth
        @Serializable
        internal sealed class Attributes(
            val colspan: Int? = null,
            val rowspan: Int? = null,
            val colwidth: List<Int>? = null,
        )
    }

    /** Table header cell. */
    @Serializable
    @SerialName("tableHeader")
    public class TableHeader internal constructor(
        @SerialName("attrs")
        override val attributes: Attributes? = null
    ) : TableElement() {
        @Serializable
        internal class Attributes : TableElement.Attributes()
    }

    /** Table data cell. */
    @Serializable
    @SerialName("tableCell")
    public class TableCell internal constructor(
        @SerialName("attrs")
        override val attributes: Attributes? = null
    ) : TableElement() {
        /** Background color of the cell. */
        public val backgroundColor: String? get() = attributes?.backgroundColor
        @Serializable
        internal class Attributes(val backgroundColor: String? = null) : TableElement.Attributes()
    }

    /** Paragraph node with optional text alignment. */
    @Serializable
    @SerialName("paragraph")
    public class Paragraph internal constructor(
        public override val content: List<RichText> = emptyList(),
        @SerialName("attrs")
        internal val attributes: Attributes? = null,
    ) : RichText(), Composite {

        /** Optional text alignment. */
        public val textAlign: TextAlign? get() = attributes?.textAlign

        @Serializable
        internal class Attributes(val textAlign: TextAlign? = null)
    }
    
    /** Text node containing plain text with optional marks (formatting). */
    @Serializable
    @SerialName("text")
    public class Text(
        /** The text content. */
        public val text: String = "",
        /** Applied formatting marks. */
        public val marks: List<Mark> = emptyList()
    ) : RichText()

    /**
     * Base class for text formatting marks.
     *
     * Marks represent inline formatting applied to text content.
     */
    @Serializable
    public sealed class Mark : RichText() {
        /** Bold text formatting. */
        @Serializable
        @SerialName("bold")
        public class Bold : Mark()

        /** Italic text formatting. */
        @Serializable
        @SerialName("italic")
        public class Italic : Mark()

        /** Underlined text formatting. */
        @Serializable
        @SerialName("underline")
        public class Underline : Mark()

        /** Strikethrough text formatting. */
        @Serializable
        @SerialName("strike")
        public class Strike : Mark()

        /** Inline code formatting. */
        @Serializable
        @SerialName("code")
        public class Code : Mark()

        /** Subscript text formatting. */
        @Serializable
        @SerialName("subscript")
        public class Subscript : Mark()

        /** Superscript text formatting. */
        @Serializable
        @SerialName("superscript")
        public class Superscript : Mark()

        /** Hyperlink mark with URL and metadata. */
        @Serializable
        @SerialName("link")
        public class Link internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes
        ): Mark() {
            /** Link destination URL. */
            public val href: String get() = attributes.href
            /** UUID of linked story (for internal links). */
            public val uuid: String? get() = attributes.uuid
            /** Anchor fragment within the target. */
            public val anchor: String? get() = attributes.anchor
            /** Custom attributes. */
            public val custom: Map<String, String>? get() = attributes.custom
            /** Link target attribute. */
            public val target: String? get() = attributes.target
            /** Type of link. */
            public val linktype: String get() = attributes.linktype

            @Serializable
            internal class Attributes(
                val href: String,
                val uuid: String? = null,
                val anchor: String? = null,
                val custom: Map<String, String>? = null,
                val target: String? = null,
                val linktype: String
            )
        }

        /** Text color styling. */
        @Serializable
        @SerialName("textStyle")
        public class TextStyle internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes,
        ) : Mark() {
            /** Text color value. */
            public val color: String? get() = attributes.color.ifEmpty { null }

            @Serializable
            internal class Attributes(val color: String)
        }

        /** Text highlight/background color. */
        @Serializable
        @SerialName("highlight")
        public class Highlight internal constructor(
            @SerialName("attrs")
            internal val attributes: Attributes
        ) : Mark() {
            /** Highlight color value. */
            public val color: String? get() = attributes.color.ifEmpty { null }
            @Serializable
            internal class Attributes(val color: String)
        }
    }

    /** Embedded component block within rich text. */
    @Serializable
    @SerialName("blok")
    public class Blok internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes,
    ) : RichText() {

        /** List of embedded components. */
        public val body: List<Component> get() = attributes.body
        @Serializable
        internal class Attributes(val id: String, val body: List<Component>)
    }

    /** List item node. */
    @Serializable
    @SerialName("list_item")
    public class ListItem internal constructor(public override val content: List<RichText>) : RichText(), Composite

    /** Emoji node with fallback image support. */
    @Serializable
    @SerialName("emoji")
    public class Emoji internal constructor(
        @SerialName("attrs")
        internal val attributes: Attributes
    ) : RichText() {
        /** Emoji name/identifier. */
        public val name: String get() = attributes.name
        /** Unicode emoji character. */
        public val emoji: String get() = attributes.emoji
        /** Fallback image URL for unsupported emoji. */
        public val fallbackImage: String get() = attributes.fallbackImage

        @Serializable
        internal class Attributes(
            val name: String,
            val emoji: String,
            val fallbackImage: String
        )
    }

    /** Hard line break node. */
    @Serializable
    @SerialName("hard_break")
    public class HardBreak(
        /** Applied formatting marks carried across the break. */
        public val marks: List<Mark> = emptyList()
    ) : RichText()
}