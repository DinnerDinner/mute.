package com.example.mute_app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.mute_app.viewmodels.ExploreViewModel
import com.example.mute_app.viewmodels.ExploreCard
import kotlinx.coroutines.delay

// Professional wellness color palette with royal tones
private val WellnessColors = listOf(
    0xFFF0FBF0, // Aloe - soft sage green
    0xFFF5F3E9, // Desert Sand - warm beige
    0xFFEBE9E0, // Mist - light gray-beige
    0xFFE1E5D7, // Teak - warm brown-green
    0xFFCDC8BD, // Army - muted olive
    0xFF817C68, // Cactus - deeper sage
    0xFFDFD3C3, // Warm cream
    0xFFD4C4A8  // Sandy beige
)

private val AccentColors = listOf(
    0xFFE68C3A, // Warm orange
    0xFF60212E, // Deep burgundy
    0xFF4A5D23, // Forest green
    0xFF8B4513, // Saddle brown
    0xFF556B2F, // Dark olive
    0xFF8B4A42, // Dark salmon
    0xFF6B4423, // Dark wood
    0xFF5D4E37  // Dark tan
)

private val AuraColors = listOf(
    0xFFE68C3A, // Warm orange glow
    0xFF60212E, // Deep burgundy glow
    0xFF4A5D23, // Forest green glow
    0xFF8B4513, // Saddle brown glow
    0xFF556B2F, // Dark olive glow
    0xFF8B4A42, // Dark salmon glow
    0xFF6B4423, // Dark wood glow
    0xFF5D4E37  // Dark tan glow
)

