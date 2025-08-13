package com.example.mute_app.viewmodels.explore.`break`

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mute_app.R
import com.example.mute_app.services.AppBlockingService
import com.example.mute_app.services.OverlayService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BreakUiState(
    val isSessionActive: Boolean = false,
    val currentMode: String = "Focus",
    val timeRemaining: Long = 25 * 60 * 1000L,
    val totalTime: Long = 25 * 60 * 1000L,
    val sessionsCompleted: Int = 0,
    val totalFocusTime: Long = 0, // in minutes
    val streakDays: Int = 0,
    val isAppBlockEnabled: Boolean = false,
    val isWebsiteBlockEnabled: Boolean = false,
    val isFocusModeEnabled: Boolean = false,
    val selectedAppsCount: Int = 0,
    val selectedWebsitesCount: Int = 0,
    val blockedApps: Set<String> = emptySet(),
    val blockedWebsites: Set<String> = emptySet(),
    val error: String? = null,
    // Permission states
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val needsPermissionSetup: Boolean = false,
    val permissionMessage: String? = null
)

@HiltViewModel
class BreakViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BreakUiState())
    val uiState: StateFlow<BreakUiState> = _uiState.asStateFlow()
    private var isAppInForeground = true

    private var timerJob: Job? = null
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // SharedPreferences for persistent storage
    private val prefs = application.getSharedPreferences("break_app_prefs", Context.MODE_PRIVATE)
    private val websitePrefs = application.getSharedPreferences("break_website_prefs", Context.MODE_PRIVATE)
    private val selectedAppsKey = "selected_apps_for_blocking"
    private val selectedWebsitesKey = "selected_websites_for_blocking"
    private val sessionStateKey = "session_active"
    private val sessionStartTimeKey = "session_start_time"
    private val sessionDurationKey = "session_duration"

    companion object {
        private const val TAG = "BreakViewModel"
        private const val NOTIFICATION_CHANNEL_ID = "break_app_blocking"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
        loadUserStats()
        loadBlocklistCounts()
        checkPermissions()
        restoreSessionIfActive()
    }

    private fun checkPermissions() {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(application)
        } else {
            true
        }

        val hasAccessibility = isAccessibilityServiceEnabled()

        // Store the current state to compare with previous
        val currentState = _uiState.value
        val permissionStateChanged = currentState.hasOverlayPermission != hasOverlay ||
                currentState.hasAccessibilityPermission != hasAccessibility

        val permissionMessage = when {
            !hasOverlay && !hasAccessibility ->
                "App and website blocking requires Overlay and Accessibility permissions. Please enable both to start blocking."
            !hasOverlay ->
                "Overlay permission is required to show blocking screen. Please enable it in settings."
            !hasAccessibility ->
                "Accessibility permission is required to detect when blocked apps are opened and websites are visited. Please enable our service in Accessibility settings."
            else -> null
        }

        _uiState.value = _uiState.value.copy(
            hasOverlayPermission = hasOverlay,
            hasAccessibilityPermission = hasAccessibility,
            needsPermissionSetup = !hasOverlay || !hasAccessibility,
            permissionMessage = permissionMessage
        )

        // Only log if there's a change to reduce log spam
        if (permissionStateChanged) {
            Log.d(TAG, "Permission state changed - Overlay: $hasOverlay, Accessibility: $hasAccessibility")
        }
    }

    fun refreshPermissions() {
        Log.d(TAG, "Refreshing permissions...")
        viewModelScope.launch {
            delay(500) // Small delay to allow system settings to propagate
            checkPermissions()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            // Get all enabled accessibility services
            val enabledServices = Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            // Check if accessibility is globally enabled
            val accessibilityEnabled = Settings.Secure.getInt(
                application.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1

            Log.d(TAG, "Accessibility globally enabled: $accessibilityEnabled")
            Log.d(TAG, "Enabled services string: '$enabledServices'")

            // If accessibility is not globally enabled, return false immediately
            if (!accessibilityEnabled) {
                Log.d(TAG, "Accessibility not globally enabled")
                return false
            }

            // Check multiple possible service name formats
            val possibleServiceNames = listOf(
                "com.example.mute_app/.services.AppBlockingService",
                "com.example.mute_app/com.example.mute_app.services.AppBlockingService",
                "${application.packageName}/.services.AppBlockingService",
                "${application.packageName}/com.example.mute_app.services.AppBlockingService",
                ".services.AppBlockingService"
            )

            var serviceFound = false
            for (serviceName in possibleServiceNames) {
                if (enabledServices.contains(serviceName)) {
                    Log.d(TAG, "Found accessibility service with name: $serviceName")
                    serviceFound = true
                    break
                }
            }

            // Also check if our service is actually connected and recent
            val serviceConnected = prefs.getBoolean("accessibility_service_connected", false)
            val connectionTime = prefs.getLong("accessibility_service_connected_time", 0)
            val recentConnection = (System.currentTimeMillis() - connectionTime) < 30000 // 30 seconds

            Log.d(TAG, "Service name found in settings: $serviceFound")
            Log.d(TAG, "Service reports connected: $serviceConnected")
            Log.d(TAG, "Recent connection: $recentConnection")

            // Return true if:
            // 1. Accessibility is globally enabled AND
            // 2. Either our service name is found in the settings OR our service reports being connected recently
            val result = accessibilityEnabled && (serviceFound || (serviceConnected && recentConnection))

            Log.d(TAG, "Final accessibility result: $result")
            return result

        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility service", e)
            return false
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(application)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${application.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                application.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open overlay permission settings", e)
                _uiState.value = _uiState.value.copy(
                    error = "Unable to open overlay permission settings. Please enable it manually in system settings."
                )
            }
        }
    }

    fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            application.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open accessibility settings", e)
            _uiState.value = _uiState.value.copy(
                error = "Unable to open accessibility settings. Please enable our service manually in system settings."
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Focus Session",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when a focus session is active"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun restoreSessionIfActive() {
        val isSessionActive = prefs.getBoolean(sessionStateKey, false)
        if (isSessionActive) {
            val startTime = prefs.getLong(sessionStartTimeKey, 0L)
            val duration = prefs.getLong(sessionDurationKey, 25 * 60 * 1000L)
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - startTime
            val remaining = (duration - elapsed).coerceAtLeast(0)

            if (remaining > 0) {
                // Reload the blocked apps and websites
                loadBlocklistCounts()

                _uiState.value = _uiState.value.copy(
                    isSessionActive = true,
                    timeRemaining = remaining,
                    totalTime = duration,
                    isAppBlockEnabled = _uiState.value.selectedAppsCount > 0,
                    isWebsiteBlockEnabled = _uiState.value.selectedWebsitesCount > 0,
                    isFocusModeEnabled = true
                )
                startBackgroundTimer()
                showForegroundNotification()
                // Restart services with both apps and websites
                startBlockingServices()
            } else {
                // Session expired while app was closed
                completeSession()
            }
        }
    }

    fun loadBlocklistCounts() {
        viewModelScope.launch {
            val selectedApps = prefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
            val selectedWebsites = websitePrefs.getStringSet(selectedWebsitesKey, emptySet()) ?: emptySet()

            _uiState.value = _uiState.value.copy(
                selectedAppsCount = selectedApps.size,
                selectedWebsitesCount = selectedWebsites.size,
                blockedApps = selectedApps,
                blockedWebsites = selectedWebsites
            )

            Log.d(TAG, "Loaded blocklist counts - Apps: ${selectedApps.size}, Websites: ${selectedWebsites.size}")
            Log.d(TAG, "Blocked apps: ${selectedApps.joinToString(", ")}")
            Log.d(TAG, "Blocked websites: ${selectedWebsites.joinToString(", ")}")
        }
    }

    fun updateWebsitesCount(count: Int) {
        _uiState.value = _uiState.value.copy(
            selectedWebsitesCount = count
        )
    }



    fun toggleSession() {
        // Check permissions first
        checkPermissions()

        if (_uiState.value.needsPermissionSetup) {
            _uiState.value = _uiState.value.copy(
                error = _uiState.value.permissionMessage ?: "Please enable required permissions to start blocking session"
            )
            return
        }

        // Check if there are apps or websites to block
        if (_uiState.value.selectedAppsCount == 0 && _uiState.value.selectedWebsitesCount == 0) {
            _uiState.value = _uiState.value.copy(
                error = "Please select at least one app or website to block before starting a session"
            )
            return
        }

        val currentState = _uiState.value

        if (currentState.isSessionActive) {
            pauseSession()
        } else {
            startSession()
        }
    }

    private fun startSession() {
        val startTime = System.currentTimeMillis()
        val duration = _uiState.value.totalTime

        // Save session state to preferences
        prefs.edit()
            .putBoolean(sessionStateKey, true)
            .putLong(sessionStartTimeKey, startTime)
            .putLong(sessionDurationKey, duration)
            .apply()

        _uiState.value = _uiState.value.copy(
            isSessionActive = true,
            isAppBlockEnabled = _uiState.value.selectedAppsCount > 0,
            isWebsiteBlockEnabled = _uiState.value.selectedWebsitesCount > 0,
            isFocusModeEnabled = true,
            error = null
        )

        startBackgroundTimer()
        showForegroundNotification()
        startBlockingServices()

        Log.d(TAG, "Focus session started - Duration: ${duration / 1000}s, Apps: ${_uiState.value.selectedAppsCount}, Websites: ${_uiState.value.selectedWebsitesCount}")
    }

    private fun startBlockingServices() {
        try {
            // Prepare the blocked websites list
            val blockedWebsitesList = ArrayList(_uiState.value.blockedWebsites)
            val blockedAppsList = ArrayList(_uiState.value.blockedApps)

            // Send broadcast to update the accessibility service with new blocked lists
            val updateIntent = Intent("com.example.mute_app.UPDATE_BLOCKED_LISTS").apply {
                putStringArrayListExtra("blocked_apps", blockedAppsList)
                putStringArrayListExtra("blocked_websites", blockedWebsitesList)
            }
            application.sendBroadcast(updateIntent)

            // Start overlay service with both apps and websites
            val overlayIntent = Intent(application, OverlayService::class.java).apply {
                putStringArrayListExtra("blocked_apps", blockedAppsList)
                putStringArrayListExtra("blocked_websites", blockedWebsitesList)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                application.startForegroundService(overlayIntent)
            } else {
                application.startService(overlayIntent)
            }

            Log.d(TAG, "Overlay service started successfully with ${_uiState.value.blockedApps.size} apps and ${_uiState.value.blockedWebsites.size} websites")
            Log.d(TAG, "Blocked websites sent to service: ${blockedWebsitesList.joinToString(", ")}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start blocking services", e)
            _uiState.value = _uiState.value.copy(
                error = "Failed to start blocking service. Please check permissions and try again."
            )
            // Stop the session if services fail to start
            pauseSession()
        }
    }

    private fun stopBlockingServices() {
        try {
            // Send broadcast to clear blocked lists in accessibility service
            val clearIntent = Intent("com.example.mute_app.CLEAR_BLOCKED_LISTS")
            application.sendBroadcast(clearIntent)

            // Hide any active overlays first
            val hideIntent = Intent(AppBlockingService.ACTION_HIDE_OVERLAY)
            application.sendBroadcast(hideIntent)

            // Stop overlay service
            val overlayIntent = Intent(application, OverlayService::class.java)
            application.stopService(overlayIntent)

            Log.d(TAG, "Blocking services stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop blocking services", e)
        }
    }

    private fun startBackgroundTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isSessionActive && _uiState.value.timeRemaining > 0) {
                delay(1000)

                val currentState = _uiState.value
                val newTimeRemaining = (currentState.timeRemaining - 1000).coerceAtLeast(0)

                _uiState.value = currentState.copy(
                    timeRemaining = newTimeRemaining
                )

                // Update notification
                updateNotification()

                // Session completed
                if (newTimeRemaining == 0L) {
                    completeSession()
                    break
                }
            }
        }
    }

    private fun pauseSession() {
        timerJob?.cancel()

        // Clear session state from preferences
        prefs.edit()
            .putBoolean(sessionStateKey, false)
            .remove(sessionStartTimeKey)
            .remove(sessionDurationKey)
            .apply()

        _uiState.value = _uiState.value.copy(
            isSessionActive = false,
            isAppBlockEnabled = false,
            isWebsiteBlockEnabled = false,
            isFocusModeEnabled = false,
            timeRemaining = _uiState.value.totalTime
        )

        dismissNotification()
        stopBlockingServices()

        Log.d(TAG, "Focus session paused")
    }

    fun stopSession() {
        pauseSession() // Use the same logic as pause
    }

    private fun completeSession() {
        timerJob?.cancel()

        // Clear session state from preferences
        prefs.edit()
            .putBoolean(sessionStateKey, false)
            .remove(sessionStartTimeKey)
            .remove(sessionDurationKey)
            .apply()

        val currentState = _uiState.value
        val sessionDurationMinutes = currentState.totalTime / (60 * 1000)

        // Update stats
        val newSessionsCompleted = currentState.sessionsCompleted + 1
        val newTotalFocusTime = currentState.totalFocusTime + sessionDurationMinutes

        // Save updated stats
        prefs.edit()
            .putInt("total_sessions", newSessionsCompleted)
            .putLong("total_focus_time", newTotalFocusTime)
            .apply()

        _uiState.value = currentState.copy(
            isSessionActive = false,
            isAppBlockEnabled = false,
            isWebsiteBlockEnabled = false,
            isFocusModeEnabled = false,
            sessionsCompleted = newSessionsCompleted,
            totalFocusTime = newTotalFocusTime,
            timeRemaining = currentState.totalTime
        )

        dismissNotification()
        stopBlockingServices()

        Log.d(TAG, "Focus session completed successfully")
    }

    private fun showForegroundNotification() {
        val intent = Intent().apply {
            setClassName(application, "com.example.mute_app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            application, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val blockedItemsText = buildString {
            if (_uiState.value.selectedAppsCount > 0) {
                append("${_uiState.value.selectedAppsCount} apps")
            }
            if (_uiState.value.selectedWebsitesCount > 0) {
                if (isNotEmpty()) append(" & ")
                append("${_uiState.value.selectedWebsitesCount} websites")
            }
        }

        val notification = NotificationCompat.Builder(application, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText("Blocking $blockedItemsText")
            .setSmallIcon(R.drawable.ic_block)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val intent = Intent().apply {
            setClassName(application, "com.example.mute_app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            application, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = (_uiState.value.timeRemaining / 1000 / 60) % 60
        val seconds = (_uiState.value.timeRemaining / 1000) % 60
        val timeText = String.format("%02d:%02d remaining", minutes, seconds)

        val notification = NotificationCompat.Builder(application, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText(timeText)
            .setSmallIcon(R.drawable.ic_block)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun dismissNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun changeMode(mode: String) {
        if (_uiState.value.isSessionActive) return // Don't change mode during active session

        val duration = when (mode) {
            "Focus" -> 25 * 60 * 1000L // 25 minutes
            "Break" -> 5 * 60 * 1000L  // 5 minutes
            "Deep Work" -> 50 * 60 * 1000L // 50 minutes
            else -> 25 * 60 * 1000L
        }

        _uiState.value = _uiState.value.copy(
            currentMode = mode,
            timeRemaining = duration,
            totalTime = duration
        )
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                // Load actual user stats from SharedPreferences
                val savedSessions = prefs.getInt("total_sessions", 0)
                val savedFocusTime = prefs.getLong("total_focus_time", 0L)
                val savedStreak = prefs.getInt("streak_days", 0)

                _uiState.value = _uiState.value.copy(
                    sessionsCompleted = savedSessions,
                    totalFocusTime = savedFocusTime,
                    streakDays = savedStreak
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user stats: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun testOverlay() {
        // Test function to show overlay manually - for development/debugging
        val intent = Intent(AppBlockingService.ACTION_SHOW_OVERLAY).apply {
            putExtra(AppBlockingService.EXTRA_BLOCKED_APP_NAME, "Test App")
            putExtra(AppBlockingService.EXTRA_BLOCKED_PACKAGE, "com.test.app")
        }
        application.sendBroadcast(intent)
        Log.d(TAG, "Test overlay broadcast sent")
    }

    override fun onCleared() {
        super.onCleared()
        // Don't cancel timer job on clear - let it run in background
        // Only dismiss notification if session is not active
        if (!_uiState.value.isSessionActive) {
            dismissNotification()
        }
        Log.d(TAG, "BreakViewModel cleared")
    }
}
