@file:OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)

package com.storyblok.cdn.schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlin.uuid.ExperimentalUuidApi

/**
 * Base class for all Storyblok components.
 *
 * Extend this class to define custom content types that match your Storyblok schema.
 */
@Serializable
public abstract class Component {
    /** Unique identifier for this component instance. */
    @SerialName("_uid")
    public val uid: String = ""

    /** Technical name of the component type. */
    public val component: String = ""

    @SerialName("_editable")
    internal val editable: String = ""

    /**
     * Fallback component for unknown or unregistered component types.
     *
     * Used when the JSON contains a component type not registered in the serializers module.
     */
    @Serializable
    @JsonIgnoreUnknownKeys
    public class Unknown : Component()
}