@Composable
private fun CardPattern(
    cardTitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    when (cardTitle.lowercase()) {
        "meditate", "mindfulness", "break." -> {
            // Soft dots pattern for meditation
            Canvas(modifier = modifier) {
                val dotSize = 6.dp.toPx()
                val spacing = 24.dp.toPx()

                for (x in 0..size.width.toInt() step spacing.toInt()) {
                    for (y in 0..size.height.toInt() step spacing.toInt()) {
                        if ((x + y) % (spacing.toInt() * 2) == 0) {
                            drawCircle(
                                color = accentColor.copy(alpha = 0.15f),
                                radius = dotSize / 2,
                                center = androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
                            )
                        }
                    }
                }
            }
        }
        "fit.", "workout", "exercise" -> {
            // Diagonal stripes for fitness
            Canvas(modifier = modifier) {
                val strokeWidth = 3.dp.toPx()
                val spacing = 20.dp.toPx()

                for (i in (-size.height.toInt())..(size.width.toInt()) step spacing.toInt()) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.12f),
                        start = androidx.compose.ui.geometry.Offset(i.toFloat(), 0f),
                        end = androidx.compose.ui.geometry.Offset(i + size.height, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
        "day.", "rest", "relax" -> {
            // Wave pattern for sleep/rest
            Canvas(modifier = modifier) {
                val amplitude = 8.dp.toPx()
                val frequency = 0.02f
                val strokeWidth = 2.dp.toPx()

                for (y in (amplitude.toInt())..(size.height.toInt() - amplitude.toInt()) step 30) {
                    val path = androidx.compose.ui.graphics.Path()
                    var firstPoint = true

                    for (x in 0..size.width.toInt() step 4) {
                        val yOffset = y + amplitude * kotlin.math.sin(x * frequency)
                        if (firstPoint) {
                            path.moveTo(x.toFloat(), yOffset)
                            firstPoint = false
                        } else {
                            path.lineTo(x.toFloat(), yOffset)
                        }
                    }

                    drawPath(
                        path = path,
                        color = accentColor.copy(alpha = 0.1f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                    )
                }
            }
        }
        "doc.", "love", "wellness" -> {
            // Heart shapes scattered
            Canvas(modifier = modifier) {
                val heartSize = 12.dp.toPx()
                val positions = listOf(
                    androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.3f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.6f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f),
                    androidx.compose.ui.geometry.Offset(size.width * 0.3f, size.height * 0.8f)
                )

                positions.forEach { position ->
                    // Simple heart approximation using circles
                    drawCircle(
                        color = accentColor.copy(alpha = 0.08f),
                        radius = heartSize / 3,
                        center = androidx.compose.ui.geometry.Offset(position.x - heartSize/4, position.y - heartSize/6)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = 0.08f),
                        radius = heartSize / 3,
                        center = androidx.compose.ui.geometry.Offset(position.x + heartSize/4, position.y - heartSize/6)
                    )
                }
            }
        }
        "eat.", "food", "diet" -> {
            // Circular/organic patterns
            Canvas(modifier = modifier) {
                val circles = listOf(
                    Triple(size.width * 0.15f, size.height * 0.25f, 8.dp.toPx()),
                    Triple(size.width * 0.75f, size.height * 0.7f, 12.dp.toPx()),
                    Triple(size.width * 0.6f, size.height * 0.15f, 6.dp.toPx()),
                    Triple(size.width * 0.25f, size.height * 0.8f, 10.dp.toPx())
                )

                circles.forEach { (x, y, radius) ->
                    drawCircle(
                        color = accentColor.copy(alpha = 0.08f),
                        radius = radius,
                        center = androidx.compose.ui.geometry.Offset(x, y),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                    )
                }
            }
        }
        else -> {
            // Default subtle diagonal pattern
            Canvas(modifier = modifier) {
                val strokeWidth = 1.5.dp.toPx()
                val spacing = 30.dp.toPx()

                for (i in (-size.height.toInt())..(size.width.toInt()) step spacing.toInt()) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.06f),
                        start = androidx.compose.ui.geometry.Offset(i.toFloat(), 0f),
                        end = androidx.compose.ui.geometry.Offset(i + size.height, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}
@Composable fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8E3D3)) // Warmer, more sophisticated background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Minimalist header with just the animated line
            MinimalistHeader()

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        WellnessLoadingAnimation()
                    }
                    uiState.error != null -> {
                        ErrorState(
                            error = uiState.error!!,
                            onRetry = { viewModel.refreshCards() }
                        )
                    }
                    else -> {
                        ExploreCardGrid(
                            cards = uiState.cards,
                            listState = listState,
                            onCardClick = { card ->
                                viewModel.onCardSelected(card)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalistHeader() {
    Column(
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var lineVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(300)
            lineVisible = true
        }

        // Just the animated gradient line, positioned higher
        AnimatedVisibility(
            visible = lineVisible,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(tween(1000))
        ) {
            var gradientOffset by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                while (true) {
                    gradientOffset = 1f
                    delay(4000)
                    gradientOffset = 0f
                    delay(4000)
                }
            }

            val animatedOffset by animateFloatAsState(
                targetValue = gradientOffset,
                animationSpec = tween(4000, easing = FastOutSlowInEasing),
                label = "gradient_offset"
            )

            Box(
                modifier = Modifier
                    .width(160.dp) // Bigger gradient line
                    .height(5.dp) // Slightly taller
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE68C3A).copy(alpha = 0.4f + animatedOffset * 0.6f), // Warm orange
                                Color(0xFF60212E).copy(alpha = 0.5f + animatedOffset * 0.5f), // Deep burgundy
                                Color(0xFF4A5D23).copy(alpha = 0.3f + animatedOffset * 0.7f), // Forest green
                                Color(0xFF8B4513).copy(alpha = 0.4f + animatedOffset * 0.6f)  // Saddle brown
                            ),
                            startX = animatedOffset * 250f,
                            endX = (animatedOffset + 0.6f) * 250f
                        ),
                        shape = RoundedCornerShape(2.5.dp)
                    )
                    .shadow(
                        elevation = (3 + animatedOffset * 6).dp,
                        shape = RoundedCornerShape(2.5.dp),
                        spotColor = Color(0xFFE68C3A).copy(alpha = 0.4f)
                    )
            )
        }
    }
}

