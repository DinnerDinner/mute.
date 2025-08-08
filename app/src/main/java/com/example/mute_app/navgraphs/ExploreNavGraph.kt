@file:OptIn(ExperimentalAnimationApi::class)
package com.example.mute_app.navgraphs
import androidx.hilt.navigation.compose.hiltViewModel

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
import com.example.mute_app.screens.ExploreScreen
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.screens.explore.BreakScreen
import com.example.mute_app.screens.explore.DocScreen
import com.example.mute_app.screens.explore.StatsScreen
import com.example.mute_app.screens.explore.EatScreen
import com.example.mute_app.screens.explore.FitScreen
import com.example.mute_app.screens.explore.DayScreen
import com.example.mute_app.viewmodels.ExploreViewModel

sealed class ExploreDestination(val route: String) {
    object Main : ExploreDestination("explore_main")
    object Break : ExploreDestination("break")
    object Doc : ExploreDestination("doc")
    object Stats : ExploreDestination("stats")
    object Eat : ExploreDestination("eat")
    object Fit : ExploreDestination("fit")
    object Day : ExploreDestination("day")
}

@Composable
fun ExploreNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {} // Callback to control main bottom bar
) {

    // Monitor current destination to control bottom bar visibility
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Control bottom bar visibility based on current route
    LaunchedEffect(currentDestination) {
        when (currentDestination) {
            ExploreDestination.Main.route -> onBottomBarVisibilityChanged(true)
            ExploreDestination.Break.route,
            ExploreDestination.Doc.route,
            ExploreDestination.Stats.route,
            ExploreDestination.Eat.route,
            ExploreDestination.Fit.route,
            ExploreDestination.Day.route -> onBottomBarVisibilityChanged(false)
        }
    }

    NavHost(
        navController = navController,
        startDestination = ExploreDestination.Main.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + scaleIn(
                initialScale = 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
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
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + scaleOut(
                targetScale = 1.05f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
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
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + scaleIn(
                initialScale = 1.05f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
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
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + scaleOut(
                targetScale = 0.95f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    ) {
        composable(ExploreDestination.Main.route) {
            val viewModel: ExploreViewModel = hiltViewModel()


            ExploreScreen(
                viewModel = viewModel,
                onNavigateToBreak = {
                    try {
                        navController.navigate(ExploreDestination.Break.route)
                    } catch (e: Exception) {
                    }
                },
                onNavigateToDoc = {
                    try {
                        navController.navigate(ExploreDestination.Doc.route)
                    } catch (e: Exception) {
                    }
                },
                onNavigateToStats = {
                    try {
                        navController.navigate(ExploreDestination.Stats.route)
                    } catch (e: Exception) {
                    }
                },
                onNavigateToEat = {
                    try {
                        navController.navigate(ExploreDestination.Eat.route)
                    } catch (e: Exception) {
                    }
                },
                onNavigateToFit = {
                    try {
                        navController.navigate(ExploreDestination.Fit.route)
                    } catch (e: Exception) {
                    }
                },
                onNavigateToDay = {
                    println("DEBUG: onNavigateToDay callback called!")
                    try {
                        navController.navigate(ExploreDestination.Day.route)
                        println("DEBUG: Navigation to day successful!")
                    } catch (e: Exception) {
                        println("DEBUG: Navigation failed - ${e.message}")
                    }
                }
            )
        }

        composable(ExploreDestination.Break.route) {
            BreakScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExploreDestination.Doc.route) {
            DocScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExploreDestination.Stats.route) {
            StatsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExploreDestination.Eat.route) {
            EatScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExploreDestination.Fit.route) {
            FitScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(ExploreDestination.Day.route) {
            DayScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}