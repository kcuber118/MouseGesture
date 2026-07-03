package com.example.mousegesture.domain.cursor

import com.example.mousegesture.domain.model.Point
import com.example.mousegesture.domain.model.ScreenBounds

/**
 * Controls cursor position on screen.
 * Uses [AccelerationCurve] for non-linear movement: slow swipes are precise, fast swipes are boosted.
 * Domain logic — no Android framework imports.
 */
class CursorController(
    private val bounds: ScreenBounds,
    private var curve: AccelerationCurve = AccelerationCurve(),
) {

    var position: Point = Point(bounds.centerX, bounds.centerY)
        private set

    /**
     * Move cursor by relative delta with a given speed, using acceleration curve.
     * Clamped to screen bounds.
     *
     * @param deltaX Raw touch delta in pixels (x-axis).
     * @param deltaY Raw touch delta in pixels (y-axis).
     * @param speed Movement speed in px/ms. Default 0f = linear fallback.
     */
    fun moveBy(deltaX: Float, deltaY: Float, speed: Float = 0f) {
        val acceleratedX = curve.apply(deltaX, speed)
        val acceleratedY = curve.apply(deltaY, speed)
        position = Point(
            x = (position.x + acceleratedX).coerceIn(0f, bounds.width),
            y = (position.y + acceleratedY).coerceIn(0f, bounds.height),
        )
    }

    /**
     * Update the acceleration curve (e.g. when sensitivity changes).
     * Does NOT move the cursor — only affects future moveBy calls.
     */
    fun updateCurve(newCurve: AccelerationCurve) {
        curve = newCurve
    }
}
