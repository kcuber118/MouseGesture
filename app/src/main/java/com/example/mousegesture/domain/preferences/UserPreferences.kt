package com.example.mousegesture.domain.preferences

import com.example.mousegesture.domain.cursor.AccelerationCurve
import com.example.mousegesture.domain.model.Point
import com.example.mousegesture.domain.touchpad.TouchpadRect

/**
 * Immutable snapshot of user preferences.
 *
 * Domain model — no Android framework imports.
 * Persistence is handled by a separate repository implementation.
 */
data class UserPreferences(
    /** Sensitivity multiplier for the acceleration curve. Default 1.0. */
    val sensitivity: Float = AccelerationCurve.DEFAULT_SENSITIVITY,
    /** Whether the overlay should be visible on service start. Default true. Per ADR-0002. */
    val overlayVisible: Boolean = true,
    /** Last known touchpad rect, null if never saved. */
    val touchpadRect: TouchpadRect? = null,
    /** Last known cursor position, null if never saved. */
    val cursorPosition: Point? = null,
) {
    fun withSensitivity(newSensitivity: Float): UserPreferences =
        copy(sensitivity = newSensitivity)

    fun withOverlayVisible(visible: Boolean): UserPreferences =
        copy(overlayVisible = visible)

    fun withTouchpadRect(rect: TouchpadRect): UserPreferences =
        copy(touchpadRect = rect)

    fun withCursorPosition(position: Point): UserPreferences =
        copy(cursorPosition = position)
}
