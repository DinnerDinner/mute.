package com.example.mute_app.screens.explore.`break`

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.viewmodels.explore.`break`.ApplicationsViewModel
import com.example.mute_app.viewmodels.explore.`break`.AppInfo
import kotlinx.coroutines.delay

// Wellness green theme for applications
private val AppWellnessColors = object {
    val Primary = Color(0xFF2E7D5A)
    val Secondary = Color(0xFF4CAF50)
    val Accent = Color(0xFF81C784)
    val Success = Color(0xFF66BB6A)
    val Background = Color(0xFF1B2E1F)
    val Surface = Color(0xFF243428)
    val OnSurface = Color(0xFFE8F5E8)
    val CardSurface = Color(0xFF2D3E31)
    val Selected = Color(0xFF4CAF50)
    val Unselected = Color(0xFF37474F)
}

@Composable
fun ApplicationsScreen(
    onBackClick: () -> Unit,
    viewModel: ApplicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.allApps.isEmpty() && !uiState.isLoading) {
            viewModel.loadInstalledApps()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppWellnessColors.Background,
                        AppWellnessColors.Primary.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Modern header with selection count
            ModernHeader(
                selectedCount = uiState.selectedApps.size,
                totalCount = uiState.filteredApps.size,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search bar
            ModernSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Apps list
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator()
                    }
                    uiState.error != null -> {
                        ErrorMessage(
                            error = uiState.error!!,
                            onRetry = viewModel::loadInstalledApps,
                            onDismiss = viewModel::clearError
                        )
                    }
                    uiState.filteredApps.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                        EmptySearchResults()
                    }
                    else -> {
                        AppsList(
                            apps = uiState.filteredApps,
                            selectedApps = uiState.selectedApps,
                            onToggleSelection = viewModel::toggleAppSelection
                        )
                    }
                }
            }

            // Save button - only show when apps are selected
            AnimatedVisibility(
                visible = uiState.selectedApps.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SaveButton(
                    selectedCount = uiState.selectedApps.size,
                    onSave = {
                        viewModel.saveSelectedApps()
                        onBackClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernHeader(
    selectedCount: Int,
    totalCount: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(
                    AppWellnessColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = AppWellnessColors.OnSurface
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "select apps.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppWellnessColors.OnSurface
            )
            if (totalCount > 0) {
                Text(
                    text = "$selectedCount of $totalCount selected",
                    fontSize = 14.sp,
                    color = AppWellnessColors.Success,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Selection indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (selectedCount > 0) AppWellnessColors.Success.copy(alpha = 0.2f)
                    else AppWellnessColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedCount.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedCount > 0) AppWellnessColors.Success else AppWellnessColors.OnSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = {
            Text(
                "Search apps...",
                color = AppWellnessColors.OnSurface.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = AppWellnessColors.Accent
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = AppWellnessColors.OnSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppWellnessColors.OnSurface,
            unfocusedTextColor = AppWellnessColors.OnSurface.copy(alpha = 0.8f),
            focusedBorderColor = AppWellnessColors.Success,
            unfocusedBorderColor = AppWellnessColors.OnSurface.copy(alpha = 0.3f),
            cursorColor = AppWellnessColors.Success,
            focusedContainerColor = AppWellnessColors.Surface.copy(alpha = 0.3f),
            unfocusedContainerColor = AppWellnessColors.Surface.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
private fun AppsList(
    apps: List<AppInfo>,
    selectedApps: Set<String>,
    onToggleSelection: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(apps) { index, app ->
            AppItem(
                app = app,
                isSelected = selectedApps.contains(app.packageName),
                onToggleSelection = { onToggleSelection(app.packageName) }
            )
        }
    }
}

@Composable
private fun AppItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_press"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isSelected)
            AppWellnessColors.Success.copy(alpha = 0.15f)
        else
            AppWellnessColors.CardSurface.copy(alpha = 0.7f),
        animationSpec = tween(300),
        label = "card_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable {
                isPressed = true
                onToggleSelection()
            }
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) AppWellnessColors.Success.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        LaunchedEffect(isPressed) {
            if (isPressed) {
                delay(100)
                isPressed = false
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            AppIcon(drawable = app.icon)

            Spacer(modifier = Modifier.width(16.dp))

            // App info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppWellnessColors.OnSurface
                )
                Text(
                    text = app.packageName,
                    fontSize = 12.sp,
                    color = AppWellnessColors.OnSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Selection checkbox
            AnimatedSelectionBox(isSelected = isSelected)
        }
    }
}

@Composable
private fun AppIcon(drawable: Drawable?) {
    val bitmap = remember(drawable) {
        try {
            drawable?.toBitmap(56, 56)
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                AppWellnessColors.Surface.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "App icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            DefaultAppIcon()
        }
    }
}


@Composable
private fun DefaultAppIcon() {
    Icon(
        Icons.Default.Android,
        contentDescription = "Default app icon",
        tint = AppWellnessColors.OnSurface.copy(alpha = 0.6f),
        modifier = Modifier.size(28.dp)
    )
}

@Composable
private fun AnimatedSelectionBox(isSelected: Boolean) {
    val checkboxColor by animateColorAsState(
        targetValue = if (isSelected) AppWellnessColors.Success else Color.Transparent,
        animationSpec = tween(200),
        label = "checkbox_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AppWellnessColors.Success else AppWellnessColors.OnSurface.copy(alpha = 0.3f),
        animationSpec = tween(200),
        label = "border_color"
    )

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(checkboxColor, RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.3f),
                        borderColor
                    )
                ),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SaveButton(
    selectedCount: Int,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = AppWellnessColors.Success.copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppWellnessColors.Success
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = "Save",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "save $selectedCount apps.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = AppWellnessColors.Success,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading your apps...",
                fontSize = 16.sp,
                color = AppWellnessColors.OnSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error loading apps",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppWellnessColors.OnSurface
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = AppWellnessColors.OnSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = AppWellnessColors.OnSurface)
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppWellnessColors.Success
                    )
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = "No results",
                tint = AppWellnessColors.OnSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No apps found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = AppWellnessColors.OnSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Try adjusting your search",
                fontSize = 14.sp,
                color = AppWellnessColors.OnSurface.copy(alpha = 0.5f)
            )
        }
    }
}