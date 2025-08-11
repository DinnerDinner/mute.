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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

// Break Screen States (following your phase system)
sealed class BreakScreenState {
    object Overview : BreakScreenState()           // Main dashboard
    object StrictModeSetup : BreakScreenState()    // Phase 4.1 - Layer 1
    object SelectApps : BreakScreenState()         // Phase 4.2 - Layer 2
    object SelectWebsites : BreakScreenState()     // Phase 4.3 - Layer 3
    object FinalizeSettings : BreakScreenState()   // Phase 5 - Layer 4
    object ActiveSession : BreakScreenState()      // Phase 6 - Layer 5
}

// Core color palette for digital discipline theme
private val DigitalDisciplineColors = object {
    val Primary = Color(0xFF1A1A2E)          // Deep navy - focus
    val Secondary = Color(0xFF16213E)        // Darker blue - depth
    val Accent = Color(0xFFE94560)           // Alert red - blocking
    val Success = Color(0xFF0F3460)          // Success blue - achievements
    val Warning = Color(0xFFFF6B35)          // Orange - warnings
    val Surface = Color(0xFF0E1B26)          // Dark surface
    val OnSurface = Color(0xFFE8E9F3)        // Light text
    val Glow = Color(0xFF64FFDA)             // Cyan glow - active states
}

