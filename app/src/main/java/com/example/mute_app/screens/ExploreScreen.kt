package com.example.mute_app.screens
//testing
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.viewmodels.ExploreViewModel
import com.example.mute_app.viewmodels.ExploreCard
import kotlinx.coroutines.delay
import kotlin.math.*

// Enhanced wellness color palette with better contrast
private val WellnessColors = listOf(
    0xFFF8F6F0, // Warm cream
    0xFFE8E3D8, // Light sage
    0xFFD4C8B8, // Warm beige
    0xFFE2D5C7, // Soft tan
    0xFFF0E6DA, // Peachy cream
    0xFFE6DDD4, // Mushroom
    0xFFDFD3C3, // Sandy beige
    0xFFEAE0D5  // Warm ivory
)

// Better contrast accent colors - not too dark!
private val AccentColors = listOf(
    0xFFD2691E, // Chocolate orange
    0xFF8B4A3D, // Warm brown
    0xFF6B7B4F, // Sage green
    0xFFB8860B, // Dark goldenrod
    0xFF8FBC8F, // Dark sea green
    0xFFCD853F, // Peru
    0xFFA0522D, // Sienna
    0xFF9ACD32  // Yellow green
)

// Aura colors that match accent colors perfectly - professional and royal
private val AuraColors = listOf(
    0xFFD2691E, // Matches chocolate orange accent
    0xFF8B4A3D, // Matches warm brown accent
    0xFF6B7B4F, // Matches sage green accent
    0xFFB8860B, // Matches dark goldenrod accent
    0xFF8FBC8F, // Matches dark sea green accent
    0xFFCD853F, // Matches peru accent
    0xFFA0522D, // Matches sienna accent
    0xFF9ACD32  // Matches yellow green accent
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

// COMPLETELY NEW ROYAL AURA RESONANCE SYSTEM - REBUILT FROM SCRATCH
@Composable
private fun RoyalAuraResonance(
    auraColor: Color,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    // Professional royal aura that actually appears and is visible but subtle
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Much smaller, professional radius - no more circus
        val baseRadius = minOf(size.width, size.height) * 0.45f // Smaller base
        val maxRadius = baseRadius * (1.0f + intensity * 0.15f) // Subtle growth

        // GUARANTEED VISIBLE base alpha - never invisible
        val baseAlpha = 0.25f + (intensity * 0.15f) // Always at least 25% visible

        // Professional royal glow layers - 3 concentric rings
        for (ring in 0..2) {
            val ringMultiplier = 1f - (ring * 0.25f) // 1.0, 0.75, 0.5
            val currentRadius = maxRadius * ringMultiplier
            val currentAlpha = baseAlpha * (1f - ring * 0.3f) // Fade outward

            // Ensure minimum visibility
            if (currentAlpha > 0.05f && currentRadius > 10f) {
                // Main royal glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = currentAlpha * 0.8f), // Strong center
                            auraColor.copy(alpha = currentAlpha * 0.5f), // Medium middle
                            auraColor.copy(alpha = currentAlpha * 0.2f), // Soft edge
                            Color.Transparent // Fade to nothing
                        ),
                        radius = currentRadius,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                    ),
                    radius = currentRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )

                // Inner concentrated royal core - MORE VISIBLE
                val coreRadius = currentRadius * 0.6f
                val coreAlpha = currentAlpha * 1.3f // Brighter core

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = coreAlpha.coerceAtMost(0.6f)), // Bright center
                            auraColor.copy(alpha = coreAlpha * 0.7f), // Medium fade
                            auraColor.copy(alpha = coreAlpha * 0.3f), // Outer fade
                            Color.Transparent
                        ),
                        radius = coreRadius,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                    ),
                    radius = coreRadius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
            }
        }

        // Subtle shimmer effect - NO LASER BEAMS, just gentle shimmer
        val shimmerCount = 6 // Fewer, more elegant
        val shimmerRadius = maxRadius * 0.8f
        val shimmerAlpha = baseAlpha * 0.4f // Subtle shimmer

        if (shimmerAlpha > 0.03f) {
            for (shimmer in 0 until shimmerCount) {
                val angle = (shimmer * 60f) + (intensity * 20f) // Gentle rotation
                val shimmerLength = shimmerRadius * 0.3f // Much shorter

                val startRadius = shimmerRadius * 0.7f
                val endRadius = startRadius + shimmerLength

                val startX = centerX + cos(angle * PI / 180f).toFloat() * startRadius
                val startY = centerY + sin(angle * PI / 180f).toFloat() * startRadius
                val endX = centerX + cos(angle * PI / 180f).toFloat() * endRadius
                val endY = centerY + sin(angle * PI / 180f).toFloat() * endRadius

                // Gentle shimmer rays - NOT laser beams
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            auraColor.copy(alpha = shimmerAlpha * 0.8f),
                            auraColor.copy(alpha = shimmerAlpha * 0.4f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY)
                    ),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(), // Much thinner
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Elegant pulsing outline - royal finishing touch
        val outlineAlpha = baseAlpha * 0.6f
        if (outlineAlpha > 0.05f) {
            drawCircle(
                color = auraColor.copy(alpha = outlineAlpha),
                radius = maxRadius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 1.5.dp.toPx()
                )
            )
        }
    }
}

