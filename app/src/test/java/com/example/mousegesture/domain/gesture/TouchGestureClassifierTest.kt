package com.example.mousegesture.domain.gesture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TouchGestureClassifierTest {

    private lateinit var classifier: TouchGestureClassifier

    @Before
    fun setUp() {
        classifier = TouchGestureClassifier()
    }

    // ── Cycle 1: Down-up nhanh + ít dịch → TAP ──

    @Test
    fun onUp_quickReleaseSmallMovement_classifiesTap() {
        classifier.onDown()
        // No move events — zero distance
        val result = classifier.onUp(elapsedTimeMs = 100L)
        assertEquals(GestureType.TAP, result)
    }

    @Test
    fun onUp_quickReleaseWithTinyJitter_classifiesTap() {
        classifier.onDown()
        // Tiny movements well below threshold
        classifier.onMove(1f, 1f)
        classifier.onMove(-1f, 0f)
        val result = classifier.onUp(elapsedTimeMs = 150L)
        assertEquals(GestureType.TAP, result)
    }

    // ── Cycle 2: Dịch quá ngưỡng → MOVE ──

    @Test
    fun onMove_exceedsDistanceThreshold_classifiesMove() {
        classifier.onDown()
        // Single move exceeding threshold
        val result = classifier.onMove(30f, 0f)
        assertEquals(GestureType.MOVE, result)
    }

    @Test
    fun onMove_accumulatedDistanceExceedsThreshold_classifiesMove() {
        classifier.onDown()
        // Multiple small moves that accumulate past threshold
        classifier.onMove(10f, 0f)
        classifier.onMove(8f, 0f)
        val result = classifier.onMove(5f, 0f) // total = 23 > 20
        assertEquals(GestureType.MOVE, result)
    }

    @Test
    fun onUp_afterExceedingDistanceThreshold_classifiesMove() {
        classifier.onDown()
        classifier.onMove(30f, 0f) // exceeds threshold → MOVE
        val result = classifier.onUp(elapsedTimeMs = 50L)
        assertEquals(GestureType.MOVE, result)
    }

    // ── Cycle 3: Giữ quá ngưỡng thời gian → LONG_PRESS ──

    @Test
    fun onUp_heldBeyondDurationThreshold_classifiesLongPress() {
        classifier.onDown()
        // No movement, held for 600ms (> 500ms threshold)
        val result = classifier.onUp(elapsedTimeMs = 600L)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onUp_heldExactlyAtDurationThreshold_classifiesLongPress() {
        classifier.onDown()
        val result = classifier.onUp(elapsedTimeMs = TouchGestureClassifier.LONG_PRESS_DURATION_THRESHOLD_MS)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onUp_heldJustBelowDurationThreshold_classifiesTap() {
        classifier.onDown()
        val result = classifier.onUp(elapsedTimeMs = TouchGestureClassifier.LONG_PRESS_DURATION_THRESHOLD_MS - 1L)
        assertEquals(GestureType.TAP, result)
    }

    // ── Cycle 4: Jitter dưới ngưỡng không nhầm MOVE ──

    @Test
    fun onUp_jitterBelowThreshold_classifiesTap() {
        classifier.onDown()
        // Back-and-forth jitter: each move is small, total stays below threshold
        classifier.onMove(5f, 0f)
        classifier.onMove(-5f, 0f)
        classifier.onMove(4f, 0f)
        classifier.onMove(-4f, 0f)
        // total = 5+5+4+4 = 18 < 20 threshold
        val result = classifier.onUp(elapsedTimeMs = 100L)
        assertEquals(GestureType.TAP, result)
    }

    @Test
    fun onUp_jitterBelowThreshold_heldLong_classifiesLongPress() {
        classifier.onDown()
        // Small jitter that doesn't exceed move threshold
        classifier.onMove(3f, 0f)
        classifier.onMove(-3f, 0f)
        // total = 6 < 20
        val result = classifier.onUp(elapsedTimeMs = 700L)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    // ── Cycle 5: Đã MOVE → nhả vẫn MOVE ──

    @Test
    fun onUp_alreadyClassifiedMove_remainsMove_evenQuickRelease() {
        classifier.onDown()
        classifier.onMove(30f, 0f) // → MOVE
        // Even though up is quick, it's still MOVE
        val result = classifier.onUp(elapsedTimeMs = 100L)
        assertEquals(GestureType.MOVE, result)
    }

    @Test
    fun onUp_alreadyClassifiedMove_remainsMove_evenLongHold() {
        classifier.onDown()
        classifier.onMove(30f, 0f) // → MOVE
        // Even though held long, it's still MOVE — no long-press injected
        val result = classifier.onUp(elapsedTimeMs = 1000L)
        assertEquals(GestureType.MOVE, result)
    }

    // ── Cycle 6: onElapsedTime phát hiện long-press ngay khi finger vẫn đang chạm ──

    @Test
    fun onElapsedTime_exceedsDurationThreshold_classifiesLongPress() {
        classifier.onDown()
        // No movement, elapsed time exceeds long-press threshold
        val result = classifier.onElapsedTime(600L)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onElapsedTime_belowDurationThreshold_returnsNull() {
        classifier.onDown()
        // Not yet long-press
        val result = classifier.onElapsedTime(300L)
        assertNull(result)
    }

    @Test
    fun onElapsedTime_exactlyAtDurationThreshold_classifiesLongPress() {
        classifier.onDown()
        val result = classifier.onElapsedTime(
            TouchGestureClassifier.LONG_PRESS_DURATION_THRESHOLD_MS
        )
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onElapsedTime_alreadyClassifiedMove_returnsNull() {
        classifier.onDown()
        classifier.onMove(30f, 0f) // → MOVE
        // Even if time exceeds threshold, already MOVE — no long-press
        val result = classifier.onElapsedTime(600L)
        assertNull(result)
    }

    @Test
    fun onUp_afterLongPressAlreadyDetectedViaElapsedTime_doesNotReclassify() {
        classifier.onDown()
        classifier.onElapsedTime(600L) // → LONG_PRESS detected early
        // onUp should not reclassify as something else
        val result = classifier.onUp(elapsedTimeMs = 700L)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onUp_afterLongPressDetectedViaElapsedTime_quickRelease_stillLongPress() {
        classifier.onDown()
        classifier.onElapsedTime(500L) // → LONG_PRESS detected at threshold
        // Even if up happens quickly after detection
        val result = classifier.onUp(elapsedTimeMs = 510L)
        assertEquals(GestureType.LONG_PRESS, result)
    }

    @Test
    fun onDown_resetsLongPressDetectedState() {
        classifier.onDown()
        classifier.onElapsedTime(600L) // → LONG_PRESS
        classifier.onUp(elapsedTimeMs = 700L)

        // New gesture: onDown should reset
        classifier.onDown()
        val result = classifier.onUp(elapsedTimeMs = 100L)
        assertEquals(GestureType.TAP, result)
    }

    @Test
    fun onElapsedTime_calledMultipleTimes_onlyReturnsLongPressOnce() {
        classifier.onDown()
        val first = classifier.onElapsedTime(500L)
        assertEquals(GestureType.LONG_PRESS, first)
        // Subsequent calls should not return LONG_PRESS again
        val second = classifier.onElapsedTime(600L)
        assertNull(second)
    }

    @Test
    fun onDown_resetsStateFromClassification() {
        classifier.onDown()
        classifier.onMove(30f, 0f) // → MOVE
        classifier.onUp(elapsedTimeMs = 100L)

        // New gesture: onDown should reset
        classifier.onDown()
        val result = classifier.onUp(elapsedTimeMs = 100L)
        assertEquals(GestureType.TAP, result)
    }
}
