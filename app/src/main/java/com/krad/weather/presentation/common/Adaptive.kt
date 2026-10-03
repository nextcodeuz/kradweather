package com.krad.weather.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

enum class WidthClass { Compact, Medium, Expanded }

/**
 * Tashqi kutubxonasiz o'lcham klassi (WindowSizeClass o'rniga — engil va risksiz).
 * <600dp ixcham (telefon portret), <840dp o'rta, undan katta — planshet/keng.
 * Konfiguratsiya o'zgarganda (burilish, fold) avtomatik qayta hisoblanadi.
 */
@Composable
fun rememberWidthClass(): WidthClass {
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp, config.orientation) {
        when {
            config.screenWidthDp < 600 -> WidthClass.Compact
            config.screenWidthDp < 840 -> WidthClass.Medium
            else -> WidthClass.Expanded
        }
    }
}

/** Balandlik ixchammi (landshaft telefon)? Hero ixcham chiziladi. */
@Composable
fun rememberCompactHeight(): Boolean {
    val config = LocalConfiguration.current
    return remember(config.screenHeightDp, config.orientation) {
        config.screenHeightDp < 600
    }
}
