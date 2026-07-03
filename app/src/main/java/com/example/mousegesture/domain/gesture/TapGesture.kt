package com.example.mousegesture.domain.gesture

/**
 * Data representing a tap gesture at absolute screen coordinates.
 * Domain model — no Android framework imports.
 */
data class TapGesture(
    val x: Float,
    val y: Float,
    val durationMs: Long,
)
