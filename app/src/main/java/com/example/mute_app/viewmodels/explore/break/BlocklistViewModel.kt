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
    val blockedWebsites: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BlocklistUiState())
    val uiState: StateFlow<BlocklistUiState> = _uiState.asStateFlow()

    // SharedPreferences for persistent storage
    private val prefs = application.getSharedPreferences("break_app_prefs", android.content.Context.MODE_PRIVATE)
    private val selectedAppsKey = "selected_apps_for_blocking"
    private val selectedWebsitesKey = "selected_websites_for_blocking"

    init {
        loadBlocklistData()
    }

    private fun loadBlocklistData() {
        viewModelScope.launch {
            val selectedApps = prefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
            val selectedWebsites = prefs.getStringSet(selectedWebsitesKey, emptySet()) ?: emptySet()

            _uiState.value = _uiState.value.copy(
                selectedAppsCount = selectedApps.size,
                selectedWebsitesCount = selectedWebsites.size,
                blockedWebsites = selectedWebsites.toList()
            )
        }
    }

    fun refreshCounts() {
        loadBlocklistData()
    }
}