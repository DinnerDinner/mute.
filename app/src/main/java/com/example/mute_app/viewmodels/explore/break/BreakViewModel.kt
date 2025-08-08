package com.example.mute_app.viewmodels.explore.`break`

import androidx.lifecycle.ViewModel
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
    val error: String? = null
)

@HiltViewModel
class BreakViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BreakUiState())
    val uiState: StateFlow<BreakUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadUserStats()
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
        _uiState.value = _uiState.value.copy(isSessionActive = true)

        timerJob = viewModelScope.launch {
            while (_uiState.value.isSessionActive && _uiState.value.timeRemaining > 0) {
                delay(1000)

                val currentState = _uiState.value
                val newTimeRemaining = (currentState.timeRemaining - 1000).coerceAtLeast(0)

                _uiState.value = currentState.copy(
                    timeRemaining = newTimeRemaining
                )

                // Session completed
                if (newTimeRemaining == 0L) {
                    completeSession()
                }
            }
        }
    }

    private fun pauseSession() {
        _uiState.value = _uiState.value.copy(isSessionActive = false)
        timerJob?.cancel()
    }

    fun stopSession() {
        timerJob?.cancel()
        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            isSessionActive = false,
            timeRemaining = currentState.totalTime
        )
    }

    private fun completeSession() {
        val currentState = _uiState.value
        val sessionDurationMinutes = currentState.totalTime / (60 * 1000)

        _uiState.value = currentState.copy(
            isSessionActive = false,
            sessionsCompleted = currentState.sessionsCompleted + 1,
            totalFocusTime = currentState.totalFocusTime + sessionDurationMinutes,
            timeRemaining = currentState.totalTime
        )

        // TODO: Save completed session to database
        // TODO: Show completion notification/celebration
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

        // TODO: Implement actual app blocking functionality
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

        // TODO: Implement actual focus mode functionality
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
                // Mock implementation
                delay(500)
                // Show success feedback
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
                // Mock implementation
                delay(500)
                // Show success feedback
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
                // Show success feedback
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
                // Show success feedback
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
        timerJob?.cancel()
    }
}