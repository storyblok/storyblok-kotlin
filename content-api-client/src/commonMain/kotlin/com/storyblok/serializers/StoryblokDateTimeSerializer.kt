package com.storyblok.serializers

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.char
import kotlinx.datetime.serializers.FormattedLocalDateTimeSerializer

public object StoryblokDateTimeSerializer : FormattedLocalDateTimeSerializer(
    "com.storyblok.serializers.StoryblokDateTime",
    LocalDateTime.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Formats.ISO)
    }
)