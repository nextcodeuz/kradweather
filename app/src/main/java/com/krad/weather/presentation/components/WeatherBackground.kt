package com.krad.weather.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.krad.weather.util.BackgroundPalette

private fun paletteFor(palette: BackgroundPalette): List<Color> = when (palette) {
    BackgroundPalette.CLEAR -> listOf(Color(0xFF2196F3), Color(0xFF64B5F6), Color(0xFFFFC107))
    BackgroundPalette.CLOUD -> listOf(Color(0xFF546E7A), Color(0xFF78909C), Color(0xFFB0BEC5))
    BackgroundPalette.RAIN -> listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF546E7A))
    BackgroundPalette.STORM -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
    BackgroundPalette.SNOW -> listOf(Color(0xFFB3E5FC), Color(0xFFE1F5FE), Color(0xFFECEFF1))
    BackgroundPalette.NIGHT -> listOf(Color(0xFF0B1026), Color(0xFF1A237E), Color(0xFF311B92))
}

@Composable
fun WeatherBackground(
    palette: BackgroundPalette,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = paletteFor(palette)
    val top by animateColorAsState(colors[0], tween(900), label = "top")
    val mid by animateColorAsState(colors[1], tween(900), label = "mid")
    val bot by animateColorAsState(colors[2], tween(900), label = "bot")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, mid, bot)))
    ) {
        content()
    }
}
