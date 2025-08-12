package com.example.mute_app.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppBlockingService : AccessibilityService() {

    private val prefs by lazy { getSharedPreferences("break_app_prefs", Context.MODE_PRIVATE) }
    private var isServiceConnected = false
    private var heartbeatJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "AppBlockingService"
        const val ACTION_SHOW_OVERLAY = "com.example.mute_app.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.mute_app.HIDE_OVERLAY"
        const val EXTRA_BLOCKED_APP_NAME = "blocked_app_name"
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"

        // Add a static method to check if service is connected
        @Volatile
        private var serviceConnected = false

        fun isServiceConnected(): Boolean = serviceConnected
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        serviceConnected = true

        // Store connection state in SharedPreferences for other components to check
        updateConnectionState(true)

        // Start heartbeat to continuously update connection state
        startHeartbeat()

        Log.d(TAG, "Accessibility service connected successfully")
    }

    private fun updateConnectionState(connected: Boolean) {
        prefs.edit()
            .putBoolean("accessibility_service_connected", connected)
            .putLong("accessibility_service_connected_time", System.currentTimeMillis())
            .apply()
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isServiceConnected) {
                // Update connection state every 10 seconds to show service is alive
                updateConnectionState(true)
                delay(10000) // 10 seconds
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        serviceConnected = false

        // Stop heartbeat
        heartbeatJob?.cancel()

        // Update connection state
        updateConnectionState(false)

        Log.d(TAG, "Accessibility service destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Only process events if service is properly connected
        if (!isServiceConnected) {
            Log.w(TAG, "Service not connected, ignoring accessibility event")
            return
        }

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            Log.d(TAG, "App switched to: $packageName")

            if (packageName != null && packageName != "com.example.mute_app") {
                checkAndBlockApp(packageName)
            }
        }
    }

    private fun checkAndBlockApp(packageName: String) {
        // Double-check service connection before processing
        if (!isServiceConnected) {
            Log.w(TAG, "Service disconnected during app check")
            return
        }

        // Check if session is active
        val isSessionActive = prefs.getBoolean("session_active", false)
        Log.d(TAG, "Session active: $isSessionActive")

        if (!isSessionActive) return

        // Get blocked apps list
        val blockedApps = prefs.getStringSet("selected_apps_for_blocking", emptySet()) ?: emptySet()
        Log.d(TAG, "Blocked apps list: $blockedApps")

        if (blockedApps.contains(packageName)) {
            Log.d(TAG, "Blocking app: $packageName")

            // Get app name (best-effort)
            val appName = try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                Log.e(TAG, "Could not get app name for $packageName", e)
                packageName
            }

            // Show blocking overlay using an explicit broadcast targeting this app package
            val intent = Intent(ACTION_SHOW_OVERLAY).apply {
                putExtra(EXTRA_BLOCKED_APP_NAME, appName)
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
                // Explicitly target our app to avoid background broadcast restrictions
                setPackage(this@AppBlockingService.packageName)
            }
            Log.d(TAG, "Sending SHOW_OVERLAY broadcast for: $appName ($packageName) -> targetPackage=${this.packageName}")
            try {
                sendBroadcast(intent)
                Log.d(TAG, "Broadcast sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SHOW_OVERLAY broadcast", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        isServiceConnected = false
        serviceConnected = false

        // Stop heartbeat
        heartbeatJob?.cancel()

        // Update connection state
        updateConnectionState(false)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Service unbound")
        isServiceConnected = false
        serviceConnected = false

        // Stop heartbeat
        heartbeatJob?.cancel()

        // Update connection state
        updateConnectionState(false)

        return super.onUnbind(intent)
    }
}