package com.example.mousegesture.domain.gesture

/**
 * Data representing a long-press gesture at absolute screen coordinates.
 * Domain model — no Android framework imports.
 */
data class LongPressGesture(
    val x: Float,
    val y: Float,
    val durationMs: Long,
)
