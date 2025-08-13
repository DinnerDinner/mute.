package com.example.mute_app.viewmodels.explore.`break`

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

data class WebsiteInfo(
    val url: String,
    val displayName: String,
    val isCustom: Boolean = true
)

data class WebsitesUiState(
    val allWebsites: List<WebsiteInfo> = emptyList(),
    val selectedWebsites: Set<String> = emptySet(),
    val filteredWebsites: List<WebsiteInfo> = emptyList(),
    val searchQuery: String = "",
    val urlInput: String = "",
    val isUrlValid: Boolean = true,
    val showError: Boolean = false,
    val errorMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WebsitesViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WebsitesUiState())
    val uiState: StateFlow<WebsitesUiState> = _uiState.asStateFlow()

    // SharedPreferences for persistent storage
    private val prefs = application.getSharedPreferences("break_website_prefs", android.content.Context.MODE_PRIVATE)
    private val selectedWebsitesKey = "selected_websites_for_blocking"

    // Popular websites for quick suggestions
    private val popularWebsites = listOf(
        WebsiteInfo("facebook.com", "Facebook", false),
        WebsiteInfo("instagram.com", "Instagram", false),
        WebsiteInfo("twitter.com", "Twitter", false),
        WebsiteInfo("tiktok.com", "TikTok", false),
        WebsiteInfo("youtube.com", "YouTube", false),
        WebsiteInfo("reddit.com", "Reddit", false),
        WebsiteInfo("netflix.com", "Netflix", false),
        WebsiteInfo("twitch.tv", "Twitch", false)
    )

    init {
        loadSavedSelectedWebsites()
        loadDefaultWebsites()
    }

    private fun loadSavedSelectedWebsites() {
        val savedWebsites = prefs.getStringSet(selectedWebsitesKey, emptySet()) ?: emptySet()
        _uiState.value = _uiState.value.copy(selectedWebsites = savedWebsites)
    }

    private fun loadDefaultWebsites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Load popular websites as default options
                _uiState.value = _uiState.value.copy(
                    allWebsites = popularWebsites,
                    filteredWebsites = popularWebsites,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load websites: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updateUrlInput(input: String) {
        _uiState.value = _uiState.value.copy(
            urlInput = input,
            showError = false,
            isUrlValid = true
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterWebsites(query)
    }

    private fun filterWebsites(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.allWebsites
        } else {
            _uiState.value.allWebsites.filter { website ->
                website.displayName.contains(query, ignoreCase = true) ||
                        website.url.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(filteredWebsites = filtered)
    }

    fun addWebsite() {
        val input = _uiState.value.urlInput.trim()

        if (!isValidUrl(input)) {
            _uiState.value = _uiState.value.copy(
                isUrlValid = false,
                showError = true,
                errorMessage = "Please enter a valid website URL"
            )
            return
        }

        val cleanedUrl = cleanUrl(input)

        // Check if website already exists
        val existingWebsite = _uiState.value.allWebsites.find { it.url.equals(cleanedUrl, ignoreCase = true) }
        if (existingWebsite != null) {
            _uiState.value = _uiState.value.copy(
                isUrlValid = false,
                showError = true,
                errorMessage = "This website is already in your list"
            )
            return
        }

        // Add new website
        val newWebsite = WebsiteInfo(
            url = cleanedUrl,
            displayName = cleanedUrl.removePrefix("www.").split(".").first().replaceFirstChar { it.uppercase() },
            isCustom = true
        )

        val updatedWebsites = _uiState.value.allWebsites + newWebsite
        _uiState.value = _uiState.value.copy(
            allWebsites = updatedWebsites,
            filteredWebsites = if (_uiState.value.searchQuery.isBlank()) updatedWebsites else filterWebsitesList(updatedWebsites, _uiState.value.searchQuery),
            urlInput = "",
            selectedWebsites = _uiState.value.selectedWebsites + cleanedUrl
        )
    }

    private fun filterWebsitesList(websites: List<WebsiteInfo>, query: String): List<WebsiteInfo> {
        return websites.filter { website ->
            website.displayName.contains(query, ignoreCase = true) ||
                    website.url.contains(query, ignoreCase = true)
        }
    }

    fun clearUrlInput() {
        _uiState.value = _uiState.value.copy(
            urlInput = "",
            showError = false,
            isUrlValid = true
        )
    }

    fun toggleWebsiteSelection(url: String) {
        val currentSelected = _uiState.value.selectedWebsites

        val newSelected = if (currentSelected.contains(url)) {
            currentSelected - url
        } else {
            currentSelected + url
        }

        _uiState.value = _uiState.value.copy(selectedWebsites = newSelected)
    }

    fun removeWebsite(url: String) {
        val updatedWebsites = _uiState.value.allWebsites.filter { it.url != url }
        val updatedSelected = _uiState.value.selectedWebsites - url

        _uiState.value = _uiState.value.copy(
            allWebsites = updatedWebsites,
            filteredWebsites = if (_uiState.value.searchQuery.isBlank()) updatedWebsites else filterWebsitesList(updatedWebsites, _uiState.value.searchQuery),
            selectedWebsites = updatedSelected
        )
    }

    fun saveSelectedWebsites() {
        val selectedWebsites = _uiState.value.selectedWebsites
        prefs.edit()
            .putStringSet(selectedWebsitesKey, selectedWebsites)
            .apply()
    }


    fun getSelectedWebsitesCount(): Int = _uiState.value.selectedWebsites.size

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun isValidUrl(url: String): Boolean {
        val urlPattern = Pattern.compile(
            "^(https?://)?" + // Optional protocol
                    "([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}" + // Domain
                    "(/.*)?$" // Optional path
        )
        return urlPattern.matcher(url.trim()).matches()
    }

    private fun cleanUrl(url: String): String {
        return url.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .lowercase()
    }
}