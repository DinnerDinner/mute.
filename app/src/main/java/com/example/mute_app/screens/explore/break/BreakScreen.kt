package com.example.mute_app.screens.explore.`break`

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kotlinx.coroutines.delay
import kotlin.math.*

// Break Screen States
sealed class BreakScreenState {
    object Overview : BreakScreenState()
    object PermissionsSetup : BreakScreenState()
    object StrictModeSetup : BreakScreenState()
    object SelectApps : BreakScreenState()
    object SelectWebsites : BreakScreenState()
    object FinalizeSettings : BreakScreenState()
    object ActiveSession : BreakScreenState()
}

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
    onNavigateToBlocklist: () -> Unit = {},
    viewModel: BreakViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load counts when screen appears
    LaunchedEffect(Unit) {
        viewModel.loadBlocklistCounts()
        viewModel.refreshPermissions()
    }

    // State management
    var currentState by remember { mutableStateOf<BreakScreenState>(BreakScreenState.Overview) }

    // Show permissions setup if needed
    LaunchedEffect(uiState.needsPermissionSetup) {
        if (uiState.needsPermissionSetup) {
            currentState = BreakScreenState.PermissionsSetup
        }
    }

    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error for 3 seconds then clear
            delay(3000)
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
        when (currentState) {
            BreakScreenState.Overview -> {
                OverviewScreen(
                    uiState = uiState,
                    onToggleBlocking = viewModel::toggleSession,
                    onNavigateToBlocklist = onNavigateToBlocklist,
                    onBackClick = onBackClick,
                    onShowPermissions = { currentState = BreakScreenState.PermissionsSetup }
                )
            }

            BreakScreenState.PermissionsSetup -> {
                PermissionsSetupScreen(
                    uiState = uiState,
                    onRequestOverlayPermission = viewModel::requestOverlayPermission,
                    onRequestAccessibilityPermission = viewModel::requestAccessibilityPermission,
                    onRefreshPermissions = viewModel::refreshPermissions,
                    onBackToOverview = { currentState = BreakScreenState.Overview }
                )
            }

            else -> {
                // Placeholder for other states
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Other screens coming soon...", color = WellnessColors.OnSurface)
                }
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

@Composable
private fun PermissionsSetupScreen(
    uiState: com.example.mute_app.viewmodels.explore.`break`.BreakUiState,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    onRefreshPermissions: () -> Unit,
    onBackToOverview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToOverview) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WellnessColors.OnSurface
                )
            }

            Text(
                text = "Permissions Required",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WellnessColors.OnSurface
            )

            Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Main content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = WellnessColors.Surface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = WellnessColors.Accent,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To block apps effectively, mute. needs special permissions:",
                    fontSize = 16.sp,
                    color = WellnessColors.OnSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Overlay Permission
                PermissionItem(
                    title = "Screen Overlay",
                    description = "Allows showing blocking screens over other apps",
                    isGranted = uiState.hasOverlayPermission,
                    onGrant = onRequestOverlayPermission
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Accessibility Permission
                PermissionItem(
                    title = "Accessibility Service",
                    description = "Detects when you open blocked apps",
                    isGranted = uiState.hasAccessibilityPermission,
                    onGrant = onRequestAccessibilityPermission
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Refresh button
                Button(
                    onClick = onRefreshPermissions,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WellnessColors.Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Permissions")
                }

                // Continue button (only show if all permissions granted)
                if (!uiState.needsPermissionSetup) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onBackToOverview,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WellnessColors.Success
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isGranted) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (isGranted) WellnessColors.Success else WellnessColors.Inactive,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

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

        if (!isGranted) {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WellnessColors.Accent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OverviewScreen(
    uiState: com.example.mute_app.viewmodels.explore.`break`.BreakUiState,
    onToggleBlocking: () -> Unit,
    onNavigateToBlocklist: () -> Unit,
    onBackClick: () -> Unit,
    onShowPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header with dynamic counters
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
                text = "break.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = WellnessColors.OnSurface
            )

            // Settings and counters
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Permissions indicator
                if (uiState.needsPermissionSetup) {
                    IconButton(onClick = onShowPermissions) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Permissions needed",
                            tint = WellnessColors.Warning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Apps counter
                CounterChip(
                    count = uiState.selectedAppsCount,
                    icon = Icons.Default.Apps,
                    label = "Apps"
                )
                // Websites counter
                CounterChip(
                    count = uiState.selectedWebsitesCount,
                    icon = Icons.Default.Language,
                    label = "Sites"
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Status indicator with session timer
        if (uiState.isSessionActive) {
            ActiveSessionHeader(currentSessionTime = uiState.timeRemaining)
        } else {
            InactiveHeader(
                onNavigateToBlocklist = onNavigateToBlocklist,
                needsPermissions = uiState.needsPermissionSetup,
                hasBlockedItems = uiState.selectedAppsCount > 0 || uiState.selectedWebsitesCount > 0
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Main control area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            MainControlCircle(
                isActive = uiState.isSessionActive,
                canStart = !uiState.needsPermissionSetup && (uiState.selectedAppsCount > 0 || uiState.selectedWebsitesCount > 0),
                onToggleBlocking = onToggleBlocking
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CounterChip(
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "counter_animation"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (count > 0) WellnessColors.Success.copy(alpha = 0.2f)
            else WellnessColors.Surface.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (count > 0) WellnessColors.Success else WellnessColors.OnSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = animatedCount.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (count > 0) WellnessColors.Success else WellnessColors.OnSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ActiveSessionHeader(currentSessionTime: Long) {
    val minutes = (currentSessionTime / 1000 / 60) % 60
    val seconds = (currentSessionTime / 1000) % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WellnessColors.Success.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Session Active",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WellnessColors.Success
                )
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WellnessColors.OnSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(WellnessColors.Success, CircleShape)
            )
        }
    }
}

@Composable
private fun InactiveHeader(
    onNavigateToBlocklist: () -> Unit,
    needsPermissions: Boolean,
    hasBlockedItems: Boolean
) {
    val headerText = when {
        needsPermissions -> "Permissions Required"
        !hasBlockedItems -> "No Apps Selected"
        else -> "Ready to Focus"
    }

    val subText = when {
        needsPermissions -> "Tap to enable required permissions"
        !hasBlockedItems -> "Tap to select apps to block"
        else -> "Tap to configure blocklist"
    }

    val iconTint = when {
        needsPermissions -> WellnessColors.Warning
        !hasBlockedItems -> WellnessColors.Accent.copy(alpha = 0.7f)
        else -> WellnessColors.Accent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToBlocklist() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = iconTint.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = WellnessColors.Surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = headerText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WellnessColors.OnSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = subText,
                    fontSize = 14.sp,
                    color = iconTint,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                if (needsPermissions) Icons.Default.Warning else Icons.Default.Settings,
                contentDescription = "Configure",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MainControlCircle(
    isActive: Boolean,
    canStart: Boolean,
    onToggleBlocking: () -> Unit
) {
    var pulsePhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                pulsePhase = 1f
                delay(2000)
                pulsePhase = 0f
                delay(1000)
            }
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = 1f + (if (isActive) pulsePhase * 0.03f else 0f),
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // Animated glow effect
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
        ) {
            val color = when {
                isActive -> WellnessColors.Success
                canStart -> WellnessColors.Accent
                else -> WellnessColors.Inactive
            }
            drawCircle(
                color = color.copy(alpha = if (isActive) 0.2f + pulsePhase * 0.2f else 0.1f),
                radius = size.minDimension / 2,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // Main control button
        Card(
            modifier = Modifier
                .size(200.dp)
                .clickable(enabled = isActive || canStart) { onToggleBlocking() }
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = if (isActive) WellnessColors.Success.copy(alpha = 0.4f)
                    else if (canStart) WellnessColors.Primary.copy(alpha = 0.3f)
                    else WellnessColors.Inactive.copy(alpha = 0.2f)
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isActive -> WellnessColors.Success.copy(alpha = 0.9f)
                    canStart -> WellnessColors.Surface
                    else -> WellnessColors.Surface.copy(alpha = 0.5f)
                }
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val iconTint = when {
                        isActive -> Color.White
                        canStart -> WellnessColors.Accent
                        else -> WellnessColors.Inactive
                    }

                    val textColor = when {
                        isActive -> Color.White
                        canStart -> WellnessColors.OnSurface
                        else -> WellnessColors.Inactive
                    }

                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isActive) "Stop blocking" else "Start blocking",
                        modifier = Modifier.size(48.dp),
                        tint = iconTint
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when {
                            isActive -> "stop blocking."
                            canStart -> "start blocking."
                            else -> "setup required."
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}