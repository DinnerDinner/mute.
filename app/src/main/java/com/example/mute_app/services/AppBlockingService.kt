package com.example.mute_app.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class AppBlockingService : AccessibilityService() {

    private val prefs by lazy { getSharedPreferences("break_app_prefs", Context.MODE_PRIVATE) }

    companion object {
        private const val TAG = "AppBlockingService"
        const val ACTION_SHOW_OVERLAY = "com.example.mute_app.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.mute_app.HIDE_OVERLAY"
        const val EXTRA_BLOCKED_APP_NAME = "blocked_app_name"
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            Log.d(TAG, "App switched to: $packageName")

            if (packageName != null && packageName != "com.example.mute_app") {
                checkAndBlockApp(packageName)
            }
        }
    }

    private fun checkAndBlockApp(packageName: String) {
        // Check if session is active
        val isSessionActive = prefs.getBoolean("session_active", false)
        if (!isSessionActive) return

        // Get blocked apps list
        val blockedApps = prefs.getStringSet("selected_apps_for_blocking", emptySet()) ?: emptySet()

        if (blockedApps.contains(packageName)) {
            Log.d(TAG, "Blocking app: $packageName")

            // Get app name
            val appName = try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                packageName
            }

            // Show blocking overlay
            val intent = Intent(ACTION_SHOW_OVERLAY).apply {
                putExtra(EXTRA_BLOCKED_APP_NAME, appName)
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
            }
            sendBroadcast(intent)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
    }
}