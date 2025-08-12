package com.example.mute_app.viewmodels.explore.`break`

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean = false
)

data class ApplicationsUiState(
    val allApps: List<AppInfo> = emptyList(),
    val selectedApps: Set<String> = emptySet(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    // SharedPreferences for persistent storage
    private val prefs = application.getSharedPreferences("break_app_prefs", android.content.Context.MODE_PRIVATE)
    private val selectedAppsKey = "selected_apps_for_blocking"

    init {
        loadSavedSelectedApps()
        loadInstalledApps()
    }

    private fun loadSavedSelectedApps() {
        val savedApps = prefs.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
        _uiState.value = _uiState.value.copy(selectedApps = savedApps)
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val apps = withContext(Dispatchers.IO) {
                    val packageManager = getApplication<Application>().packageManager

                    // Get apps with launcher activities (what you see in Settings > Apps)
                    val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                        addCategory(android.content.Intent.CATEGORY_LAUNCHER)
                    }
                    val launcherApps = packageManager.queryIntentActivities(launcherIntent, 0)
                        .map { it.activityInfo.packageName }
                        .toSet()

                    // Get all installed apps
                    val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

                    installedApps
                        .filter { app ->
                            // Only include apps that meet these criteria
                            launcherApps.contains(app.packageName) || // Has launcher activity
                                    (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || // User installed
                                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 // Updated system apps
                        }
                        .mapNotNull { app ->
                            try {
                                val appName = app.loadLabel(packageManager).toString()
                                val icon = try {
                                    app.loadIcon(packageManager)
                                } catch (e: Exception) {
                                    null
                                }

                                // Skip system services and background shit
                                if (shouldSkipApp(app.packageName, appName)) {
                                    null
                                } else {
                                    AppInfo(
                                        packageName = app.packageName,
                                        appName = appName,
                                        icon = icon,
                                        isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                    )
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        .distinctBy { it.packageName }
                        .sortedWith(compareBy<AppInfo> { it.isSystemApp }.thenBy { it.appName.lowercase() })
                }

                _uiState.value = _uiState.value.copy(
                    allApps = apps,
                    filteredApps = apps,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load apps: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun shouldSkipApp(packageName: String, appName: String): Boolean {
        // Skip system services and background components
        val skipPatterns = listOf(
            "com.android.systemui",
            "com.android.shell",
            "com.android.phone",
            "com.android.providers",
            "com.android.inputmethod",
            "com.android.keychain",
            "com.android.bluetooth",
            "com.android.nfc",
            "com.android.server",
            "com.android.captiveportal",
            "com.android.carrierconfig",
            "com.android.emergency",
            "android.ext.services",
            "com.android.printspooler",
            "com.android.externalstorage",
            "com.android.mtp",
            "com.android.wallpaper",
            "com.android.bips",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.google.android.setupwizard",
            "com.google.android.feedback",
            "com.google.android.partnersetup",
            "com.google.android.syncadapters",
            "com.google.android.backuptransport",
            "com.google.android.configupdater",
            "com.google.android.overlay",
            "com.google.android.packageinstaller"
        )

        // Skip if package matches any pattern
        if (skipPatterns.any { packageName.startsWith(it) }) {
            return true
        }

        // Skip apps with generic system names
        val systemNames = listOf(
            "Android System",
            "System UI",
            "System",
            "Package installer",
            "Launcher",
            "Bluetooth",
            "NFC",
            "Print Spooler"
        )

        return systemNames.any { appName.equals(it, ignoreCase = true) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterApps(query)
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.allApps
        } else {
            _uiState.value.allApps.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
            }
        }
        _uiState.value = _uiState.value.copy(filteredApps = filtered)
    }

    fun toggleAppSelection(packageName: String) {
        val currentSelected = _uiState.value.selectedApps
        val newSelected = if (currentSelected.contains(packageName)) {
            currentSelected - packageName
        } else {
            currentSelected + packageName
        }

        _uiState.value = _uiState.value.copy(selectedApps = newSelected)
    }

    fun saveSelectedApps() {
        val selectedApps = _uiState.value.selectedApps
        prefs.edit()
            .putStringSet(selectedAppsKey, selectedApps)
            .apply()
    }

    fun getSelectedAppsCount(): Int = _uiState.value.selectedApps.size

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}