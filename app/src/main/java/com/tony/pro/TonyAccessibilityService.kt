package com.tony.pro
import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class TonyAccessibilityService : AccessibilityService() {
    companion object { var instance: TonyAccessibilityService? = null }
    override fun onServiceConnected() { super.onServiceConnected(); instance = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); instance = null }

    fun clickByText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (node in root.findAccessibilityNodeInfosByText(text)) {
            if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            var p = node.parent
            while (p != null) {
                if (p.isClickable) return p.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                p = p.parent
            }
        }
        return false
    }

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focus = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text) }
        return focus.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }
}
