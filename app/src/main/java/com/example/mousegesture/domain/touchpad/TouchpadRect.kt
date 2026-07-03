package com.example.mousegesture.domain.touchpad

/**
 * Axis-aligned rectangle representing the Touchpad's position and size on screen.
 * All values are in pixels (absolute screen coordinates).
 * Domain model — no Android framework imports.
 */
data class TouchpadRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
}
