/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetnews.model

import com.storyblok.cdn.schema.Component
import com.storyblok.cdn.schema.Link
import com.storyblok.cdn.schema.RichText
import com.storyblok.cdn.schema.Story
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.serializers.FormattedLocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("page")
data class Page(val body: List<Component>) : Component()

@Serializable
@SerialName("highlighted")
class HighlightedPost(val title: String, val post: Story<Post>) : Component()

@Serializable
@SerialName("recent")
class RecentPosts(val posts: List<Story<Post>>) : Component()

@Serializable
@SerialName("popular")
class PopularPosts(val title: String, val posts: List<Story<Post>>) : Component()

@Serializable
@SerialName("recommended")
class RecommendedPosts(val strapline: String, val posts: List<Story<Post>>) : Component()

@Serializable
@SerialName("post")
data class Post(
    val title: String,
    val subtitle: String? = null,
    val url: Link,
    val imageId: String,
    val imageThumbId: String,
    val body: List<Component>,
    @Serializable(with = StoryblokDateTimeSerializer::class)
    val date: LocalDateTime,
    val author: Story<Author>,
    val readTimeMinutes: Int
) : Component()

@Serializable
@SerialName("author")
data class Author(val name: String, val url: Link? = null) : Component()

@Serializable
@SerialName("header")
data class Header(val alternativeTitle: String, val alternativeSubtitle: String) : Component()

@Serializable
@SerialName("metadata")
class Metadata() : Component()

@Serializable
@SerialName("body")
data class Body(val text: RichText) : Component()

data class Paragraph(val type: ParagraphType, val text: String, val markups: List<Markup> = emptyList())

data class Markup(val type: MarkupType, val start: Int, val end: Int, val href: String? = null)

enum class MarkupType {
    Link,
    Code,
    Italic,
    Bold,
}

enum class ParagraphType {
    Title,
    Caption,
    Header,
    Subhead,
    Text,
    CodeBlock,
    Quote,
    Bullet,
}

// serializes LocalDateTime(2020, 1, 4, 12, 30) as the string "2020-01-04 12:30"
object StoryblokDateTimeSerializer : FormattedLocalDateTimeSerializer("com.example.jetnews.model.StoryblokDateTime",
    LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Formats.ISO)
    }
)
