package com.example.mute_app.viewmodels.explore.`break`

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val isFocusModeEnabled: Boolean = false,
    val selectedAppsCount: Int = 0,
    val selectedWebsitesCount: Int = 0,
    val error: String? = null
)

@HiltViewModel
class BreakViewModel @Inject constructor(
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BreakUiState())
    val uiState: StateFlow<BreakUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // SharedPreferences for persistent storage
    private val prefs = application.getSharedPreferences("break_app_prefs", Context.MODE_PRIVATE)
    private val selectedAppsKey = "selected_apps_for_blocking"
    private val selectedWebsitesKey = "selected_websites_for_blocking"
    private val sessionStateKey = "session_active"
    private val sessionStartTimeKey = "session_start_time"
    private val sessionDurationKey = "session_duration"

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "break_app_blocking"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
        loadUserStats()
        loadBlocklistCounts()
        restoreSessionIfActive()
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
                _uiState.value = _uiState.value.copy(
                    isSessionActive = true,
                    timeRemaining = remaining,
                    totalTime = duration
                )
                startBackgroundTimer()
                showForegroundNotification()
            } else {
                // Session expired while app was closed
                completeSession()
            }
        }
    }

    fun loadBlocklistCounts() {
        viewModelScope.launch {
            val selectedApps = prefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
            val selectedWebsites = prefs.getStringSet(selectedWebsitesKey, emptySet()) ?: emptySet()

            _uiState.value = _uiState.value.copy(
                selectedAppsCount = selectedApps.size,
                selectedWebsitesCount = selectedWebsites.size
            )
        }
    }

    fun toggleSession() {
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
            isAppBlockEnabled = true,
            isFocusModeEnabled = true
        )

        startBackgroundTimer()
        showForegroundNotification()
        enableAppBlocking()
        enableFocusMode()
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
            isFocusModeEnabled = false,
            timeRemaining = _uiState.value.totalTime
        )

        dismissNotification()
        disableAppBlocking()
        disableFocusMode()
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

        _uiState.value = currentState.copy(
            isSessionActive = false,
            isAppBlockEnabled = false,
            isFocusModeEnabled = false,
            sessionsCompleted = currentState.sessionsCompleted + 1,
            totalFocusTime = currentState.totalFocusTime + sessionDurationMinutes,
            timeRemaining = currentState.totalTime
        )

        dismissNotification()
        disableAppBlocking()
        disableFocusMode()

        // TODO: Show completion celebration
        // TODO: Save completed session to database
    }

    private fun showForegroundNotification() {
        val intent = Intent(application, application.javaClass) // Replace with your main activity
        val pendingIntent = PendingIntent.getActivity(
            application, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(application, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText("Blocking distractions...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Replace with your app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val intent = Intent(application, application.javaClass) // Replace with your main activity
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
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Replace with your app icon
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

    fun toggleAppBlock() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isAppBlockEnabled = !currentState.isAppBlockEnabled
        )

        if (_uiState.value.isAppBlockEnabled) {
            enableAppBlocking()
        } else {
            disableAppBlocking()
        }
    }

    fun toggleFocusMode() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isFocusModeEnabled = !currentState.isFocusModeEnabled
        )

        if (_uiState.value.isFocusModeEnabled) {
            enableFocusMode()
        } else {
            disableFocusMode()
        }
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            try {
                // TODO: Load actual user stats from database
                // For now, using mock data
                _uiState.value = _uiState.value.copy(
                    sessionsCompleted = 3,
                    totalFocusTime = 120, // 2 hours
                    streakDays = 5
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load user stats: ${e.message}"
                )
            }
        }
    }

    private fun enableAppBlocking() {
        // TODO: Integrate with Android's app usage/device admin APIs
        // or implement custom app blocking mechanism
        viewModelScope.launch {
            try {
                // Mock implementation - replace with actual blocking logic
                delay(500)

                // This is where you would:
                // 1. Get the list of selected apps from SharedPreferences
                // 2. Use Android's UsageStatsManager or AccessibilityService
                // 3. Block the selected apps from launching

                val selectedApps = prefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
                // TODO: Apply blocking to these apps

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to enable app blocking: ${e.message}",
                    isAppBlockEnabled = false
                )
            }
        }
    }

    private fun disableAppBlocking() {
        viewModelScope.launch {
            try {
                // Mock implementation - replace with actual unblocking logic
                delay(500)

                // TODO: Remove blocking from all apps

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to disable app blocking: ${e.message}",
                    isAppBlockEnabled = true
                )
            }
        }
    }

    private fun enableFocusMode() {
        // TODO: Implement focus mode features like:
        // - Do Not Disturb
        // - Custom notification filtering
        // - Screen dimming
        // - Background app restrictions
        viewModelScope.launch {
            try {
                // Mock implementation
                delay(500)

                // This is where you would:
                // 1. Enable Do Not Disturb mode
                // 2. Dim screen brightness
                // 3. Filter notifications
                // 4. Apply system-wide focus settings

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to enable focus mode: ${e.message}",
                    isFocusModeEnabled = false
                )
            }
        }
    }

    private fun disableFocusMode() {
        viewModelScope.launch {
            try {
                // Mock implementation
                delay(500)

                // TODO: Restore normal system settings

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to disable focus mode: ${e.message}",
                    isFocusModeEnabled = true
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        // Don't cancel timer job on clear - let it run in background
        // timerJob?.cancel()

        // Only dismiss notification if session is not active
        if (!_uiState.value.isSessionActive) {
            dismissNotification()
        }
    }
}