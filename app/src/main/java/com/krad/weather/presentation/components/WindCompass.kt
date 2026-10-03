package com.krad.weather.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.CurrentWeather
import com.krad.weather.util.WeatherFormatters
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindCompass(current: CurrentWeather, modifier: Modifier = Modifier) {
    val target = current.windDeg.toFloat()
    val angle by animateFloatAsState(target, tween(800), label = "wind")

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(modifier = Modifier.size(72.dp)) {
                val c = center
                val r = size.minDimension / 2f - 6f
                drawCircle(Color.White.copy(alpha = 0.25f), radius = r, center = c)
                // Asosiy yo'nalishlar belgilari
                listOf(0f, 90f, 180f, 270f).forEach { deg ->
                    val rad = Math.toRadians((deg - 90).toDouble())
                    val p1 = Offset(
                        c.x + cos(rad).toFloat() * (r - 8f),
                        c.y + sin(rad).toFloat() * (r - 8f)
                    )
                    val p2 = Offset(
                        c.x + cos(rad).toFloat() * r,
                        c.y + sin(rad).toFloat() * r
                    )
                    drawLine(Color.White.copy(alpha = 0.6f), p1, p2, strokeWidth = 3f)
                }
                rotate(angle, c) {
                    // Shimolga yo'nalgan strelka (meteorologik: deg = shamol qayerdan esayotgani)
                    drawLine(
                        Color(0xFFFFD54F),
                        Offset(c.x, c.y + r - 10f),
                        Offset(c.x, c.y - r + 10f),
                        strokeWidth = 6f
                    )
                    drawCircle(Color(0xFFFFD54F), radius = 8f, center = Offset(c.x, c.y - r + 10f))
                }
                drawCircle(Color.White, radius = 6f, center = c)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = stringResource(R.string.wind),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${WeatherFormatters.oneDecimal(current.windSpeed)} m/s ${WeatherFormatters.windDir(current.windDeg)}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                current.windGust?.let {
                    Text(
                        text = stringResource(R.string.wind_gust_fmt, WeatherFormatters.oneDecimal(it)),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
