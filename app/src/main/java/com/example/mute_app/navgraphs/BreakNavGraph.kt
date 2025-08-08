@file:OptIn(ExperimentalAnimationApi::class)
package com.example.mute_app.navgraphs

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mute_app.screens.explore.`break`.*
import androidx.compose.runtime.getValue

sealed class BreakDestination(val route: String) {
    object Main : BreakDestination("break_main")
    object Blocklist : BreakDestination("blocklist")
    object Applications : BreakDestination("applications")
    object Websites : BreakDestination("websites")
}

@Composable
fun BreakNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onBackToExplore: () -> Unit
) {
    println("DEBUG: BreakNavGraph composable called")

    // Monitor current destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    LaunchedEffect(currentDestination) {
        println("DEBUG: BreakNavGraph - Current destination: $currentDestination")
    }

    NavHost(
        navController = navController,
        startDestination = BreakDestination.Main.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(BreakDestination.Main.route) {
            println("DEBUG: Break Main route composable called")
            BreakScreen(
                onNavigateToBlocklist = {
                    println("DEBUG: Navigate to blocklist called")
                    navController.navigate(BreakDestination.Blocklist.route)
                },
                onBackClick = onBackToExplore
            )
        }

        composable(BreakDestination.Blocklist.route) {
            println("DEBUG: Blocklist route composable called")
            BlocklistScreen(
                onNavigateToApplications = {
                    println("DEBUG: Navigate to applications called")
                    navController.navigate(BreakDestination.Applications.route)
                },
                onNavigateToWebsites = {
                    println("DEBUG: Navigate to websites called")
                    navController.navigate(BreakDestination.Websites.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(BreakDestination.Applications.route) {
            println("DEBUG: Applications route composable called")
            ApplicationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(BreakDestination.Websites.route) {
            println("DEBUG: Websites route composable called")
            WebsitesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}