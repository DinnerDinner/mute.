@file:OptIn(ExperimentalAnimationApi::class)
package com.example.mute_app.navgraphs
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mute_app.screens.ExploreScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mute_app.screens.explore.BreakScreen
import com.example.mute_app.screens.explore.DocScreen
import com.example.mute_app.screens.explore.EatScreen
import com.example.mute_app.screens.explore.FitScreen
import com.example.mute_app.screens.explore.DayScreen
import com.example.mute_app.screens.explore.StatsScreen
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
    navController: NavHostController = rememberNavController()
) {
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
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ExploreScreen(
                viewModel = viewModel
            )

            // Handle navigation when card is selected
            LaunchedEffect(uiState.selectedCard) {
                uiState.selectedCard?.let { card ->
                    when (card.id) {
                        "break" -> {
                            navController.navigate(ExploreDestination.Break.route)
                            viewModel.clearSelection()
                        }
                        "doc" -> {
                            navController.navigate(ExploreDestination.Doc.route)
                            viewModel.clearSelection()
                        }
                        "stats" -> {
                            navController.navigate(ExploreDestination.Stats.route)
                            viewModel.clearSelection()
                        }
                        "eat" -> {
                            navController.navigate(ExploreDestination.Eat.route)
                            viewModel.clearSelection()
                        }
                        "fit" -> {
                            navController.navigate(ExploreDestination.Fit.route)
                            viewModel.clearSelection()
                        }
                        "day" -> {
                            navController.navigate(ExploreDestination.Day.route)
                            viewModel.clearSelection()
                        }
                    }
                }
            }
        }

        composable(ExploreDestination.Break.route) {
            BreakScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ExploreDestination.Doc.route) {
            // Temporary placeholder screen for Doc
            DocScreen(
                title = "doc.",
                subtitle = "Personal Medical & Mental Health Bot",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ExploreDestination.Stats.route) {
            // Temporary placeholder screen for Stats
            StatsScreen(
                title = "stats.",
                subtitle = "Personal Analytics & Emotional Pulse",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ExploreDestination.Eat.route) {
            // Temporary placeholder screen for Eat
            EatScreen(
                title = "eat.",
                subtitle = "AI-Powered Smart Cooking Assistant",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ExploreDestination.Fit.route) {
            // Temporary placeholder screen for Fit
            FitScreen(
                title = "fit.",
                subtitle = "Personalized Fitness Companion",
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ExploreDestination.Day.route) {
            // Temporary placeholder screen for Day
            DayScreen(
                title = "day.",
                subtitle = "Lifestyle Routine & Micro Habits Manager",
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}