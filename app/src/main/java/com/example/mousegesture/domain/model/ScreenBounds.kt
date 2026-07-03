package com.example.mousegesture.domain.model

/**
 * Screen dimensions used as boundary for cursor clamping.
 * Domain model — no Android framework imports.
 */
data class ScreenBounds(val width: Float, val height: Float) {
    val centerX: Float get() = width / 2f
    val centerY: Float get() = height / 2f
}
