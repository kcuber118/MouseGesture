package com.example.mousegesture.service.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import com.example.mousegesture.domain.cursor.CursorController
import com.example.mousegesture.domain.gesture.GestureFactory
import com.example.mousegesture.domain.model.ScreenBounds
import com.example.mousegesture.domain.touchpad.TouchpadMode
import com.example.mousegesture.domain.touchpad.TouchpadState

/**
 * Manages the overlay lifecycle: adds/removes views via WindowManager,
 * wires touchpad callbacks to cursor movement and gesture injection.
 */
class OverlayManager(
    private val service: AccessibilityService,
    private val gestureDispatcher: GestureDispatcher,
) {
    private var overlayView: OverlayRootView? = null
    private var touchpadView: TouchpadView? = null
    private var cursorController: CursorController? = null
    private var touchpadState: TouchpadState? = null
    private val gestureFactory = GestureFactory()

    /**
     * Set up the overlay: create views, add to WindowManager, wire callbacks.
     */
    fun setup() {
        val wm = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Get screen dimensions using current API (avoid deprecated defaultDisplay.getMetrics)
        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val inset = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(android.view.WindowInsets.Type.systemBars())
            ScreenBounds(
                width = (windowMetrics.bounds.width() - inset.left - inset.right).toFloat(),
                height = (windowMetrics.bounds.height() - inset.top - inset.bottom).toFloat(),
            )
        } else {
            @Suppress("DEPRECATION")
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(metrics)
            ScreenBounds(metrics.widthPixels.toFloat(), metrics.heightPixels.toFloat())
        }

        // Create domain objects
        cursorController = CursorController(bounds)
        touchpadState = TouchpadState(bounds)

        // Create touchpad view
        val touchpad = TouchpadView(service)
        touchpadView = touchpad

        // Create root overlay view
        val root = OverlayRootView(service, cursorController!!, touchpad, touchpadState!!)
        overlayView = root

        // Wire touchpad callbacks — only active in ACTIVE mode
        touchpad.onDragCallback = { dx, dy, speed ->
            if (touchpadState?.mode == TouchpadMode.ACTIVE) {
                cursorController?.moveBy(dx, dy, speed)
                root.updateCursor()
            }
        }
        touchpad.onTapCallback = {
            if (touchpadState?.mode == TouchpadMode.ACTIVE) {
                onTouchpadTap()
            }
        }
        touchpad.onLongPressCallback = {
            if (touchpadState?.mode == TouchpadMode.ACTIVE) {
                onTouchpadLongPress()
            }
        }

        // Add overlay to WindowManager
        val params = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP
        }
        wm.addView(root, params)
    }

    /**
     * Remove overlay from WindowManager.
     */
    fun teardown() {
        val wm = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView?.let { wm.removeView(it) }
        overlayView = null
        touchpadView = null
        cursorController = null
        touchpadState = null
    }

    /**
     * Called when user taps in the touchpad (Active mode).
     * Creates a tap gesture at the cursor position and dispatches it.
     */
    private fun onTouchpadTap() {
        val cursor = cursorController ?: return
        val tapGesture = gestureFactory.createTapAt(cursor.position)
        val gestureDescription = buildTapGesture(tapGesture.x, tapGesture.y, tapGesture.durationMs)
        gestureDispatcher.dispatchGesture(gestureDescription, null, null)
    }

    /**
     * Called when user long-presses in the touchpad (Active mode).
     * Creates a long-press gesture at the cursor position and dispatches it.
     */
    private fun onTouchpadLongPress() {
        val cursor = cursorController ?: return
        val longPressGesture = gestureFactory.createLongPressAt(cursor.position)
        val gestureDescription = buildLongPressGesture(
            longPressGesture.x,
            longPressGesture.y,
            longPressGesture.durationMs,
        )
        gestureDispatcher.dispatchGesture(gestureDescription, null, null)
    }

    /**
     * Convert domain tap gesture data to Android GestureDescription.
     * Uses a path with a tiny vertical offset to avoid zero-length stroke rejection on some OEMs.
     */
    private fun buildTapGesture(x: Float, y: Float, durationMs: Long): GestureDescription {
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x, y + 1f) // tiny offset to avoid zero-length rejection
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    /**
     * Convert domain long-press gesture data to Android GestureDescription.
     * Uses a path with a tiny vertical offset to avoid zero-length stroke rejection on some OEMs.
     */
    private fun buildLongPressGesture(x: Float, y: Float, durationMs: Long): GestureDescription {
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x, y + 1f) // tiny offset to avoid zero-length rejection
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        return GestureDescription.Builder().addStroke(stroke).build()
    }
}
