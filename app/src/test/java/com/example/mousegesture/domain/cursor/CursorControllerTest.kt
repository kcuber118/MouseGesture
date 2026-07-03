package com.example.mousegesture.domain.cursor

import com.example.mousegesture.domain.model.Point
import com.example.mousegesture.domain.model.ScreenBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CursorControllerTest {

    private lateinit var bounds: ScreenBounds
    private lateinit var controller: CursorController

    @Before
    fun setUp() {
        bounds = ScreenBounds(width = 1080f, height = 2400f)
        controller = CursorController(bounds)
    }

    @Test
    fun moveBy_positiveDelta_movesCursorRightAndDown() {
        controller.moveBy(100f, 200f)
        val pos = controller.position
        // Initial position is center (540, 1200), + (100, 200) = (640, 1400)
        assertEquals(Point(640f, 1400f), pos)
    }

    @Test
    fun moveBy_negativeDelta_movesCursorLeftAndUp() {
        controller.moveBy(-100f, -200f)
        val pos = controller.position
        // Initial position is center (540, 1200), + (-100, -200) = (440, 1000)
        assertEquals(Point(440f, 1000f), pos)
    }

    @Test
    fun moveBy_clampsToRightAndBottomBounds() {
        controller.moveBy(1000f, 2000f)
        val pos = controller.position
        // Center (540, 1200) + (1000, 2000) = (1540, 3200) → clamped to (1080, 2400)
        assertEquals(Point(1080f, 2400f), pos)
    }

    @Test
    fun moveBy_clampsToLeftAndTopBounds() {
        controller.moveBy(-1000f, -2000f)
        val pos = controller.position
        // Center (540, 1200) + (-1000, -2000) = (-460, -800) → clamped to (0, 0)
        assertEquals(Point(0f, 0f), pos)
    }

    @Test
    fun moveBy_sequentialMoves_accumulatePosition() {
        controller.moveBy(100f, 50f)
        controller.moveBy(200f, 100f)
        val pos = controller.position
        // Center (540, 1200) + (100+200, 50+100) = (840, 1350)
        assertEquals(Point(840f, 1350f), pos)
    }

    @Test
    fun position_afterMoveBy_thenNoFurtherMove_remainsAtLastPosition() {
        controller.moveBy(300f, 400f)
        val posAfterMove = controller.position
        // Position should not change without further moveBy call
        assertEquals(posAfterMove, controller.position)
    }

    @Test
    fun position_initialState_isCenterOfScreen() {
        val pos = controller.position
        assertEquals(Point(bounds.centerX, bounds.centerY), pos)
    }

    // --- AccelerationCurve integration ---

    @Test
    fun moveBy_withAcceleration_fastSwipe_movesMoreThanSlowSwipe() {
        val curve = AccelerationCurve()
        val accelerated = CursorController(bounds, curve)

        // Slow swipe: 10px at 0.1 px/ms
        val slow = CursorController(bounds, curve)
        slow.moveBy(10f, 10f, speed = 0.1f)

        // Fast swipe: 10px at 1.0 px/ms
        val fast = CursorController(bounds, curve)
        fast.moveBy(10f, 10f, speed = 1.0f)

        // Fast swipe should have moved cursor further from center
        val slowDistFromCenter = kotlin.math.hypot(
            (slow.position.x - bounds.centerX).toDouble(),
            (slow.position.y - bounds.centerY).toDouble(),
        )
        val fastDistFromCenter = kotlin.math.hypot(
            (fast.position.x - bounds.centerX).toDouble(),
            (fast.position.y - bounds.centerY).toDouble(),
        )
        assertTrue(
            "Fast swipe ($fastDistFromCenter) should move cursor further than slow ($slowDistFromCenter)",
            fastDistFromCenter > slowDistFromCenter,
        )
    }

    @Test
    fun moveBy_withAcceleration_zeroSpeed_fallsBackToLinear() {
        val curve = AccelerationCurve()
        val ctrl = CursorController(bounds, curve)

        // Zero speed → linear 1:1 (same as old behavior)
        ctrl.moveBy(100f, 200f, speed = 0f)
        val pos = ctrl.position
        // Center (540, 1200) + (100, 200) = (640, 1400)
        assertEquals(Point(640f, 1400f), pos)
    }

    @Test
    fun moveBy_withoutSpeed_overloadUsesDefaultSpeed() {
        val curve = AccelerationCurve()
        val ctrl = CursorController(bounds, curve)

        // moveBy(dx, dy) without speed should use some default speed
        // This is the backward-compatible overload
        ctrl.moveBy(100f, 200f)
        val pos = ctrl.position

        // Position should have moved (not stay at center)
        assertTrue("Cursor should have moved from center", pos.x != bounds.centerX || pos.y != bounds.centerY)
    }

    @Test
    fun changingSensitivity_doesNotMoveCursor() {
        val curve = AccelerationCurve(sensitivity = 1.0f)
        val ctrl = CursorController(bounds, curve)

        // Move cursor to some position
        ctrl.moveBy(100f, 50f, speed = 0.5f)
        val posBeforeSensitivityChange = ctrl.position

        // Change sensitivity (by creating new curve and updating controller)
        val newCurve = curve.withSensitivity(2.0f)
        ctrl.updateCurve(newCurve)

        // Cursor position should NOT have changed
        assertEquals(posBeforeSensitivityChange, ctrl.position)
    }

    @Test
    fun higherSensitivity_movesCursorMore() {
        val lowCurve = AccelerationCurve(sensitivity = 0.5f)
        val highCurve = AccelerationCurve(sensitivity = 2.0f)

        val lowCtrl = CursorController(bounds, lowCurve)
        val highCtrl = CursorController(bounds, highCurve)

        lowCtrl.moveBy(10f, 10f, speed = 0.5f)
        highCtrl.moveBy(10f, 10f, speed = 0.5f)

        // Higher sensitivity should move cursor further
        val lowDist = kotlin.math.hypot(
            (lowCtrl.position.x - bounds.centerX).toDouble(),
            (lowCtrl.position.y - bounds.centerY).toDouble(),
        )
        val highDist = kotlin.math.hypot(
            (highCtrl.position.x - bounds.centerX).toDouble(),
            (highCtrl.position.y - bounds.centerY).toDouble(),
        )
        assertTrue(
            "High sensitivity ($highDist) should move more than low ($lowDist)",
            highDist > lowDist,
        )
    }
}
