package com.example.mute_app.services

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URL

class AppBlockingService : AccessibilityService() {

    private val prefs by lazy { getSharedPreferences("break_app_prefs", Context.MODE_PRIVATE) }
    private val websitePrefs by lazy { getSharedPreferences("break_website_prefs", Context.MODE_PRIVATE) }
    private var isServiceConnected = false
    private var heartbeatJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val updateReceiver = UpdateReceiver()

    // Cache for blocked lists to avoid frequent SharedPreferences reads
    private var blockedApps: Set<String> = emptySet()
    private var blockedWebsites: Set<String> = emptySet()

    companion object {
        private const val TAG = "AppBlockingService"
        const val ACTION_SHOW_OVERLAY = "com.example.mute_app.SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "com.example.mute_app.HIDE_OVERLAY"
        const val EXTRA_BLOCKED_APP_NAME = "blocked_app_name"
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        const val EXTRA_BLOCKED_URL = "blocked_url"
        const val EXTRA_IS_WEBSITE = "is_website"

        // Add a static method to check if service is connected
        @Volatile
        private var serviceConnected = false

        fun isServiceConnected(): Boolean = serviceConnected

        // Common browser packages
        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.microsoft.emmx",
            "com.opera.browser",
            "com.brave.browser",
            "com.samsung.android.sbrowser",
            "com.UCMobile.intl",
            "org.mozilla.fenix",
            "com.duckduckgo.mobile.android",
            "com.kiwibrowser.browser",
            "com.yandex.browser",
            "com.vivaldi.browser"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceConnected = true
        serviceConnected = true

        // Store connection state in SharedPreferences for other components to check
        updateConnectionState(true)

        // Load blocked lists
        loadBlockedLists()

        // Register receiver for updates
        registerUpdateReceiver()

        // Start heartbeat to continuously update connection state
        startHeartbeat()

        Log.d(TAG, "Accessibility service connected successfully")
    }

    private fun registerUpdateReceiver() {
        val filter = IntentFilter().apply {
            addAction("com.example.mute_app.UPDATE_BLOCKED_LISTS")
            addAction("com.example.mute_app.CLEAR_BLOCKED_LISTS")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(updateReceiver, filter)
        }
    }

    private fun loadBlockedLists() {
        blockedApps = prefs.getStringSet("selected_apps_for_blocking", emptySet()) ?: emptySet()
        blockedWebsites = websitePrefs.getStringSet("selected_websites_for_blocking", emptySet()) ?: emptySet()

        Log.d(TAG, "Loaded blocked apps: ${blockedApps.size} items - $blockedApps")
        Log.d(TAG, "Loaded blocked websites: ${blockedWebsites.size} items - $blockedWebsites")
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

        // Unregister receiver
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister update receiver", e)
        }

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

        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString()
                Log.d(TAG, "App switched to: $packageName")

