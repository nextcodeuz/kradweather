package com.krad.weather.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = androidx.compose.material3.lightColorScheme(
    primary = SkyBlue,
    onPrimary = Color.White,
    secondary = SunsetOrange,
    background = Color(0xFFF5F7FA),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = DeepBlue,
    onPrimary = Color.White,
    secondary = SunnyYellow,
    background = NightIndigo,
    surface = Color(0xFF0B1026)
)

@Composable
fun KradWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, typography = KradTypography, content = content)
}
