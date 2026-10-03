package com.krad.weather.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.krad.weather.presentation.common.WidthClass
import com.krad.weather.presentation.common.rememberWidthClass
import com.krad.weather.presentation.components.KradBottomNav
import com.krad.weather.presentation.components.KradRail
import com.krad.weather.presentation.favorites.FavoritesScreen
import com.krad.weather.presentation.home.HomeScreen
import com.krad.weather.presentation.search.SearchScreen
import com.krad.weather.presentation.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
}

/**
 * Adaptiv navigatsiya:
 * - Telefon (portret/o'rta): pastki menyu.
 * - Keng ekran (landshaft/planshet): yon rail — menyu siqilib ketmaydi.
 * - Kontent planshetda markazda, maksimal 900dp — cho'zilib ketmaydi.
 * - Scaffold padding to'liq qo'llanadi — status/nav bar ostida qolmaydi.
 */
@Composable
fun KradWeatherNavHost(navController: NavHostController = rememberNavController()) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val widthClass = rememberWidthClass()
    val useRail = widthClass == WidthClass.Expanded

    fun goHome() {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (!useRail) {
                KradBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (useRail) {
                KradRail(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 900.dp)
                        .align(Alignment.TopCenter),
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) }
                ) {
                    composable(Routes.HOME) { HomeScreen() }
                    composable(Routes.SEARCH) {
                        SearchScreen(onNavigateHome = { goHome() })
                    }
                    composable(Routes.FAVORITES) {
                        FavoritesScreen(onNavigateHome = { goHome() })
                    }
                    composable(Routes.SETTINGS) { SettingsScreen() }
                }
            }
        }
    }
}
