@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlin.uuid.ExperimentalUuidApi

@Serializable
public abstract class Component {
    @SerialName("_uid")
    public val uid: String = ""
    public val component: String = ""
    @SerialName("_editable")
    internal val editable: String = ""

    @Serializable
    @JsonIgnoreUnknownKeys
    public class Unknown : Component()
}