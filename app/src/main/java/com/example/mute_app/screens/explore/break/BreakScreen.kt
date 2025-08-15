@file:OptIn(ExperimentalAnimationApi::class)
package com.example.mute_app.screens.explore.`break`

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.viewmodels.explore.`break`.BreakViewModel
import com.example.mute_app.viewmodels.explore.`break`.WebsitesViewModel
import kotlinx.coroutines.delay
import kotlin.math.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner

// Wellness-focused green color palette
private val WellnessColors = object {
    val Primary = Color(0xFF2E7D5A)        // Deep forest green
    val Secondary = Color(0xFF4CAF50)      // Material green
    val Accent = Color(0xFF81C784)         // Light green
    val Success = Color(0xFF66BB6A)        // Success green
    val Warning = Color(0xFFFF9800)        // Orange for warnings
    val Error = Color(0xFFE53935)          // Red for errors
    val Background = Color(0xFF1B2E1F)     // Dark green background
    val Surface = Color(0xFF243428)        // Card surface green
    val OnSurface = Color(0xFFE8F5E8)      // Light green text
    val Glow = Color(0xFF4CAF50)           // Green glow
    val Inactive = Color(0xFF546E7A)       // Inactive state
}

@Composable
fun BreakScreen(
    onBackClick: () -> Unit,
    onNavigateToApps: () -> Unit = {},
    onNavigateToWebsites: () -> Unit = {},
    viewModel: BreakViewModel = hiltViewModel(),
    websitesViewModel: WebsitesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val websitesUiState by websitesViewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Timer picker state - Initialize with saved values from viewModel
    var showTimerPicker by remember { mutableStateOf(false) }
    var selectedHours by remember { mutableStateOf(uiState.savedTimerHours) }
    var selectedMinutes by remember { mutableStateOf(uiState.savedTimerMinutes) }

    // Update local state when viewModel state changes
    LaunchedEffect(uiState.savedTimerHours, uiState.savedTimerMinutes) {
        selectedHours = uiState.savedTimerHours
        selectedMinutes = uiState.savedTimerMinutes
    }

    // Load counts when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadBlocklistCounts()
        viewModel.refreshPermissions()
        // Load websites count
        viewModel.updateWebsitesCount(websitesUiState.selectedWebsites.size)

        // Debug logging
        println("DEBUG: BreakScreen - needsPermissionSetup: ${uiState.needsPermissionSetup}")
        println("DEBUG: BreakScreen - hasOverlayPermission: ${uiState.hasOverlayPermission}")
        println("DEBUG: BreakScreen - hasAccessibilityPermission: ${uiState.hasAccessibilityPermission}")
        println("DEBUG: BreakScreen - selectedAppsCount: ${uiState.selectedAppsCount}")
        println("DEBUG: BreakScreen - selectedWebsitesCount: ${websitesUiState.selectedWebsites.size}")
    }

    // Update websites count when it changes
    LaunchedEffect(websitesUiState.selectedWebsites.size) {
        viewModel.updateWebsitesCount(websitesUiState.selectedWebsites.size)
    }

    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error for 3 seconds then clear
            delay(5000)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WellnessColors.Background,
                        WellnessColors.Primary.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // If permissions are needed, show permissions screen instead of main content
        if (uiState.needsPermissionSetup) {
            PermissionsScreen(
                uiState = uiState,
                onRequestOverlayPermission = viewModel::requestOverlayPermission,
                onRequestAccessibilityPermission = viewModel::requestAccessibilityPermission,
                onRefreshPermissions = viewModel::refreshPermissions,
                onBackClick = onBackClick
            )
        } else {
            // Main Break Screen Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(bottom = 80.dp) // Add padding for pinned button
                ) {
                    // Header with back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = WellnessColors.OnSurface
                            )
                        }

                        Text(
                            text = "break.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = WellnessColors.OnSurface
                        )

                        // Permissions indicator - keep the warning triangle
                        IconButton(onClick = viewModel::refreshPermissions) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh permissions",
                                tint = WellnessColors.Accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // TOP SECTION (40% height) - Dynamic State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSessionActive) {
                            // State 2: Active Timer
                            ActiveTimerState(timeRemaining = uiState.timeRemaining)
                        } else {
                            // State 1: Unblocked
                            UnblockedState()
                        }
                    }

                    // MIDDLE SECTION - Control Buttons (Reduced height)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Row 1: Timer & Access Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ControlButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Timer,
                                title = "Timer",
                                subtitle = formatTimerDuration(selectedHours, selectedMinutes),
                                onClick = { showTimerPicker = true }
                            )
                            ControlButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Key,
                                title = "Access",
                                subtitle = "Toggle", // TODO: Get from viewModel
                                onClick = { /* TODO: Access options */ }
                            )
                        }

                        // Row 2: Apps & Websites
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ControlButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Apps,
                                title = "Apps",
                                subtitle = "${uiState.selectedAppsCount} selected",
                                onClick = onNavigateToApps,
                                hasSelection = uiState.selectedAppsCount > 0
                            )
                            ControlButton(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Language,
                                title = "Websites",
                                subtitle = "${uiState.selectedWebsitesCount} selected",
                                onClick = onNavigateToWebsites,
                                hasSelection = uiState.selectedWebsitesCount > 0
                            )
                        }

                        // Row 3: More Options (Full width)
                        ControlButton(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Settings,
                            title = "More Options",
                            subtitle = "Advanced settings",
                            onClick = { /* TODO: More options */ },
                            isFullWidth = true
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SUMMARY SECTION (Scrollable preview)
                    SummarySection(
                        selectedAppsCount = uiState.selectedAppsCount,
                        selectedWebsitesCount = uiState.selectedWebsitesCount,
                        blockedApps = uiState.blockedApps,
                        blockedWebsites = uiState.blockedWebsites,
                        timerDuration = formatTimerDuration(selectedHours, selectedMinutes)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // PINNED BOTTOM BUTTON - Start/Stop Button
                StartStopButton(
                    isActive = uiState.isSessionActive,
                    canStart = !uiState.needsPermissionSetup &&
                            (uiState.selectedAppsCount > 0 || uiState.selectedWebsitesCount > 0),
                    onToggle = viewModel::toggleSession,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Timer Picker Modal
            if (showTimerPicker) {
                TimerPickerModal(
                    initialHours = selectedHours,
                    initialMinutes = selectedMinutes,
                    onTimeSelected = { hours, minutes ->
                        selectedHours = hours
                        selectedMinutes = minutes

                        // Update viewModel with new timer duration
                        viewModel.updateTimerDuration(hours, minutes)

                        showTimerPicker = false
                    },
                    onDismiss = { showTimerPicker = false }
                )
            }
        }

        // Error Snackbar
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = WellnessColors.Error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = viewModel::clearError
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// OLD PERMISSIONS SCREEN FROM THE FIRST CODE - EXACT SAME
@Composable
private fun PermissionsScreen(
    uiState: com.example.mute_app.viewmodels.explore.`break`.BreakUiState,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    onRefreshPermissions: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WellnessColors.OnSurface
                )
            }

            Text(
                text = "Permissions Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WellnessColors.OnSurface
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Warning Icon
        Icon(
            Icons.Default.Security,
            contentDescription = "Security",
            tint = WellnessColors.Warning,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "To function properly, mute. needs these permissions:",
            fontSize = 18.sp,
            color = WellnessColors.OnSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Permission Cards
        PermissionCard(
            title = "Display over other apps",
            description = "Required to show blocking overlay",
            isGranted = uiState.hasOverlayPermission,
            onGrant = onRequestOverlayPermission
        )

        Spacer(modifier = Modifier.height(16.dp))

        PermissionCard(
            title = "Accessibility Service",
            description = "Required to detect blocked apps",
            isGranted = uiState.hasAccessibilityPermission,
            onGrant = onRequestAccessibilityPermission
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Refresh Button
        Button(
            onClick = onRefreshPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WellnessColors.Primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Check Permissions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "After granting permissions, tap 'Check Permissions'",
            fontSize = 14.sp,
            color = WellnessColors.OnSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                WellnessColors.Success.copy(alpha = 0.1f)
            else
                WellnessColors.Surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WellnessColors.OnSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = WellnessColors.OnSurface.copy(alpha = 0.7f)
                )
            }

            if (isGranted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = WellnessColors.Success,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onGrant,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WellnessColors.Warning
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Grant",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun UnblockedState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large padlock icon with subtle animation
        var isAnimating by remember { mutableStateOf(false) }
        val rotationAngle by animateFloatAsState(
            targetValue = if (isAnimating) 10f else 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "padlock_rotation"
        )

        LaunchedEffect(Unit) {
            delay(1000)
            isAnimating = true
        }

        Icon(
            Icons.Default.LockOpen,
            contentDescription = "Unblocked",
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer(rotationZ = rotationAngle),
            tint = WellnessColors.Accent.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unblocked",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = WellnessColors.OnSurface
        )

        Text(
            text = "Configure your settings and start focusing",
            fontSize = 16.sp,
            color = WellnessColors.OnSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActiveTimerState(timeRemaining: Long) {
    val minutes = (timeRemaining / 1000 / 60) % 60
    val seconds = (timeRemaining / 1000) % 60
    val hours = (timeRemaining / 1000 / 60 / 60)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle lock icon
        Icon(
            Icons.Default.Lock,
            contentDescription = "Blocked",
            modifier = Modifier.size(40.dp),
            tint = WellnessColors.Success.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large countdown timer
        Text(
            text = if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = WellnessColors.Success,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Blocking Active",
            fontSize = 18.sp,
            color = WellnessColors.OnSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ControlButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    hasSelection: Boolean = false,
    isFullWidth: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(
                elevation = if (hasSelection) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (hasSelection) WellnessColors.Success.copy(alpha = 0.3f)
                else WellnessColors.Primary.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (hasSelection)
                WellnessColors.Success.copy(alpha = 0.1f)
            else
                WellnessColors.Surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isFullWidth) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = if (hasSelection) WellnessColors.Success else WellnessColors.Accent
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WellnessColors.OnSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = WellnessColors.OnSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // MODIFIED: Reduced height by putting icon and title on same line
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Reduced padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon and title on same line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = title,
                        modifier = Modifier.size(20.dp), // Slightly smaller icon
                        tint = if (hasSelection) WellnessColors.Success else WellnessColors.Accent
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        fontSize = 14.sp, // Slightly smaller title
                        fontWeight = FontWeight.Medium,
                        color = WellnessColors.OnSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp)) // Reduced spacing

                Text(
                    text = subtitle,
                    fontSize = 12.sp, // Smaller subtitle
                    color = WellnessColors.OnSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    selectedAppsCount: Int,
    selectedWebsitesCount: Int,
    blockedApps: Set<String>,
    blockedWebsites: Set<String>,
    timerDuration: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = WellnessColors.Surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Summary:",
                fontSize = 14.sp,
                color = WellnessColors.OnSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Apps summary
            if (selectedAppsCount > 0) {
                SummaryItem(
                    icon = Icons.Default.Apps,
                    title = "Apps ($selectedAppsCount)",
                    items = blockedApps.take(3).toList(),
                    hasMore = blockedApps.size > 3
                )

                if (selectedWebsitesCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Websites summary
            if (selectedWebsitesCount > 0) {
                SummaryItem(
                    icon = Icons.Default.Language,
                    title = "Websites ($selectedWebsitesCount)",
                    items = blockedWebsites.take(3).toList(),
                    hasMore = blockedWebsites.size > 3
                )
            }

            // Placeholder items for timer and access method
            if (selectedAppsCount > 0 || selectedWebsitesCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                SummaryItem(
                    icon = Icons.Default.Timer,
                    title = "Timer",
                    items = listOf(timerDuration),
                    hasMore = false
                )

                Spacer(modifier = Modifier.height(8.dp))
                SummaryItem(
                    icon = Icons.Default.Key,
                    title = "Access Method",
                    items = listOf("Toggle mode"), // TODO: Get from viewModel
                    hasMore = false
                )
            }

            // Empty state
            if (selectedAppsCount == 0 && selectedWebsitesCount == 0) {
                Text(
                    text = "No apps or websites selected",
                    fontSize = 14.sp,
                    color = WellnessColors.OnSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    items: List<String>,
    hasMore: Boolean
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = WellnessColors.Accent
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WellnessColors.OnSurface
            )

            Text(
                text = buildString {
                    append(items.joinToString(", "))
                    if (hasMore) append("...")
                },
                fontSize = 12.sp,
                color = WellnessColors.OnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StartStopButton(
    isActive: Boolean,
    canStart: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isActive || canStart,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) WellnessColors.Error else WellnessColors.Primary,
            disabledContainerColor = WellnessColors.Inactive.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when {
                isActive -> "Stop Blocking"
                canStart -> "Start Blocking"
                else -> "Setup Required"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Helper function to format timer duration
private fun formatTimerDuration(hours: Int, minutes: Int): String {
    return when {
        hours == 0 && minutes == 0 -> "Off"
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

@Composable
private fun TimerPickerModal(
    initialHours: Int,
    initialMinutes: Int,
    onTimeSelected: (hours: Int, minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var tempHours by remember { mutableStateOf(initialHours) }
    var tempMinutes by remember { mutableStateOf(initialMinutes) }

    // Background overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .clickable { }, // Prevent click-through
            colors = CardDefaults.cardColors(
                containerColor = WellnessColors.Surface
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Set Timer Duration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WellnessColors.OnSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Time Pickers Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hours Picker
                    TimePickerColumn(
                        label = "Hours",
                        value = tempHours,
                        range = 0..23,
                        onValueChange = { tempHours = it }
                    )

                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = WellnessColors.OnSurface
                    )

                    // Minutes Picker
                    TimePickerColumn(
                        label = "Minutes",
                        value = tempMinutes,
                        range = 0..59,
                        step = 5, // 5-minute increments
                        onValueChange = { tempMinutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick preset buttons
                Text(
                    text = "Quick Presets:",
                    fontSize = 14.sp,
                    color = WellnessColors.OnSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton("15m") { tempHours = 0; tempMinutes = 15 }
                    PresetButton("25m") { tempHours = 0; tempMinutes = 25 }
                    PresetButton("45m") { tempHours = 0; tempMinutes = 45 }
                    PresetButton("1h") { tempHours = 1; tempMinutes = 0 }
                    PresetButton("2h") { tempHours = 2; tempMinutes = 0 }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = WellnessColors.OnSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onTimeSelected(tempHours, tempMinutes)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WellnessColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Set Timer")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerColumn(
    label: String,
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = WellnessColors.OnSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Up button
        IconButton(
            onClick = {
                val newValue = (value + step).coerceIn(range)
                onValueChange(newValue)
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase $label",
                tint = WellnessColors.Accent
            )
        }

        // Current value display
        Card(
            colors = CardDefaults.cardColors(
                containerColor = WellnessColors.Primary.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WellnessColors.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Down button
        IconButton(
            onClick = {
                val newValue = (value - step).coerceIn(range)
                onValueChange(newValue)
            }
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease $label",
                tint = WellnessColors.Accent
            )
        }
    }
}

@Composable
private fun PresetButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = WellnessColors.Accent,
            containerColor = WellnessColors.Accent.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}