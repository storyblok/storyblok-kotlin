package com.example.jetnews

/** Where a story is in its fetch cycle. */
internal enum class ContentState {

    /** First fetch for this story: nothing to show yet, and no pull-to-refresh spinner. */
    Loading,

    /** A refresh the user pulled for, so the pull-to-refresh spinner shows. */
    Refreshing,

    /** Loading complete. */
    Loaded,

    /** The last fetch failed. Entering [Loading] or [Refreshing] clears it. */
    Failed;

    /** Whether this state should run a fetch. [Loaded] and [Failed] wait for the user to pull. */
    val isFetching: Boolean get() = this == Loading || this == Refreshing
}