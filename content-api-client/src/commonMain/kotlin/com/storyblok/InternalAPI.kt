package com.storyblok

/**
 * Marks declarations that are internal to the Storyblok client. They are exposed only so that the client's own
 * modules and tests can reach them, and may change or be removed without notice — do not depend on them from
 * application code.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal to the Storyblok client and may change or be removed without notice.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class InternalAPI
