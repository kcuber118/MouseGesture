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

        /**
         * Current overlay visibility. Per ADR-0002:
         * - null: service not connected (no overlay to show/hide)
         * - true: overlay is visible
         * - false: overlay is hidden (service still running)
         */
        @Volatile
        var isOverlayVisible: Boolean? = null
            private set

        /**
         * Show the overlay from outside the service (e.g. MainActivity "Bật overlay" button).
         * No-op if the service isn't connected.
         * Updates [isOverlayVisible] after the call.
         */
        fun showOverlay() {
            instance?.overlayManager?.showOverlay()
            isOverlayVisible = instance?.overlayManager?.isOverlayVisible
        }

        /**
         * Hide the overlay from outside the service.
         * Updates [isOverlayVisible] after the call.
         */
        fun hideOverlay() {
            instance?.overlayManager?.hideOverlay()
            isOverlayVisible = instance?.overlayManager?.isOverlayVisible
        }

        private var instance: MouseGestureAccessibilityService? = null
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
        instance = this

        val gestureDispatcher = AccessibilityGestureDispatcher(this)
        overlayManager = OverlayManager(this, gestureDispatcher).also { manager ->
            manager.setup()
        }
        isOverlayVisible = overlayManager?.isOverlayVisible
    }

    override fun onDestroy() {
        overlayManager?.teardown()
        overlayManager = null
        isRunning = false
        isOverlayVisible = null
        instance = null
        super.onDestroy()
    }
}
