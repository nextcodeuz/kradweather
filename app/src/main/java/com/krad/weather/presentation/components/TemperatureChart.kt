package com.krad.weather.presentation.components

import android.graphics.Color as GraphicsColor
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.HourlyForecast
import com.krad.weather.util.NativeWeather
import com.krad.weather.util.WeatherFormatters
import kotlin.math.roundToInt

@Composable
fun TemperatureChart(
    items: List<HourlyForecast>,
    timezoneOffsetSeconds: Int,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.temp_chart),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            // C++ engine silliqlaydi (moving average) — chiziq silliq chiqadi
            val temps = remember(items) {
                NativeWeather.smoothTemps(items.map { it.temperature })
            }
            val min = temps.minOrNull() ?: 0.0
            val max = temps.maxOrNull() ?: 0.0
            val span = (max - min).takeIf { it > 0.5 } ?: 1.0
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 12.dp)
            ) {
                val w = size.width
                val h = size.height - 40f
                val n = temps.size
                fun x(i: Int): Float = if (n == 1) w / 2f else (i.toFloat() / (n - 1)) * w
                fun y(t: Double): Float = (h - ((t - min) / span * h).toFloat()).coerceIn(0f, h)

                // Min/max fon chiziqlari
                drawLine(
                    Color.White.copy(alpha = 0.2f),
                    Offset(0f, y(max)), Offset(w, y(max))
                )
                drawLine(
                    Color.White.copy(alpha = 0.2f),
                    Offset(0f, y(min)), Offset(w, y(min))
                )

                val path = Path()
                temps.forEachIndexed { i, t ->
                    if (i == 0) path.moveTo(x(i), y(t)) else path.lineTo(x(i), y(t))
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFFD54F),
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
                temps.forEachIndexed { i, t ->
                    drawCircle(Color.White, radius = 7f, center = Offset(x(i), y(t)))
                    drawCircle(Color(0xFFFFB300), radius = 4f, center = Offset(x(i), y(t)))
                }

                // Har 2-nuqtada qiymat + vaqt yorlig'i
                temps.forEachIndexed { i, t ->
                    if (i % 2 == 0) {
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                "${t.roundToInt()}°",
                                x(i) - 18f,
                                y(t) - 14f,
                                Paint().apply {
                                    color = GraphicsColor.WHITE
                                    textSize = 30f
                                    isFakeBoldText = true
                                }
                            )
                            drawText(
                                WeatherFormatters.timeOfDay(items[i].dt, timezoneOffsetSeconds),
                                x(i) - 30f,
                                h + 32f,
                                Paint().apply {
                                    color = GraphicsColor.argb(200, 255, 255, 255)
                                    textSize = 26f
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