@Composable
fun BreakScreen(
    onBackClick: () -> Unit,
    onNavigateToBlocklist: () -> Unit = {}
) {
    // State management
    var currentState by remember { mutableStateOf<BreakScreenState>(BreakScreenState.Overview) }
    var isStrictModeActive by remember { mutableStateOf(false) }
    var currentStreak by remember { mutableStateOf(7) } // Example streak
    var remainingTime by remember { mutableStateOf(0L) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var selectedWebsites by remember { mutableStateOf(setOf<String>()) }
    var sessionDuration by remember { mutableStateOf(900000L) } // 15 minutes default
    var unlockMethod by remember { mutableStateOf("Password") }
    var advancedSettings by remember { mutableStateOf(mapOf<String, Boolean>()) }

    // Timer countdown effect
    LaunchedEffect(isStrictModeActive, remainingTime) {
        if (isStrictModeActive && remainingTime > 0) {
            while (remainingTime > 0) {
                delay(1000)
                remainingTime -= 1000
            }
            if (remainingTime <= 0) {
                isStrictModeActive = false
                currentStreak++
                currentState = BreakScreenState.Overview
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DigitalDisciplineColors.Primary,
                        DigitalDisciplineColors.Secondary
                    )
                )
            )
    ) {
        when (currentState) {
            BreakScreenState.Overview -> {
                OverviewScreen(
                    streak = currentStreak,
                    isActive = isStrictModeActive,
                    remainingTime = remainingTime,
                    onStartStrictMode = { currentState = BreakScreenState.StrictModeSetup },
                    onBackClick = onBackClick,
                    onNavigateToBlocklist = onNavigateToBlocklist
                )
            }
            BreakScreenState.StrictModeSetup -> {
                StrictModeSetupScreen(
                    selectedAppsCount = selectedApps.size,
                    selectedWebsitesCount = selectedWebsites.size,
                    onSelectApps = { currentState = BreakScreenState.SelectApps },
                    onSelectWebsites = { currentState = BreakScreenState.SelectWebsites },
                    onProceedToFinalize = {
                        if (selectedApps.isNotEmpty() || selectedWebsites.isNotEmpty()) {
                            currentState = BreakScreenState.FinalizeSettings
                        }
                    },
                    onBack = { currentState = BreakScreenState.Overview }
                )
            }
            BreakScreenState.SelectApps -> {
                SelectAppsScreen(
                    selectedApps = selectedApps,
                    onSelectionChanged = { selectedApps = it },
                    onBack = { currentState = BreakScreenState.StrictModeSetup }
                )
            }
            BreakScreenState.SelectWebsites -> {
                SelectWebsitesScreen(
                    selectedWebsites = selectedWebsites,
                    onSelectionChanged = { selectedWebsites = it },
                    onBack = { currentState = BreakScreenState.StrictModeSetup }
                )
            }
            BreakScreenState.FinalizeSettings -> {
                FinalizeSettingsScreen(
                    duration = sessionDuration,
                    unlockMethod = unlockMethod,
                    advancedSettings = advancedSettings,
                    onDurationChanged = { sessionDuration = it },
                    onUnlockMethodChanged = { unlockMethod = it },
                    onAdvancedSettingsChanged = { advancedSettings = it },
                    onActivate = {
                        currentState = BreakScreenState.ActiveSession
                        isStrictModeActive = true
                        remainingTime = sessionDuration
                    },
                    onBack = { currentState = BreakScreenState.StrictModeSetup }
                )
            }
            BreakScreenState.ActiveSession -> {
                ActiveSessionScreen(
                    remainingTime = remainingTime,
                    selectedApps = selectedApps,
                    selectedWebsites = selectedWebsites,
                    unlockMethod = unlockMethod,
                    onSessionComplete = {
                        isStrictModeActive = false
                        remainingTime = 0L
                        currentStreak++
                        currentState = BreakScreenState.Overview
                    },
                    onEmergencyExit = {
                        // Handle emergency exit logic
                        isStrictModeActive = false
                        remainingTime = 0L
                        currentState = BreakScreenState.Overview
                    }
                )
            }
            else -> {
                // Default case - should never happen
                OverviewScreen(
                    streak = currentStreak,
                    isActive = isStrictModeActive,
                    remainingTime = remainingTime,
                    onStartStrictMode = { currentState = BreakScreenState.StrictModeSetup },
                    onNavigateToBlocklist = onNavigateToBlocklist,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Composable
private fun OverviewScreen(
    streak: Int,
    isActive: Boolean,
    remainingTime: Long,
    onStartStrictMode: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToBlocklist: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DigitalDisciplineColors.OnSurface
                )
            }

            Text(
                text = "break.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = DigitalDisciplineColors.OnSurface
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Streak indicator at top
        StreakIndicator(streak = streak)

        Spacer(modifier = Modifier.height(40.dp))

        // Dominant Timer Circle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                ActiveTimerCircle(remainingTime = remainingTime)
            } else {
                IdleTimerCircle(
                    onStartClick = onStartStrictMode,
                    onBlocklistClick = onNavigateToBlocklist
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Quick Actions (only when not active)
        if (!isActive) {
            QuickActionCards(onStartStrictMode = onStartStrictMode)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StreakIndicator(streak: Int) {
    var glowPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            glowPhase = 1f
            delay(2000)
            glowPhase = 0f
            delay(1500)
        }
    }

    val glowAlpha by animateFloatAsState(
        targetValue = glowPhase * 0.4f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "streak_glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DigitalDisciplineColors.Success.copy(alpha = glowAlpha)
            ),
        colors = CardDefaults.cardColors(
            containerColor = DigitalDisciplineColors.Surface.copy(alpha = 0.8f)
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
                    text = "Current Streak",
                    fontSize = 14.sp,
                    color = DigitalDisciplineColors.OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "$streak days",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DigitalDisciplineColors.Glow
                )
            }

            Icon(
                Icons.Default.Whatshot,
                contentDescription = "Streak",
                tint = DigitalDisciplineColors.Warning,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(alpha = 0.8f + glowAlpha)
            )
        }
    }
}

@Composable
private fun IdleTimerCircle(
    onStartClick: () -> Unit,
    onBlocklistClick: () -> Unit
) {
    var pulsePhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            pulsePhase = 1f
            delay(3000)
            pulsePhase = 0f
            delay(2000)
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = 1f + (pulsePhase * 0.05f),
        animationSpec = tween(3000, easing = FastOutSlowInEasing),
        label = "idle_pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(320.dp) // Made bigger to accommodate blocklist area
            .clickable { onBlocklistClick() } // Whole area opens blocklist
    ) {
        // Outer glow ring
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
        ) {
            drawCircle(
                color = DigitalDisciplineColors.Glow.copy(alpha = 0.1f + pulsePhase * 0.2f),
                radius = size.minDimension / 2,
                style = Stroke(width = 8.dp.toPx())
            )
        }

        // Main circle - this is the start button
        Card(
            modifier = Modifier
                .size(240.dp)
                .clickable { onStartClick() } // Only center circle starts session
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = DigitalDisciplineColors.Primary.copy(alpha = 0.5f)
                ),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = DigitalDisciplineColors.Surface.copy(alpha = 0.9f)
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
                        Icons.Default.Shield,
                        contentDescription = "Start Protection",
                        modifier = Modifier.size(48.dp),
                        tint = DigitalDisciplineColors.Glow
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Start\nStrict Mode",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DigitalDisciplineColors.OnSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Blocklist hint text around the circle
        Text(
            text = "Tap around to manage blocklist",
            fontSize = 12.sp,
            color = DigitalDisciplineColors.OnSurface.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
        )
    }
}