                if (packageName != null && packageName != "com.example.mute_app") {
                    // Check if it's a browser - if so, check for website blocking
                    if (BROWSER_PACKAGES.contains(packageName)) {
                        Log.d(TAG, "Browser detected: $packageName")
                        checkAndBlockWebsite(event, packageName)
                    } else {
                        // Regular app blocking
                        checkAndBlockApp(packageName)
                    }
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Also check content changes in browsers for URL updates
                val packageName = event.packageName?.toString()
                if (packageName != null && BROWSER_PACKAGES.contains(packageName)) {
                    checkAndBlockWebsite(event, packageName)
                }
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

            // Show blocking overlay
            showAppBlockingOverlay(appName, packageName)
        }
    }

    private fun checkAndBlockWebsite(event: AccessibilityEvent, packageName: String) {
        // Double-check service connection before processing
        if (!isServiceConnected) {
            Log.w(TAG, "Service disconnected during website check")
            return
        }

        // Check if session is active
        val isSessionActive = prefs.getBoolean("session_active", false)
        if (!isSessionActive) return

        val blockedWebsites = websitePrefs.getStringSet("selected_websites_for_blocking", emptySet()) ?: emptySet()
        Log.d(TAG, "Blocked websites list: $blockedWebsites")

        // Skip if no websites to block
        if (blockedWebsites.isEmpty()) return

        // Extract URL from the accessibility event
        val url = extractUrlFromEvent(event)
        if (url.isNullOrBlank()) {
            return
        }

        Log.d(TAG, "Checking URL: $url")

        // Check if any blocked website domain is contained in the current URL
        for (blockedSite in blockedWebsites) {
            if (isUrlBlocked(url, blockedSite)) {
                Log.d(TAG, "Blocking website: $blockedSite in URL: $url")
                showWebsiteBlockingOverlay(blockedSite, url, packageName)
                return
            }
        }
    }

    private fun extractUrlFromEvent(event: AccessibilityEvent): String? {
        try {
            // Try to get URL from the source node
            val source = event.source
            if (source != null) {
                val url = findUrlInNode(source)
                source.recycle()
                return url
            }

            // Fallback: try to get URL from event text
            val eventText = event.text?.joinToString(" ") ?: ""
            return extractUrlFromText(eventText)
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting URL from event", e)
            return null
        }
    }

    private fun findUrlInNode(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null

        try {
            // Check if this node contains URL-like content
            val nodeText = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""

            // Look for URL patterns in text and content description
            var url = extractUrlFromText(nodeText)
            if (url != null) return url

            url = extractUrlFromText(contentDesc)
            if (url != null) return url

            // Recursively check child nodes
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    url = findUrlInNode(child)
                    child.recycle()
                    if (url != null) return url
                }
            }

            return null
        } catch (e: Exception) {
            Log.w(TAG, "Error finding URL in node", e)
            return null
        }
    }

    private fun extractUrlFromText(text: String): String? {
        if (text.isBlank()) return null

        try {
            // Look for URLs starting with http:// or https://
            val urlPattern = Regex("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+")
            val match = urlPattern.find(text)
            if (match != null) {
                return match.value
            }

            // Look for domain patterns (e.g., google.com, facebook.com)
            val domainPattern = Regex("(?:^|\\s)([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(?:/[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?")
            val domainMatch = domainPattern.find(text)
            if (domainMatch != null) {
                return "https://" + domainMatch.value.trim()
            }

            return null
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting URL from text: $text", e)
            return null
        }
    }

    private fun isUrlBlocked(currentUrl: String, blockedSite: String): Boolean {
        try {
            val cleanCurrentUrl = currentUrl.lowercase().trim()
            val cleanBlockedSite = blockedSite.lowercase().trim()

            // Remove protocol from current URL for comparison
            val urlWithoutProtocol = cleanCurrentUrl
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")

            val blockedSiteWithoutWww = cleanBlockedSite.removePrefix("www.")

            // Check if the blocked site domain is contained in the current URL
            return urlWithoutProtocol.contains(blockedSiteWithoutWww) ||
                    extractDomainFromUrl(cleanCurrentUrl)?.contains(blockedSiteWithoutWww) == true
        } catch (e: Exception) {
            Log.w(TAG, "Error checking if URL is blocked: $currentUrl vs $blockedSite", e)
            return false
        }
    }

    private fun extractDomainFromUrl(url: String): String? {
        return try {
            val urlObj = URL(url)
            urlObj.host?.lowercase()
        } catch (e: Exception) {
            // Fallback: extract domain using regex
            try {
                val cleanUrl = url.removePrefix("http://").removePrefix("https://")
                val domain = cleanUrl.substringBefore("/").substringBefore("?").substringBefore("#")
                domain.lowercase()
            } catch (e2: Exception) {
                Log.w(TAG, "Could not extract domain from URL: $url", e2)
                null
            }
        }
    }

    private fun showAppBlockingOverlay(appName: String, packageName: String) {
        val intent = Intent(ACTION_SHOW_OVERLAY).apply {
            putExtra(EXTRA_BLOCKED_APP_NAME, appName)
            putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(EXTRA_IS_WEBSITE, false)
            // Explicitly target our app to avoid background broadcast restrictions
            setPackage(this@AppBlockingService.packageName)
        }
        Log.d(TAG, "Sending SHOW_OVERLAY broadcast for app: $appName ($packageName)")
        try {
            sendBroadcast(intent)
            Log.d(TAG, "App blocking broadcast sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SHOW_OVERLAY broadcast for app", e)
        }
    }

    private fun showWebsiteBlockingOverlay(blockedSite: String, currentUrl: String, browserPackage: String) {
        val intent = Intent(ACTION_SHOW_OVERLAY).apply {
            putExtra(EXTRA_BLOCKED_APP_NAME, blockedSite)
            putExtra(EXTRA_BLOCKED_PACKAGE, browserPackage)
            putExtra(EXTRA_BLOCKED_URL, currentUrl)
            putExtra(EXTRA_IS_WEBSITE, true)
            // Explicitly target our app to avoid background broadcast restrictions
            setPackage(this@AppBlockingService.packageName)
        }
        Log.d(TAG, "Sending SHOW_OVERLAY broadcast for website: $blockedSite (URL: $currentUrl)")
        try {
            sendBroadcast(intent)
            Log.d(TAG, "Website blocking broadcast sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SHOW_OVERLAY broadcast for website", e)
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

    private inner class UpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.mute_app.UPDATE_BLOCKED_LISTS" -> {
                    Log.d(TAG, "Received UPDATE_BLOCKED_LISTS broadcast")

                    // Get updated lists from intent extras
                    val appsList = intent.getStringArrayListExtra("blocked_apps")
                    val websitesList = intent.getStringArrayListExtra("blocked_websites")

                    if (appsList != null) {
                        blockedApps = appsList.toSet()
                        Log.d(TAG, "Updated blocked apps: ${blockedApps.size} items")
                    }

                    if (websitesList != null) {
                        blockedWebsites = websitesList.toSet()
                        Log.d(TAG, "Updated blocked websites: ${blockedWebsites.size} items")
                    }

                    // Also reload from preferences as backup
//                    loadBlockedLists()
                }
                "com.example.mute_app.CLEAR_BLOCKED_LISTS" -> {
                    Log.d(TAG, "Received CLEAR_BLOCKED_LISTS broadcast")
                    blockedApps = emptySet()
                    blockedWebsites = emptySet()
                }
            }
        }
    }
}


