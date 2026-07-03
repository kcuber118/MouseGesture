package com.example.mousegesture.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.mousegesture.service.overlay.AccessibilityGestureDispatcher
import com.example.mousegesture.service.overlay.OverlayManager

/**
 * AccessibilityService for Mouse Gesture.
 *
 * When connected, creates a full-screen overlay with a Touchpad and Cursor.
 * Touch events on the Touchpad drive the cursor; taps inject gestures via dispatchGesture.
 */
class MouseGestureAccessibilityService : AccessibilityService() {

    companion object {
        /**
         * Simple static flag to check if service is running.
         * For production, prefer [isAccessibilityServiceEnabled] in MainActivity
         * which queries the system. This flag is a fast-path for in-process checks.
         */
        var isRunning: Boolean = false
            private set
    }

    private var overlayManager: OverlayManager? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not needed for this service — we don't inspect the UI tree
    }

    override fun onInterrupt() {
        // Called when the system wants to interrupt the service — no action needed
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true

        val gestureDispatcher = AccessibilityGestureDispatcher(this)
        overlayManager = OverlayManager(this, gestureDispatcher)
        overlayManager?.setup()
    }

    override fun onDestroy() {
        overlayManager?.teardown()
        overlayManager = null
        isRunning = false
        super.onDestroy()
    }
}