@Composable
private fun ActiveTimerCircle(remainingTime: Long) {
    val progress = remember(remainingTime) {
        if (remainingTime > 0) remainingTime.toFloat() / 3600000f else 0f // Assuming max 1 hour
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(280.dp)
    ) {
        // Animated progress ring
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background ring
            drawCircle(
                color = DigitalDisciplineColors.Surface,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Progress ring
            drawArc(
                color = DigitalDisciplineColors.Accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Time display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val minutes = (remainingTime / 1000 / 60) % 60
            val seconds = (remainingTime / 1000) % 60

            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = DigitalDisciplineColors.OnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Remaining",
                fontSize = 14.sp,
                color = DigitalDisciplineColors.OnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun QuickActionCards(onStartStrictMode: () -> Unit) {
    val actions = listOf(
        Triple("Quick Block", "15 min session", Icons.Default.Timer),
        Triple("Focus Mode", "Deep work block", Icons.Default.Psychology),
        Triple("Detox Break", "Social media pause", Icons.Default.SelfImprovement)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(180.dp)
    ) {
        items(actions) { (title, subtitle, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartStrictMode() }
                    .height(50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DigitalDisciplineColors.Surface.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = title,
                            tint = DigitalDisciplineColors.Glow,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = DigitalDisciplineColors.OnSurface
                            )
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = DigitalDisciplineColors.OnSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Start",
                        tint = DigitalDisciplineColors.OnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Placeholder screens for other states (you'll expand these)
@Composable
private fun StrictModeSetupScreen(
    selectedAppsCount: Int,
    selectedWebsitesCount: Int,
    onSelectApps: () -> Unit,
    onSelectWebsites: () -> Unit,
    onProceedToFinalize: () -> Unit,
    onBack: () -> Unit
) {
    // Phase 4.1 implementation - Layer 1
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Strict Mode Setup\nApps: $selectedAppsCount | Sites: $selectedWebsitesCount",
            color = DigitalDisciplineColors.OnSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SelectAppsScreen(
    selectedApps: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onBack: () -> Unit
) {
    // Phase 4.2 implementation - Layer 2
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Select Apps Screen", color = DigitalDisciplineColors.OnSurface)
    }
}

@Composable
private fun SelectWebsitesScreen(
    selectedWebsites: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onBack: () -> Unit
) {
    // Phase 4.3 implementation - Layer 3
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Select Websites Screen", color = DigitalDisciplineColors.OnSurface)
    }
}

@Composable
private fun FinalizeSettingsScreen(
    duration: Long,
    unlockMethod: String,
    advancedSettings: Map<String, Boolean>,
    onDurationChanged: (Long) -> Unit,
    onUnlockMethodChanged: (String) -> Unit,
    onAdvancedSettingsChanged: (Map<String, Boolean>) -> Unit,
    onActivate: () -> Unit,
    onBack: () -> Unit
) {
    // Phase 5 implementation - Layer 4
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Finalize Settings Screen", color = DigitalDisciplineColors.OnSurface)
    }
}

@Composable
private fun ActiveSessionScreen(
    remainingTime: Long,
    selectedApps: Set<String>,
    selectedWebsites: Set<String>,
    unlockMethod: String,
    onSessionComplete: () -> Unit,
    onEmergencyExit: () -> Unit
) {
    // Phase 6 implementation - Layer 5
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Active Session Screen", color = DigitalDisciplineColors.OnSurface)
    }
}






//package com.example.mute_app.screens.explore.`break`
//
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//
//@Composable
//fun BreakScreen(
//    onNavigateToBlocklist: () -> Unit,
//    onBackClick: () -> Unit
//) {
//    var isBlockingActive by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFF0A0A0A), // Deep black
//                        Color(0xFF1A1A2E)  // Dark navy
//                    )
//                )
//            )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp)
//        ) {
//            // Header with back button and title
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = onBackClick) {
//                    Icon(
//                        Icons.Default.ArrowBack,
//                        contentDescription = "Back",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//
//                // App title with icon (inspired by your first image)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Default.Shield,
//                        contentDescription = "break.",
//                        tint = Color(0xFF00BCD4), // Cyan blue
//                        modifier = Modifier.size(28.dp)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "break.",
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color(0xFF00BCD4)
//                    )
//                }
//
//                // Settings icon (placeholder)
//                IconButton(onClick = { /* TODO: Settings */ }) {
//                    Icon(
//                        Icons.Default.Settings,
//                        contentDescription = "Settings",
//                        tint = Color.White.copy(alpha = 0.6f),
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            // Quick Block Widget (main feature based on your first image)
//            QuickBlockWidget(
//                isActive = isBlockingActive,
//                onToggleBlocking = { isBlockingActive = !isBlockingActive },
//                onConfigureBlocklist = onNavigateToBlocklist
//            )
//
//            Spacer(modifier = Modifier.height(40.dp))
//
//            // TODO: Additional features will go here later
//            // - Timer settings
//            // - Advanced options
//            // - Statistics/streaks
//        }
//    }
//}
//
//@Composable
//private fun QuickBlockWidget(
//    isActive: Boolean,
//    onToggleBlocking: () -> Unit,
//    onConfigureBlocklist: () -> Unit
//) {
//    var pulsePhase by remember { mutableStateOf(0f) }
//
//    // Pulsing animation for active state
//    LaunchedEffect(isActive) {
//        if (isActive) {
//            while (isActive) {
//                pulsePhase = 1f
//                delay(1500)
//                pulsePhase = 0f
//                delay(1500)
//            }
//        }
//    }
//
//    val pulseAlpha by animateFloatAsState(
//        targetValue = if (isActive) pulsePhase * 0.3f else 0f,
//        animationSpec = tween(1500, easing = FastOutSlowInEasing),
//        label = "pulse_animation"
//    )
//
//    // Main Quick Block container (inspired by your first image)
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .clickable { onConfigureBlocklist() } // Clicking anywhere opens blocklist
//            .shadow(
//                elevation = 12.dp,
//                shape = RoundedCornerShape(20.dp),
//                spotColor = if (isActive) Color(0xFF00BCD4).copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)
//            ),
//        shape = RoundedCornerShape(20.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFF2A2A3E).copy(alpha = 0.9f)
//        )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .border(
//                    width = 2.dp,
//                    brush = Brush.horizontalGradient(
//                        colors = listOf(
//                            Color(0xFF00BCD4).copy(alpha = 0.6f + pulseAlpha),
//                            Color(0xFF0097A7).copy(alpha = 0.4f + pulseAlpha)
//                        )
//                    ),
//                    shape = RoundedCornerShape(20.dp)
//                )
//                .padding(24.dp)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                // Header section
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "break now.",
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White
//                    )
//
//                    // Timer and Globe icons (from your first image)
//                    Row {
//                        Icon(
//                            Icons.Default.Timer,
//                            contentDescription = "Timer",
//                            tint = Color.White.copy(alpha = 0.6f),
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                Icons.Default.Language,
//                                contentDescription = "Websites",
//                                tint = Color.White.copy(alpha = 0.6f),
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Text(
//                                text = "3",
//                                fontSize = 14.sp,
//                                color = Color.White.copy(alpha = 0.6f),
//                                modifier = Modifier.padding(start = 4.dp)
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.weight(1f))
//
//                Button(
//                    onClick = onToggleBlocking,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (isActive) Color(0xFF4CAF50) else Color(0xFF424242)
//                    ),
//                    shape = RoundedCornerShape(16.dp),
//                    elevation = ButtonDefaults.buttonElevation(
//                        defaultElevation = if (isActive) 8.dp else 4.dp
//                    )
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Icon(
//                            if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
//                            contentDescription = if (isActive) "Stop" else "Start",
//                            tint = Color.White,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = if (isActive) "Stop" else "Start",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Active status indicator (from your first image)
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    // Status indicator circle
//                    Box(
//                        modifier = Modifier
//                            .size(16.dp)
//                            .background(
//                                color = if (isActive) Color(0xFF4CAF50) else Color(0xFF757575),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .border(
//                                width = 2.dp,
//                                color = if (isActive) Color(0xFF66BB6A).copy(alpha = 0.5f + pulseAlpha) else Color.Transparent,
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                    )
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    Text(
//                        text = if (isActive) "Active" else "Inactive",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = if (isActive) Color(0xFF00BCD4) else Color.White.copy(alpha = 0.7f)
//                    )
//                }
//            }
//        }
//    }
//
//    // Subtle hint text
//    Spacer(modifier = Modifier.height(16.dp))
//    Text(
//        text = "Tap anywhere on the card to configure your blocklist",
//        fontSize = 14.sp,
//        color = Color.White.copy(alpha = 0.5f),
//        textAlign = TextAlign.Center,
//        modifier = Modifier.fillMaxWidth()
//    )
//}
//
//
//
//
//
//
//
//
