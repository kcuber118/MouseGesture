package com.example.mousegesture.domain.gesture

/**
 * Classifies a single-finger touch gesture on the Touchpad into
 * [GestureType.TAP], [GestureType.LONG_PRESS], or [GestureType.MOVE]
 * based on duration and total distance thresholds.
 *
 * Domain logic — no Android framework imports.
 *
 * Thresholds are centralized in [Companion] constants.
 *
 * State machine:
 * - onDown(): reset state
 * - onMove(dx, dy): accumulate distance; if exceeds [moveDistanceThreshold] → MOVE
 * - onUp(elapsedTimeMs): classify based on accumulated state
 *   - If already MOVE → MOVE
 *   - If elapsed > [longPressDurationThresholdMs] and distance ≤ [moveDistanceThreshold] → LONG_PRESS
 *   - Otherwise → TAP
 */
class TouchGestureClassifier {

    companion object {
        /** Duration threshold (ms) beyond which a hold is classified as long-press. */
        const val LONG_PRESS_DURATION_THRESHOLD_MS = 500L

        /** Total distance threshold (px) beyond which movement is classified as MOVE. */
        const val MOVE_DISTANCE_THRESHOLD_PX = 20f
    }

    private var totalDistance = 0f
    private var classifiedAsMove = false

    /**
     * Reset state when finger goes down.
     */
    fun onDown() {
        totalDistance = 0f
        classifiedAsMove = false
    }

    /**
     * Accumulate movement distance when finger moves.
     * Returns [GestureType.MOVE] if total distance exceeds threshold,
     * or null if classification is not yet determined.
     */
    fun onMove(dx: Float, dy: Float): GestureType? {
        totalDistance += kotlin.math.hypot(dx, dy)
        if (totalDistance > MOVE_DISTANCE_THRESHOLD_PX) {
            classifiedAsMove = true
            return GestureType.MOVE
        }
        return null
    }

    /**
     * Classify the gesture when finger goes up.
     * [elapsedTimeMs] is the time between DOWN and UP events.
     */
    fun onUp(elapsedTimeMs: Long): GestureType {
        if (classifiedAsMove) {
            return GestureType.MOVE
        }
        if (elapsedTimeMs >= LONG_PRESS_DURATION_THRESHOLD_MS) {
            return GestureType.LONG_PRESS
        }
        return GestureType.TAP
    }
}
