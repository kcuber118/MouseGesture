package com.example.mousegesture.service.overlay

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.Handler

/**
 * Real implementation that delegates to [AccessibilityService.dispatchGesture].
 */
class AccessibilityGestureDispatcher(
    private val service: AccessibilityService,
) : GestureDispatcher {
    override fun dispatchGesture(
        gesture: GestureDescription,
        callback: AccessibilityService.GestureResultCallback?,
        handler: Handler?,
    ): Boolean = service.dispatchGesture(gesture, callback, handler)
}
