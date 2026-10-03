package com.krad.weather.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.CurrentWeather
import com.krad.weather.util.WeatherFormatters

@Composable
fun SunArc(current: CurrentWeather, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sun_title),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = 12.dp)
            ) {
                val w = size.width
                val h = size.height
                val path = Path()
                path.moveTo(0f, h)
                path.quadraticTo(w / 2f, -h * 0.3f, w, h)
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.35f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                val total = (current.sunset - current.sunrise).coerceAtLeast(1L)
                val frac = ((current.dt - current.sunrise).toDouble() / total.toDouble()).coerceIn(0.0, 1.0)
                val t = frac.toFloat()
                val cx = (1 - t) * (1 - t) * 0f + 2 * (1 - t) * t * (w / 2f) + t * t * w
                val cy = (1 - t) * (1 - t) * h + 2 * (1 - t) * t * (-h * 0.3f) + t * t * h
                drawCircle(color = Color(0xFFFFD54F), radius = 9f, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFF59D).copy(alpha = 0.35f), radius = 20f, center = Offset(cx, cy))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = WeatherFormatters.timeOfDay(current.sunrise, current.timezoneOffsetSeconds),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = WeatherFormatters.timeOfDay(current.sunset, current.timezoneOffsetSeconds),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}
