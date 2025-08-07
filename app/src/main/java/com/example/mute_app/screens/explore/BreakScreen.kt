package com.example.mute_app.screens.explore

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.viewmodels.explore.BreakViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun BreakScreen(
    onBackClick: () -> Unit,
    viewModel: BreakViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Animated background elements
    var backgroundPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            backgroundPhase += 0.02f
            delay(50)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0FBF0), // Soft sage green
                        Color(0xFFE8F5E8), // Lighter sage
                        Color(0xFFE1E5D7)  // Warm beige-green
                    )
                )
            )
    ) {
        // Floating meditation dots background
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val dotCount = 15
            repeat(dotCount) { index ->
                val x = (index * 0.618f * size.width) % size.width
                val y = (index * 0.382f * size.height +
                        sin(backgroundPhase + index) * 20.dp.toPx()) % size.height

                drawCircle(
                    color = Color(0xFF4A5D23).copy(
                        alpha = 0.1f + sin(backgroundPhase + index * 0.5f) * 0.05f
                    ),
                    radius = (4 + sin(backgroundPhase + index * 0.3f) * 2).dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Header
            BreakHeader(
                onBackClick = onBackClick,
                isSessionActive = uiState.isSessionActive
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main session display
            BreakSessionCard(
                uiState = uiState,
                onStartPause = viewModel::toggleSession,
                onStop = viewModel::stopSession,
                onModeChange = viewModel::changeMode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick actions
            QuickActionsSection(viewModel)

            Spacer(modifier = Modifier.height(24.dp))

            // Stats overview
            StatsOverviewSection(uiState)

            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding
        }
    }
}

@Composable
private fun BreakHeader(
    onBackClick: () -> Unit,
    isSessionActive: Boolean
) {
    var headerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        headerVisible = true
    }

    AnimatedVisibility(
        visible = headerVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(800))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF4A5D23),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "break.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )

                val statusColor = if (isSessionActive) Color(0xFFE68C3A) else Color(0xFF4A5D23)
                val statusText = if (isSessionActive) "ACTIVE" else "READY"

                Text(
                    text = statusText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    letterSpacing = 1.sp
                )
            }

            IconButton(
                onClick = { /* Settings */ },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF4A5D23),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BreakSessionCard(
    uiState: com.example.mute_app.viewmodels.explore.BreakUiState,
    onStartPause: () -> Unit,
    onStop: () -> Unit,
    onModeChange: (String) -> Unit
) {
    var cardVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600)
        cardVisible = true
    }

    AnimatedVisibility(
        visible = cardVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            initialScale = 0.9f
        ) + fadeIn(tween(800))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFF4A5D23).copy(alpha = 0.25f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mode selector
                ModeSelector(
                    currentMode = uiState.currentMode,
                    onModeChange = onModeChange
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Timer display
                TimerDisplay(
                    timeRemaining = uiState.timeRemaining,
                    totalTime = uiState.totalTime,
                    isActive = uiState.isSessionActive
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Control buttons
                ControlButtons(
                    isActive = uiState.isSessionActive,
                    onStartPause = onStartPause,
                    onStop = onStop
                )
            }
        }
    }
}

@Composable
private fun ModeSelector(
    currentMode: String,
    onModeChange: (String) -> Unit
) {
    val modes = listOf("Focus", "Break", "Deep Work")

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { mode ->
            val isSelected = currentMode == mode

            FilterChip(
                onClick = { onModeChange(mode) },
                label = {
                    Text(
                        text = mode,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = isSelected,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4A5D23),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFFF0FBF0),
                    labelColor = Color(0xFF4A5D23)
                )
            )
        }
    }
}

@Composable
private fun TimerDisplay(
    timeRemaining: Long,
    totalTime: Long,
    isActive: Boolean
) {
    val minutes = (timeRemaining / 1000) / 60
    val seconds = (timeRemaining / 1000) % 60
    val progress = if (totalTime > 0) (totalTime - timeRemaining).toFloat() / totalTime.toFloat() else 0f

    // Pulsing animation for active state
    var pulsePhase by remember { mutableStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (isActive) {
                pulsePhase += 0.05f
                delay(50)
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1f + sin(pulsePhase) * 0.02f else 1f,
        animationSpec = tween(100),
        label = "timer_pulse"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Background circle
            drawCircle(
                color = Color(0xFFF0FBF0),
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
            )

            // Progress arc
            drawArc(
                color = if (isActive) Color(0xFFE68C3A) else Color(0xFF4A5D23),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }

        // Time text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            Text(
                text = if (isActive) "ACTIVE" else "PAUSED",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Color(0xFFE68C3A) else Color(0xFF4A5D23),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun ControlButtons(
    isActive: Boolean,
    onStartPause: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start/Pause button
        FloatingActionButton(
            onClick = onStartPause,
            modifier = Modifier
                .size(64.dp)
                .shadow(8.dp, CircleShape),
            containerColor = if (isActive) Color(0xFFE68C3A) else Color(0xFF4A5D23),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isActive) "Pause" else "Start",
                modifier = Modifier.size(28.dp)
            )
        }

        // Stop button
        FloatingActionButton(
            onClick = onStop,
            modifier = Modifier
                .size(48.dp)
                .shadow(6.dp, CircleShape),
            containerColor = Color(0xFF60212E),
            contentColor = Color.White
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun QuickActionsSection(viewModel: BreakViewModel) {
    var sectionVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1000)
        sectionVisible = true
    }

    AnimatedVisibility(
        visible = sectionVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(tween(800))
    ) {
        Column {
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "App Block",
                    description = "Block distracting apps",
                    color = Color(0xFFE68C3A),
                    onClick = { viewModel.toggleAppBlock() },
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Focus Mode",
                    description = "Enable deep focus",
                    color = Color(0xFF4A5D23),
                    onClick = { viewModel.toggleFocusMode() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = color.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2D2D)
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun StatsOverviewSection(uiState: com.example.mute_app.viewmodels.explore.BreakUiState) {
    var statsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        statsVisible = true
    }

    AnimatedVisibility(
        visible = statsVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(tween(800))
    ) {
        Column {
            Text(
                text = "Today's Progress",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = "${uiState.sessionsCompleted}",
                    label = "Sessions",
                    color = Color(0xFF4A5D23),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    value = "${uiState.totalFocusTime / 60}m",
                    label = "Focus Time",
                    color = Color(0xFFE68C3A),
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    value = "${uiState.streakDays}",
                    label = "Day Streak",
                    color = Color(0xFF60212E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = color.copy(alpha = 0.3f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}