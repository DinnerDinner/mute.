@file:OptIn(ExperimentalAnimationApi::class)
package com.example.mute_app.navgraphs
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mute_app.screens.HomeScreen
import com.example.mute_app.screens.ExploreScreen
import com.example.mute_app.screens.ProfileScreen

sealed class MainDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : MainDestination("home", "Home", Icons.Default.Home)
    object Explore : MainDestination("explore", "Explore", Icons.Default.Search)
    object Profile : MainDestination("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val destinations = listOf(
        MainDestination.Home,
        MainDestination.Explore,
        MainDestination.Profile
    )

    Scaffold(
        bottomBar = {
            SpectacularBottomBar(
                destinations = destinations,
                currentDestination = currentDestination?.route,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(MainDestination.Home.route) {
                HomeScreen()
            }
            composable(MainDestination.Explore.route) {
                ExploreScreen()
            }
            composable(MainDestination.Profile.route) {
                ProfileScreen()
            }
        }
    }
}

@Composable
private fun SpectacularBottomBar(
    destinations: List<MainDestination>,
    currentDestination: String?,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val isSelected = currentDestination == destination.route

                AnimatedContent(
                    targetState = isSelected,
                    transitionSpec = {
                        scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) with scaleOut(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    },
                    label = "bottom_bar_animation"
                ) { selected ->
                    BottomBarItem(
                        destination = destination,
                        isSelected = selected,
                        onClick = { onNavigate(destination.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    destination: MainDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "color_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(scaleX = animatedScale, scaleY = animatedScale),
                tint = animatedColor
            )
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
            exit = fadeOut(animationSpec = tween(200)) + slideOutVertically()
        ) {
            Text(
                text = destination.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = animatedColor
            )
        }
    }
}