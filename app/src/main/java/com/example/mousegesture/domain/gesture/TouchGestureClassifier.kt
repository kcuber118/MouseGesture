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
 * - onElapsedTime(ms): check if held beyond [longPressDurationThresholdMs] → LONG_PRESS (early detection)
 * - onUp(elapsedTimeMs): classify based on accumulated state
 *   - If already MOVE → MOVE
 *   - If already LONG_PRESS (detected via onElapsedTime) → LONG_PRESS
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
    private var classifiedAsLongPress = false

    /**
     * Reset state when finger goes down.
     */
    fun onDown() {
        totalDistance = 0f
        classifiedAsMove = false
        classifiedAsLongPress = false
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
     * Check elapsed time while finger is still down.
     * Returns [GestureType.LONG_PRESS] if elapsed time exceeds the long-press
     * threshold and the gesture has not already been classified as MOVE.
     * Returns null if classification is not yet determined or already emitted.
     *
     * This enables early long-press detection (inject before finger lifts),
     * matching Android's long-press behavior where the action fires while held.
     */
    fun onElapsedTime(elapsedTimeMs: Long): GestureType? {
        if (classifiedAsMove) return null
        if (classifiedAsLongPress) return null
        if (elapsedTimeMs >= LONG_PRESS_DURATION_THRESHOLD_MS) {
            classifiedAsLongPress = true
            return GestureType.LONG_PRESS
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
        if (classifiedAsLongPress) {
            return GestureType.LONG_PRESS
        }
        if (elapsedTimeMs >= LONG_PRESS_DURATION_THRESHOLD_MS) {
            return GestureType.LONG_PRESS
        }
        return GestureType.TAP
    }
}
