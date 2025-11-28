package com.storyblok.ktor

/**
 * A resource's version determines who can access it.
 *
 * Resources have two potential versions: draft (unpublished) or published.
 */
public enum class Version(internal val value: String) {
    /** This resource will only appear in preview versions of your website. */
    Draft("draft"),
    /** This resource will appear live on your website. */
    Published("published")
}