package com.krad.weather.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.krad.weather.R
import com.krad.weather.presentation.navigation.Routes

data class BottomTab(val route: String, @StringRes val labelRes: Int, val icon: ImageVector)

val BOTTOM_TABS = listOf(
    BottomTab(Routes.HOME, R.string.tab_home, Icons.Default.Home),
    BottomTab(Routes.SEARCH, R.string.tab_search, Icons.Default.Search),
    BottomTab(Routes.FAVORITES, R.string.tab_favorites, Icons.Default.Favorite),
    BottomTab(Routes.SETTINGS, R.string.tab_settings, Icons.Default.Settings)
)

@Composable
fun KradRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color(0xFF0B1026).copy(alpha = 0.92f)
    ) {
        BOTTOM_TABS.forEach { tab ->
            val label = stringResource(tab.labelRes)
            NavigationRailItem(
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab.route) },
                icon = { Icon(tab.icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD54F),
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.White.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
fun KradBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0B1026).copy(alpha = 0.92f)
    ) {
        BOTTOM_TABS.forEach { tab ->
            val label = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onNavigate(tab.route) },
                icon = { Icon(tab.icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFD54F),
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                    indicatorColor = Color.White.copy(alpha = 0.12f)
                )
            )
        }
    }
}
