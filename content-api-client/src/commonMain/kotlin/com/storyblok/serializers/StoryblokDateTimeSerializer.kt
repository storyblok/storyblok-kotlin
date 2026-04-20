package com.storyblok.serializers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.serializers.FormattedLocalDateTimeSerializer

/**
 * Serializer for [LocalDateTime] values using the Storyblok date-time format (`yyyy-MM-dd HH:mm:ss`).
 *
 * #### Example
 * ```kotlin
 * @Serializable
 * @SerialName("event")
 * class Event(
 *     @Serializable(with = StoryblokDateTimeSerializer::class)
 *     val date: LocalDateTime
 * ) : Component()
 * ```
 */
public object StoryblokDateTimeSerializer : FormattedLocalDateTimeSerializer(
    "com.storyblok.serializers.StoryblokDateTime",
    LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Formats.ISO)
    }
)