@Composable
private fun ExploreCardGrid(
    cards: List<ExploreCard>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onCardClick: (ExploreCard) -> Unit
) {
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Group cards into pairs for dual-column layout
        val cardPairs = cards.chunked(2)

        itemsIndexed(cardPairs) { pairIndex, cardPair ->
            WellnessCardRow(
                cardPair = cardPair,
                pairIndex = pairIndex,
                onCardClick = onCardClick
            )
        }
    }
}

@Composable
private fun WellnessCardRow(
    cardPair: List<ExploreCard>,
    pairIndex: Int,
    onCardClick: (ExploreCard) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(pairIndex * 150L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        ) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.92f
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // First card
            WellnessCard(
                card = cardPair[0],
                colorIndex = pairIndex * 2,
                modifier = Modifier.weight(1f),
                onClick = { onCardClick(cardPair[0]) }
            )

            // Second card (if exists)
            if (cardPair.size > 1) {
                WellnessCard(
                    card = cardPair[1],
                    colorIndex = pairIndex * 2 + 1,
                    modifier = Modifier.weight(1f),
                    onClick = { onCardClick(cardPair[1]) }
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WellnessCard(
    card: ExploreCard,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    // Get colors from wellness palette
    val primaryColor = Color(WellnessColors[colorIndex % WellnessColors.size])
    val accentColor = Color(AccentColors[colorIndex % AccentColors.size])

    // Idle jiggle animation
    var jigglePhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        delay((colorIndex * 500L)) // Stagger the animations
        while (true) {
            jigglePhase = 1f
            delay(3000 + (colorIndex * 200L))
            jigglePhase = 0f
            delay(2000 + (colorIndex * 300L))
        }
    }

    val jiggleOffset by animateFloatAsState(
        targetValue = jigglePhase * 2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "jiggle"
    )

    // Aura resonance animation
    var auraPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            auraPhase = 1f
            delay(4000)
            auraPhase = 0f
            delay(3000)
        }
    }

    val auraIntensity by animateFloatAsState(
        targetValue = auraPhase * 0.3f,
        animationSpec = tween(4000, easing = FastOutSlowInEasing),
        label = "aura"
    )

    // Gentle animations
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isHovered -> 1.02f
            else -> 1f + (jigglePhase * 0.01f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    // Subtle rotation with jiggle
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) (-1..1).random().toFloat() else jiggleOffset * 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_rotation"
    )

    // Handle press animation reset
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(200)
            isPressed = false
        }
    }

    // Resonating aura effect
    Box(
        modifier = modifier
            .aspectRatio(1.33f)
            .height(120.dp)
    ) {
        // Aura background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = auraIntensity),
                            accentColor.copy(alpha = auraIntensity * 0.5f),
                            Color.Transparent
                        ),
                        radius = 150f
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = primaryColor.copy(alpha = 0.2f)
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    translationX = jiggleOffset
                    translationY = -jiggleOffset * 0.5f
                }
                .clickable(
                    onClick = {
                        isPressed = true
                        onClick()
                    },
                    onClickLabel = "Open ${card.title}"
                ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = primaryColor
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryColor.copy(alpha = 0.8f),
                                accentColor.copy(alpha = 0.1f)
                            ),
                            radius = 200f
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pattern overlay based on card type
                CardPattern(
                    cardTitle = card.title,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle corner accent
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .background(
                            accentColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                // Professional, readable text with rounder font
                Text(
                    text = card.title,
                    fontSize = 18.sp, // Increased text size
                    fontWeight = FontWeight.W500, // Rounder weight
                    color = Color(0xFF2D2D2D),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun WellnessLoadingAnimation() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var pulseState by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                pulseState = !pulseState
                delay(1500)
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (pulseState) 1.1f else 0.9f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing),
            label = "loading_scale"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE68C3A).copy(alpha = 0.3f),
                                Color(0xFF817C68).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(40.dp)
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFE68C3A),
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Something went wrong",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF60212E).copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE68C3A)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp)
                )
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}