package com.example.mute_app.screens.explore.`break`

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.viewmodels.explore.`break`.WebsitesViewModel
import com.example.mute_app.viewmodels.explore.`break`.WebsiteInfo
import kotlinx.coroutines.delay

// Pastel pink theme for websites
private val WebsitePastelColors = object {
    val Primary = Color(0xFFE91E63)
    val Secondary = Color(0xFFF06292)
    val Accent = Color(0xFFFFAB91)
    val Success = Color(0xFFE91E63)
    val Background = Color(0xFF2E1B2E)
    val Surface = Color(0xFF3E2B3E)
    val OnSurface = Color(0xFFFDE7F3)
    val CardSurface = Color(0xFF4A3249)
    val Selected = Color(0xFFE91E63)
    val Unselected = Color(0xFF6D4C41)
    val InputBackground = Color(0xFF3E2B3E)
    val Error = Color(0xFFEF5350)
}

@Composable
fun WebsitesScreen(
    onBackClick: () -> Unit,
    viewModel: WebsitesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WebsitePastelColors.Background,
                        WebsitePastelColors.Primary.copy(alpha = 0.2f)
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
                selectedCount = uiState.selectedWebsites.size,
                totalCount = uiState.filteredWebsites.size,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // URL input section
            UrlInputCard(
                urlInput = uiState.urlInput,
                isUrlValid = uiState.isUrlValid,
                showError = uiState.showError,
                errorMessage = uiState.errorMessage,
                onUrlInputChange = viewModel::updateUrlInput,
                onAddWebsite = viewModel::addWebsite,
                onClearInput = viewModel::clearUrlInput,
                focusRequester = focusRequester,
                keyboardController = keyboardController
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Search bar
            ModernSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Websites list
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        LoadingIndicator()
                    }
                    uiState.error != null -> {
                        ErrorMessage(
                            error = uiState.error!!,
                            onRetry = { /* Reload if needed */ },
                            onDismiss = viewModel::clearError
                        )
                    }
                    uiState.filteredWebsites.isEmpty() && uiState.searchQuery.isNotBlank() -> {
                        EmptySearchResults()
                    }
                    else -> {
                        WebsitesList(
                            websites = uiState.filteredWebsites,
                            selectedWebsites = uiState.selectedWebsites,
                            onToggleSelection = viewModel::toggleWebsiteSelection,
                            onRemoveWebsite = viewModel::removeWebsite
                        )
                    }
                }
            }

            // Save button - only show when websites are selected
            AnimatedVisibility(
                visible = uiState.selectedWebsites.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                SaveButton(
                    selectedCount = uiState.selectedWebsites.size,
                    onSave = {
                        viewModel. saveSelectedWebsites()
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
                    WebsitePastelColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = WebsitePastelColors.OnSurface
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "block websites.",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WebsitePastelColors.OnSurface
            )
            if (totalCount > 0) {
                Text(
                    text = "$selectedCount of $totalCount selected",
                    fontSize = 14.sp,
                    color = WebsitePastelColors.Success,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Selection indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (selectedCount > 0) WebsitePastelColors.Success.copy(alpha = 0.2f)
                    else WebsitePastelColors.Surface.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedCount.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedCount > 0) WebsitePastelColors.Success else WebsitePastelColors.OnSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun UrlInputCard(
    urlInput: String,
    isUrlValid: Boolean,
    showError: Boolean,
    errorMessage: String,
    onUrlInputChange: (String) -> Unit,
    onAddWebsite: () -> Unit,
    onClearInput: () -> Unit,
    focusRequester: FocusRequester,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = WebsitePastelColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = WebsitePastelColors.CardSurface.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Add Website to Block",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WebsitePastelColors.OnSurface
            )

            Text(
                text = "Enter a domain (e.g., reddit.com, youtube.com)",
                fontSize = 14.sp,
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // URL Input field
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlInputChange,
                label = {
                    Text(
                        "Website URL",
                        color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Website",
                        tint = if (isUrlValid) WebsitePastelColors.Accent else WebsitePastelColors.Error
                    )
                },
                trailingIcon = {
                    if (urlInput.isNotEmpty()) {
                        Row {
                            IconButton(
                                onClick = onAddWebsite
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add",
                                    tint = WebsitePastelColors.Success
                                )
                            }
                            IconButton(onClick = onClearInput) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WebsitePastelColors.OnSurface,
                    unfocusedTextColor = WebsitePastelColors.OnSurface.copy(alpha = 0.8f),
                    focusedBorderColor = if (isUrlValid) WebsitePastelColors.Success else WebsitePastelColors.Error,
                    unfocusedBorderColor = if (isUrlValid) WebsitePastelColors.OnSurface.copy(alpha = 0.3f) else WebsitePastelColors.Error,
                    cursorColor = WebsitePastelColors.Success,
                    focusedContainerColor = WebsitePastelColors.InputBackground.copy(alpha = 0.3f),
                    unfocusedContainerColor = WebsitePastelColors.InputBackground.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onAddWebsite()
                        keyboardController?.hide()
                    }
                ),
                isError = !isUrlValid,
                singleLine = true
            )

            // Error message
            AnimatedVisibility(
                visible = showError && !isUrlValid,
                enter = fadeIn() + slideInVertically()
            ) {
                Text(
                    text = errorMessage,
                    fontSize = 12.sp,
                    color = WebsitePastelColors.Error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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
                "Search websites...",
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = WebsitePastelColors.Accent
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = WebsitePastelColors.OnSurface,
            unfocusedTextColor = WebsitePastelColors.OnSurface.copy(alpha = 0.8f),
            focusedBorderColor = WebsitePastelColors.Success,
            unfocusedBorderColor = WebsitePastelColors.OnSurface.copy(alpha = 0.3f),
            cursorColor = WebsitePastelColors.Success,
            focusedContainerColor = WebsitePastelColors.Surface.copy(alpha = 0.3f),
            unfocusedContainerColor = WebsitePastelColors.Surface.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
private fun WebsitesList(
    websites: List<WebsiteInfo>,
    selectedWebsites: Set<String>,
    onToggleSelection: (String) -> Unit,
    onRemoveWebsite: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(websites) { index, website ->
            WebsiteItem(
                website = website,
                isSelected = selectedWebsites.contains(website.url),
                onToggleSelection = { onToggleSelection(website.url) },
                onRemoveWebsite = if (website.isCustom) { { onRemoveWebsite(website.url) } } else null
            )
        }
    }
}

@Composable
private fun WebsiteItem(
    website: WebsiteInfo,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    onRemoveWebsite: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_press"
    )

    val cardColor by animateColorAsState(
        targetValue = if (isSelected)
            WebsitePastelColors.Success.copy(alpha = 0.15f)
        else
            WebsitePastelColors.CardSurface.copy(alpha = 0.7f),
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
                spotColor = if (isSelected) WebsitePastelColors.Success.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
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
            // Website icon
            WebsiteIcon()

            Spacer(modifier = Modifier.width(16.dp))

            // Website info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = website.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WebsitePastelColors.OnSurface
                )
                Text(
                    text = website.url,
                    fontSize = 12.sp,
                    color = WebsitePastelColors.OnSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Remove button for custom websites
            if (onRemoveWebsite != null) {
                IconButton(
                    onClick = onRemoveWebsite,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = WebsitePastelColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Selection checkbox
            AnimatedSelectionBox(isSelected = isSelected)
        }
    }
}

@Composable
private fun WebsiteIcon() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                WebsitePastelColors.Surface.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Language,
            contentDescription = "Website icon",
            tint = WebsitePastelColors.Accent,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun AnimatedSelectionBox(isSelected: Boolean) {
    val checkboxColor by animateColorAsState(
        targetValue = if (isSelected) WebsitePastelColors.Success else Color.Transparent,
        animationSpec = tween(200),
        label = "checkbox_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) WebsitePastelColors.Success else WebsitePastelColors.OnSurface.copy(alpha = 0.3f),
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
                spotColor = WebsitePastelColors.Success.copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = WebsitePastelColors.Success
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
                text = "save $selectedCount websites.",
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
                color = WebsitePastelColors.Success,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Loading websites...",
                fontSize = 16.sp,
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
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
            containerColor = WebsitePastelColors.Error.copy(alpha = 0.1f)
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
                tint = WebsitePastelColors.Error,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error loading websites",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WebsitePastelColors.OnSurface
            )
            Text(
                text = error,
                fontSize = 14.sp,
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss", color = WebsitePastelColors.OnSurface)
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WebsitePastelColors.Success
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
                tint = WebsitePastelColors.OnSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "No websites found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Try adjusting your search",
                fontSize = 14.sp,
                color = WebsitePastelColors.OnSurface.copy(alpha = 0.5f)
            )
        }
    }
}