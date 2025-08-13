package com.example.mute_app.viewmodels.explore.`break`

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlocklistUiState(
    val selectedAppsCount: Int = 0,
    val selectedWebsitesCount: Int = 0,
    val blockedApps: Set<String> = emptySet(),
    val blockedWebsites: Set<String> = emptySet(),
    val totalBlockedItems: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BlocklistUiState())
    val uiState: StateFlow<BlocklistUiState> = _uiState.asStateFlow()

    // SharedPreferences for persistent storage
    private val appPrefs = application.getSharedPreferences("break_app_prefs", android.content.Context.MODE_PRIVATE)
    private val websitePrefs = application.getSharedPreferences("break_website_prefs", android.content.Context.MODE_PRIVATE)
    private val selectedAppsKey = "selected_apps_for_blocking"
    private val selectedWebsitesKey = "selected_websites_for_blocking"

    init {
        loadBlocklistData()
    }

    private fun loadBlocklistData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val selectedApps = appPrefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
                val selectedWebsites = websitePrefs.getStringSet(selectedWebsitesKey, emptySet()) ?: emptySet()

                _uiState.value = _uiState.value.copy(
                    selectedAppsCount = selectedApps.size,
                    selectedWebsitesCount = selectedWebsites.size,
                    blockedApps = selectedApps,
                    blockedWebsites = selectedWebsites,
                    totalBlockedItems = selectedApps.size + selectedWebsites.size,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load blocklist data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun refreshCounts() {
        loadBlocklistData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getBlocklistSummary(): String {
        val appsCount = _uiState.value.selectedAppsCount
        val websitesCount = _uiState.value.selectedWebsitesCount

        return when {
            appsCount > 0 && websitesCount > 0 -> "$appsCount apps & $websitesCount websites"
            appsCount > 0 -> "$appsCount apps"
            websitesCount > 0 -> "$websitesCount websites"
            else -> "No items selected"
        }
    }

    fun hasBlockedItems(): Boolean {
        return _uiState.value.totalBlockedItems > 0
    }
}