// NEW PROFESSIONAL AURA ANIMATION SYSTEM - REBUILT FROM SCRATCH
@Composable
private fun useRoyalAuraAnimation(cardIndex: Int): Float {
    var auraIntensity by remember { mutableFloatStateOf(0.4f) } // Start at 40% - always visible

    // Staggered, professional animation cycles
    LaunchedEffect(cardIndex) {
        delay(cardIndex * 400L) // Staggered start - more elegant

        while (true) {
            // Gentle rise phase - professional
            repeat(25) { step ->
                auraIntensity = 0.4f + (step / 24f) * 0.35f // 40% to 75%
                delay(60) // Smooth transition
            }

            delay(800) // Hold at peak - elegant pause

            // Gentle fall phase - royal descent
            repeat(30) { step ->
                auraIntensity = 0.75f - (step / 29f) * 0.35f // 75% back to 40%
                delay(50) // Smooth descent
            }

            delay(1200 + (cardIndex * 200L)) // Varied rest period per card
        }
    }

    return auraIntensity
}

@Composable fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Warmer, more sophisticated background
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
        verticalArrangement = Arrangement.spacedBy(16.dp), // Reduced spacing - less spaced out
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 20.dp),
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
            horizontalArrangement = Arrangement.spacedBy(20.dp),
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

    // Get colors from wellness palette - MATCHING aura colors properly
    val primaryColor = Color(WellnessColors[colorIndex % WellnessColors.size])
    val accentColor = Color(AccentColors[colorIndex % AccentColors.size])
    val auraColor = Color(AuraColors[colorIndex % AuraColors.size]) // Perfect match now!

    // Professional aura animation - always visible, never disappears
    val auraIntensity = useRoyalAuraAnimation(colorIndex)

    // Idle jiggle animation - more subtle now
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
        targetValue = jigglePhase * 1.5f, // Reduced jiggle
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "jiggle"
    )

    // Gentle animations
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.02f
            else -> 1f + (jigglePhase * 0.005f) // More subtle
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )

    // Subtle rotation with jiggle
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) (-1..1).random().toFloat() else jiggleOffset * 0.3f, // Less rotation
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

    // THE MAIN CARD WITH PROFESSIONAL ROYAL AURA
    Box(
        modifier = modifier
            .aspectRatio(1.4f) // Slightly increased card size
            .height(130.dp) // Increased from 120dp
    ) {
        // PROFESSIONAL ROYAL AURA RESONANCE - ALWAYS VISIBLE AND MATCHING
        RoyalAuraResonance(
            auraColor = auraColor, // Perfect color match to accent
            intensity = auraIntensity, // Always visible, never hidden
            modifier = Modifier.fillMaxSize()
        )

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp) // Reduced aura space - more compact
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = auraColor.copy(alpha = 0.3f) // Matching shadow color
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
                defaultElevation = 8.dp
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
                                primaryColor.copy(alpha = 0.9f),
                                accentColor.copy(alpha = 0.15f)
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
                            accentColor.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                // ENHANCED TEXT WITH BETTER FONT
                Text(
                    text = card.title,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF2A2A2A),
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp,
                    letterSpacing = 0.3.sp,
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
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = Color(0xFF8B4A3D).copy(alpha = 0.8f) // Better error color
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD2691E) // Better button color
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
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}