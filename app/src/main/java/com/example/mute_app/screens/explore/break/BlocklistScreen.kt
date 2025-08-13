package com.example.mute_app.screens.explore.`break`

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.mute_app.viewmodels.explore.`break`.BlocklistViewModel
import com.example.mute_app.viewmodels.explore.`break`.WebsitesViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer

// Wellness theme for blocklist
private val BlocklistWellnessColors = object {
    val Primary = Color(0xFF2E7D5A)
    val Secondary = Color(0xFF4CAF50)
    val Accent = Color(0xFF81C784)
    val Success = Color(0xFF66BB6A)
    val Background = Color(0xFF1B2E1F)
    val Surface = Color(0xFF243428)
    val OnSurface = Color(0xFFE8F5E8)
    val CardSurface = Color(0xFF2D3E31)
    val Glow = Color(0xFF4CAF50)
}

@Composable
fun BlocklistScreen(
    onNavigateToApplications: () -> Unit,
    onNavigateToWebsites: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: BlocklistViewModel = hiltViewModel(),
    websitesViewModel: WebsitesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val websitesUiState by websitesViewModel.uiState.collectAsState()

    // Refresh data when screen appears
    LaunchedEffect(Unit) {
        viewModel.refreshCounts()
    }

    // Calculate website count from websitesViewModel
    val websitesCount = websitesUiState.selectedWebsites.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BlocklistWellnessColors.Background,
                        BlocklistWellnessColors.Primary.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Modern header
            ModernBlocklistHeader(onBackClick = onBackClick)

            Spacer(modifier = Modifier.height(40.dp))

            // Category cards
            CategorySection(
                appsCount = uiState.selectedAppsCount,
                websitesCount = websitesCount,
                blockedWebsites = websitesUiState.selectedWebsites,
                onNavigateToApplications = onNavigateToApplications,
                onNavigateToWebsites = onNavigateToWebsites
            )

            Spacer(modifier = Modifier.weight(1f))

            // Summary footer
            if (uiState.selectedAppsCount > 0 || websitesCount > 0) {
                SummaryFooter(
                    appsCount = uiState.selectedAppsCount,
                    websitesCount = websitesCount
                )
            }
        }
    }
}

@Composable
private fun ModernBlocklistHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(
                    BlocklistWellnessColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = BlocklistWellnessColors.OnSurface
            )
        }

        Text(
            text = "blocklist.",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BlocklistWellnessColors.OnSurface
        )

        IconButton(
            onClick = { /* TODO: Settings */ },
            modifier = Modifier
                .background(
                    BlocklistWellnessColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = BlocklistWellnessColors.OnSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CategorySection(
    appsCount: Int,
    websitesCount: Int,
    blockedWebsites: Set<String>,
    onNavigateToApplications: () -> Unit,
    onNavigateToWebsites: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Apps category
        AnimatedCategoryCard(
            title = "Applications",
            description = "Block distracting mobile apps",
            icon = Icons.Default.Apps,
            count = appsCount,
            onClick = onNavigateToApplications,
            animationDelay = 0L
        )

        // Websites category
        AnimatedCategoryCard(
            title = "Websites",
            description = "Block distracting websites",
            icon = Icons.Default.Language,
            count = websitesCount,
            onClick = onNavigateToWebsites,
            animationDelay = 150L
        )
    }
}

@Composable
private fun AnimatedCategoryCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    onClick: () -> Unit,
    animationDelay: Long
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(400))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .clickable {
                    isPressed = true
                    onClick()
                }
                .shadow(
                    elevation = if (count > 0) 12.dp else 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (count > 0)
                        BlocklistWellnessColors.Success.copy(alpha = 0.3f)
                    else
                        Color.Black.copy(alpha = 0.1f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (count > 0)
                    BlocklistWellnessColors.Success.copy(alpha = 0.1f)
                else
                    BlocklistWellnessColors.CardSurface.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            LaunchedEffect(isPressed) {
                if (isPressed) {
                    delay(150)
                    isPressed = false
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Icon
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = if (count > 0) BlocklistWellnessColors.Success else BlocklistWellnessColors.Accent,
                        modifier = Modifier.size(32.dp)
                    )

                    // Title and description
                    Column {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlocklistWellnessColors.OnSurface
                        )
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = BlocklistWellnessColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Count and arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Count badge
                    Text(
                        text = count.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (count > 0) BlocklistWellnessColors.Success else BlocklistWellnessColors.OnSurface.copy(alpha = 0.6f)
                    )

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Navigate",
                        tint = BlocklistWellnessColors.OnSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryFooter(
    appsCount: Int,
    websitesCount: Int
) {
    var footerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600)
        footerVisible = true
    }

    AnimatedVisibility(
        visible = footerVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = BlocklistWellnessColors.Success.copy(alpha = 0.2f)
                ),
            colors = CardDefaults.cardColors(
                containerColor = BlocklistWellnessColors.Success.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp)
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
                        text = "Blocklist Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlocklistWellnessColors.OnSurface
                    )
                    Text(
                        text = "You're blocking ${appsCount + websitesCount} items",
                        fontSize = 14.sp,
                        color = BlocklistWellnessColors.OnSurface.copy(alpha = 0.7f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (appsCount > 0) {
                        SummaryItem(
                            count = appsCount,
                            label = "apps",
                            icon = Icons.Default.Apps
                        )
                    }

                    if (websitesCount > 0) {
                        SummaryItem(
                            count = websitesCount,
                            label = "sites",
                            icon = Icons.Default.Language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    count: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .background(
                BlocklistWellnessColors.Success.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = BlocklistWellnessColors.Success,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$count $label",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BlocklistWellnessColors.Success
        )
    }
}