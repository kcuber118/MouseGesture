package com.example.mousegesture.domain.touchpad

import com.example.mousegesture.domain.model.ScreenBounds
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TouchpadStateTest {

    private lateinit var screenBounds: ScreenBounds

    @Before
    fun setUp() {
        screenBounds = ScreenBounds(width = 1080f, height = 2400f)
    }

    // --- Tracer bullet: existence and initial state ---

    @Test
    fun initialState_isActiveMode() {
        val state = TouchpadState(screenBounds)
        assertEquals(TouchpadMode.ACTIVE, state.mode)
    }

    @Test
    fun initialState_rectIsDefaultSizeAtBottomRight() {
        val state = TouchpadState(screenBounds)
        // Default: 200×200dp touchpad at bottom-right corner
        // In pixels (density=1 for domain test): right edge = screen width, bottom edge = screen height
        val rect = state.rect
        assertEquals(880f, rect.left, 0.01f)   // 1080 - 200
        assertEquals(2200f, rect.top, 0.01f)   // 2400 - 200
        assertEquals(1080f, rect.right, 0.01f)
        assertEquals(2400f, rect.bottom, 0.01f)
    }

    @Test
    fun toggleMode_switchesActiveToEdit() {
        val state = TouchpadState(screenBounds)
        state.toggleMode()
        assertEquals(TouchpadMode.EDIT, state.mode)
    }

    @Test
    fun toggleMode_switchesEditBackToActive() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit
        state.toggleMode() // Edit → Active
        assertEquals(TouchpadMode.ACTIVE, state.mode)
    }

    // --- moveBy with boundary clamping (only in EDIT mode) ---

    @Test
    fun moveBy_inEditMode_movesRectByDelta() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Default rect: (880, 2200, 1080, 2400) — already at bottom-right
        // Move left/up so there's room to move freely
        state.moveBy(-200f, -200f)
        // Raw: (680, 2000, 880, 2200) — all within bounds
        var rect = state.rect
        assertEquals(680f, rect.left, 0.01f)
        assertEquals(2000f, rect.top, 0.01f)
        assertEquals(880f, rect.right, 0.01f)
        assertEquals(2200f, rect.bottom, 0.01f)

        // Now move freely
        state.moveBy(50f, 30f)
        rect = state.rect
        assertEquals(730f, rect.left, 0.01f)
        assertEquals(2030f, rect.top, 0.01f)
        assertEquals(930f, rect.right, 0.01f)
        assertEquals(2230f, rect.bottom, 0.01f)
    }

    @Test
    fun moveBy_inActiveMode_doesNotMoveRect() {
        val state = TouchpadState(screenBounds)
        // Default is ACTIVE mode
        val rectBefore = state.rect
        state.moveBy(50f, 30f)
        assertEquals(rectBefore, state.rect)
    }

    @Test
    fun moveBy_clampsToRightBoundary() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move right beyond screen: right edge should not exceed screen width
        state.moveBy(500f, 0f)
        val rect = state.rect
        assertEquals(screenBounds.width, rect.right, 0.01f)
    }

    @Test
    fun moveBy_clampsToBottomBoundary() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move down beyond screen: bottom edge should not exceed screen height
        state.moveBy(0f, 500f)
        val rect = state.rect
        assertEquals(screenBounds.height, rect.bottom, 0.01f)
    }

    @Test
    fun moveBy_clampsToLeftBoundary() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move left beyond screen: left edge should not go below 0
        state.moveBy(-2000f, 0f)
        val rect = state.rect
        assertEquals(0f, rect.left, 0.01f)
    }

    @Test
    fun moveBy_clampsToTopBoundary() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move up beyond screen: top edge should not go below 0
        state.moveBy(0f, -5000f)
        val rect = state.rect
        assertEquals(0f, rect.top, 0.01f)
    }

    @Test
    fun moveBy_sequentialMoves_accumulate() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        state.moveBy(50f, 30f)
        state.moveBy(30f, 20f)
        // Total delta: (80, 50) from default (880, 2200, 1080, 2400)
        // right=1080+80=1160→clamped to 1080, left=880+80=960 (clamped so right-200=880... wait)
        // Actually: move shifts the entire rect, then clamps
        val rect = state.rect
        // After first move: (930, 2230, 1080, 2400) — right and bottom clamped
        // After second move: would be (960, 2250, 1110, 2430) → clamped: left shifts back
        // When right > screenWidth, we clamp right to screenWidth and left = screenWidth - width
        // width = 200, so left = 1080 - 200 = 880 when clamped to right
        // But left would be 930+30=960, right=1080+30=1110→1080, left=960 if 960+200=1160>1080
        // Actually: right=1080, left=1080-200=880? No, we need to preserve width.
        // Move shifts all 4 edges by delta, then clamps the whole rect to screen.
        // Let's think: moveBy(80, 50) from (880, 2200, 1080, 2400)
        // Raw: (960, 2250, 1160, 2450)
        // Clamp: shift left so right ≤ 1080: right=1080, left=880
        // Shift top so bottom ≤ 2400: bottom=2400, top=2200
        // So rect = (880, 2200, 1080, 2400) — same as start because it was already at bottom-right!
        assertEquals(880f, rect.left, 0.01f)
        assertEquals(2200f, rect.top, 0.01f)
        assertEquals(1080f, rect.right, 0.01f)
        assertEquals(2400f, rect.bottom, 0.01f)
    }

    @Test
    fun moveBy_negativeDelta_fromTopLeft_staysAtZero() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // First move to top-left
        state.moveBy(-880f, -2200f)
        // Expected: (0, 0, 200, 200)
        assertEquals(0f, state.rect.left, 0.01f)
        assertEquals(0f, state.rect.top, 0.01f)

        // Try to move further left/up — should stay at 0
        state.moveBy(-100f, -100f)
        assertEquals(0f, state.rect.left, 0.01f)
        assertEquals(0f, state.rect.top, 0.01f)
        assertEquals(200f, state.rect.right, 0.01f)
        assertEquals(200f, state.rect.bottom, 0.01f)
    }

    // --- resizeBy with min/max size constraints (only in EDIT mode) ---

    @Test
    fun resizeBy_inEditMode_expandsRect() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move to a position with room to expand
        state.moveBy(-400f, -400f)
        // Rect: (480, 1800, 680, 2000), size 200×200

        state.resizeBy(100f, 50f)
        val rect = state.rect
        // Expands from top-left anchor (left/top stay, right/bottom grow)
        assertEquals(480f, rect.left, 0.01f)
        assertEquals(1800f, rect.top, 0.01f)
        assertEquals(780f, rect.right, 0.01f)   // 680 + 100
        assertEquals(2050f, rect.bottom, 0.01f)  // 2000 + 50
    }

    @Test
    fun resizeBy_inActiveMode_doesNotResizeRect() {
        val state = TouchpadState(screenBounds)
        // Default is ACTIVE mode
        val rectBefore = state.rect
        state.resizeBy(100f, 100f)
        assertEquals(rectBefore, state.rect)
    }

    @Test
    fun resizeBy_clampsToMaxSize() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move to top-left where there's plenty of room
        state.moveBy(-880f, -2200f)
        // Rect: (0, 0, 200, 200)
        state.resizeBy(1000f, 1000f)
        val rect = state.rect
        // Max size is 600, and screen is 1080×2400, so plenty of room
        assertEquals(TouchpadState.MAX_SIZE_PX, rect.width, 0.01f)
        assertEquals(TouchpadState.MAX_SIZE_PX, rect.height, 0.01f)
    }

    @Test
    fun resizeBy_clampsToMinSize() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Try to resize smaller than MIN_SIZE_PX (100)
        state.resizeBy(-500f, -500f)
        val rect = state.rect
        assertEquals(TouchpadState.MIN_SIZE_PX, rect.width, 0.01f)
        assertEquals(TouchpadState.MIN_SIZE_PX, rect.height, 0.01f)
    }

    @Test
    fun resizeBy_clampsToScreenBoundary() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Default rect at bottom-right: (880, 2200, 1080, 2400)
        // Expanding right/bottom would go past screen — clamped
        state.resizeBy(200f, 200f)
        val rect = state.rect
        assertEquals(880f, rect.left, 0.01f)
        assertEquals(2200f, rect.top, 0.01f)
        assertEquals(screenBounds.width, rect.right, 0.01f)  // 1080
        assertEquals(screenBounds.height, rect.bottom, 0.01f) // 2400
        // Width/height didn't actually grow because already at boundary
    }

    @Test
    fun resizeBy_negativeShrink_fromTopLeftAnchor_shrinksRightAndBottom() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Default rect at bottom-right: (880, 2200, 1080, 2400), size 200×200
        // Shrink by 50 in each dimension
        state.resizeBy(-50f, -50f)
        val rect = state.rect
        // left/top anchored, right/bottom shrink inward
        assertEquals(880f, rect.left, 0.01f)
        assertEquals(2200f, rect.top, 0.01f)
        assertEquals(1030f, rect.right, 0.01f)   // 1080 - 50
        assertEquals(2350f, rect.bottom, 0.01f)  // 2400 - 50
        assertEquals(150f, rect.width, 0.01f)
        assertEquals(150f, rect.height, 0.01f)
    }

    // --- Mode-gated behavior: move/resize blocked in ACTIVE, allowed in EDIT ---

    @Test
    fun moveBy_and_resizeBy_bothBlockedInActiveMode() {
        val state = TouchpadState(screenBounds)
        // Active mode by default
        assertEquals(TouchpadMode.ACTIVE, state.mode)

        val originalRect = state.rect.copy()
        state.moveBy(100f, 100f)
        state.resizeBy(100f, 100f)
        // Neither should have any effect
        assertEquals(originalRect, state.rect)
    }

    @Test
    fun moveBy_and_resizeBy_bothAllowedInEditMode() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit
        assertEquals(TouchpadMode.EDIT, state.mode)

        // Move to top-left for room
        state.moveBy(-880f, -2200f)
        val rectAfterMove = state.rect
        assertEquals(0f, rectAfterMove.left, 0.01f)
        assertEquals(0f, rectAfterMove.top, 0.01f)

        // Resize
        state.resizeBy(100f, 100f)
        val rectAfterResize = state.rect
        assertEquals(300f, rectAfterResize.width, 0.01f)
        assertEquals(300f, rectAfterResize.height, 0.01f)
    }

    @Test
    fun switchingBackToActive_freezesRectAtCurrentPosition() {
        val state = TouchpadState(screenBounds)
        state.toggleMode() // Active → Edit

        // Move to a specific position
        state.moveBy(-400f, -400f)
        val movedRect = state.rect.copy()

        // Switch back to Active
        state.toggleMode() // Edit → Active

        // Attempting move/resize should be no-op
        state.moveBy(500f, 500f)
        state.resizeBy(200f, 200f)
        assertEquals(movedRect, state.rect)
    }

    @Test
    fun toggleMode_multipleTimes_cyclesBetweenActiveAndEdit() {
        val state = TouchpadState(screenBounds)
        assertEquals(TouchpadMode.ACTIVE, state.mode)

        state.toggleMode()
        assertEquals(TouchpadMode.EDIT, state.mode)

        state.toggleMode()
        assertEquals(TouchpadMode.ACTIVE, state.mode)

        state.toggleMode()
        assertEquals(TouchpadMode.EDIT, state.mode)
    }
}
