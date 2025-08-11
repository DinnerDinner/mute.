package com.example.mute_app.screens.explore.`break`

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.regex.Pattern

@Composable
fun WebsitesScreen(
    onBackClick: () -> Unit
) {
    var blockedWebsites by remember {
        mutableStateOf(
            mutableListOf("4chan.org", "reddit.com", "youtube.com") // Default from your image
        )
    }
    var urlInput by remember { mutableStateOf("") }
    var isUrlValid by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // URL validation function
    fun isValidUrl(url: String): Boolean {
        val urlPattern = Pattern.compile(
            "^(https?://)?" + // Optional protocol
                    "([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}" + // Domain
                    "(/.*)?$" // Optional path
        )
        return urlPattern.matcher(url.trim()).matches()
    }

    // Clean URL function (remove protocol, www, etc.)
    fun cleanUrl(url: String): String {
        return url.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .lowercase()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0A), // Deep black
                        Color(0xFF1A1A2E)  // Dark navy
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Block Websites",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "${blockedWebsites.size} sites",
                    fontSize = 14.sp,
                    color = Color(0xFF00BCD4),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // URL input section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color.Black.copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2A3E).copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Add Website to Block",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Text(
                        text = "Enter a domain (e.g., reddit.com, youtube.com)",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // URL Input field
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            showError = false
                            isUrlValid = true
                        },
                        label = { Text("Website URL", color = Color.White.copy(alpha = 0.7f)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = "Website",
                                tint = if (isUrlValid) Color.White.copy(alpha = 0.7f) else Color(0xFFE57373)
                            )
                        },
                        trailingIcon = {
                            if (urlInput.isNotEmpty()) {
                                Row {
                                    if (isValidUrl(urlInput)) {
                                        IconButton(
                                            onClick = {
                                                val cleanedUrl = cleanUrl(urlInput)
                                                if (!blockedWebsites.contains(cleanedUrl)) {
                                                    blockedWebsites.add(cleanedUrl)
                                                    urlInput = ""
                                                    keyboardController?.hide()
                                                } else {
                                                    showError = true
                                                    isUrlValid = false
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { urlInput = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                            focusedBorderColor = if (isUrlValid) Color(0xFF00BCD4) else Color(0xFFE57373),
                            unfocusedBorderColor = if (isUrlValid) Color.White.copy(alpha = 0.3f) else Color(0xFFE57373),
                            cursorColor = Color(0xFF00BCD4),
                            errorBorderColor = Color(0xFFE57373)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isValidUrl(urlInput)) {
                                    val cleanedUrl = cleanUrl(urlInput)
                                    if (!blockedWebsites.contains(cleanedUrl)) {
                                        blockedWebsites.add(cleanedUrl)
                                        urlInput = ""
                                        keyboardController?.hide()
                                    } else {
                                        showError = true
                                        isUrlValid = false
                                    }
                                } else {
                                    isUrlValid = false
                                    showError = true
                                }
                            }
                        ),
                        isError = !isUrlValid
                    )

                    // Error message
                    AnimatedVisibility(
                        visible = showError && !isUrlValid,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Text(
                            text = if (blockedWebsites.contains(cleanUrl(urlInput))) {
                                "This website is already in your blocklist"
                            } else {
                                "Please enter a valid website URL"
                            },
                            fontSize = 12.sp,
                            color = Color(0xFFE57373),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Quick add suggestions
                    if (urlInput.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quick Add:",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val suggestions = listOf("facebook.com", "instagram.com", "twitter.com", "tiktok.com")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            suggestions.take(2).forEach { suggestion ->
                                if (!blockedWebsites.contains(suggestion)) {
                                    SuggestionChip(
                                        onClick = {
                                            blockedWebsites.add(suggestion)
                                        },
                                        label = {
                                            Text(
                                                text = suggestion,
                                                fontSize = 12.sp,
                                                color = Color(0xFF00BCD4)
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = Color(0xFF00BCD4).copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Blocked websites list
            if (blockedWebsites.isNotEmpty()) {
                Text(
                    text = "Blocked Websites (${blockedWebsites.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(blockedWebsites) { index, website ->
                        var itemVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(website) {
                            delay(index * 30L) // Staggered animation
                            itemVisible = true
                        }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessHigh
                                )
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { -it / 4 },
                                animationSpec = tween(200)
                            ) + fadeOut()
                        ) {
                            WebsiteItem(
                                website = website,
                                onDelete = {
                                    blockedWebsites.remove(website)
                                }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "No websites",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No websites blocked yet",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            AnimatedVisibility(
                visible = blockedWebsites.isNotEmpty(),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn()
            ) {
                Button(
                    onClick = {
                        // TODO: Save blocked websites locally (SharedPreferences/DataStore)
                        println("DEBUG: Saving ${blockedWebsites.size} blocked websites: $blockedWebsites")
                        onBackClick() // Go back after saving
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save ${blockedWebsites.size} Websites",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsiteItem(
    website: String,
    onDelete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A3E).copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = "Website",
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = website,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            IconButton(
                onClick = {
                    isPressed = true
                    onDelete()
                }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}