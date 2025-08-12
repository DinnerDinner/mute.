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
    }

    // State management
    var currentState by remember { mutableStateOf<BreakScreenState>(BreakScreenState.Overview) }

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
                    onBackClick = onBackClick
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
    }
}

@Composable
private fun OverviewScreen(
    uiState: com.example.mute_app.viewmodels.explore.`break`.BreakUiState,
    onToggleBlocking: () -> Unit,
    onNavigateToBlocklist: () -> Unit,
    onBackClick: () -> Unit
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

            // Dynamic counters
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

        // Status indicator with session timer - CLICKABLE HEADER
        if (uiState.isSessionActive) {
            ActiveSessionHeader(currentSessionTime = uiState.timeRemaining)
        } else {
            InactiveHeader(onNavigateToBlocklist = onNavigateToBlocklist) // Made clickable
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Main control area - ONLY TIMER CONTROL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            MainControlCircle(
                isActive = uiState.isSessionActive,
                onToggleBlocking = onToggleBlocking // Only toggles blocking, no navigation
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
private fun InactiveHeader(onNavigateToBlocklist: () -> Unit) {
    // Make this entire card clickable and elevated
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToBlocklist() }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = WellnessColors.Accent.copy(alpha = 0.3f)
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
                    text = "Ready to Focus",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WellnessColors.OnSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "Tap to configure blocklist",
                    fontSize = 14.sp,
                    color = WellnessColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Arrow to indicate it's clickable
            Icon(
                Icons.Default.Settings,
                contentDescription = "Configure",
                tint = WellnessColors.Accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MainControlCircle(
    isActive: Boolean,
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
            val color = if (isActive) WellnessColors.Success else WellnessColors.Accent
            drawCircle(
                color = color.copy(alpha = if (isActive) 0.2f + pulsePhase * 0.2f else 0.1f),
                radius = size.minDimension / 2,
                style = Stroke(width = 6.dp.toPx())
            )
        }

        // Main control button - ONLY FOR TIMER CONTROL
        Card(
            modifier = Modifier
                .size(200.dp)
                .clickable { onToggleBlocking() }
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = if (isActive) WellnessColors.Success.copy(alpha = 0.4f) else WellnessColors.Primary.copy(alpha = 0.3f)
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = if (isActive) WellnessColors.Success.copy(alpha = 0.9f) else WellnessColors.Surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isActive) "Stop blocking" else "Start blocking",
                        modifier = Modifier.size(48.dp),
                        tint = if (isActive) Color.White else WellnessColors.Accent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isActive) "stop blocking." else "start blocking.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) Color.White else WellnessColors.OnSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}