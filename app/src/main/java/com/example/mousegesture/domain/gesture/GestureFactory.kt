package com.example.mousegesture.domain.gesture

import com.example.mousegesture.domain.model.Point

/**
 * Creates gesture data objects from domain models.
 * Domain logic — no Android framework imports.
 * The conversion to GestureDescription (Android framework) happens in the service layer.
 */
class GestureFactory {

    companion object {
        const val DEFAULT_TAP_DURATION_MS = 50L
        const val DEFAULT_LONG_PRESS_DURATION_MS = 500L
    }

    /**
     * Create a tap gesture at the given absolute screen position.
     */
    fun createTapAt(point: Point): TapGesture {
        return TapGesture(
            x = point.x,
            y = point.y,
            durationMs = DEFAULT_TAP_DURATION_MS,
        )
    }

    /**
     * Create a long-press gesture at the given absolute screen position.
     */
    fun createLongPressAt(point: Point): LongPressGesture {
        return LongPressGesture(
            x = point.x,
            y = point.y,
            durationMs = DEFAULT_LONG_PRESS_DURATION_MS,
        )
    }
}
