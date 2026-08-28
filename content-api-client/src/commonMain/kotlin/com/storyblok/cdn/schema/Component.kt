package com.storyblok.cdn.schema

import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Base class for all Storyblok components.
 *
 * Extend this class to define custom content types that match your Storyblok schema.
 */
@Serializable
public abstract class Component {
    /** Unique identifier for this component instance. */
    @JsonNames("_uid")
    public val uid: String = ""

    /** Technical name of the component type. */
    public val component: String = ""

    @JsonNames("_editable")
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
