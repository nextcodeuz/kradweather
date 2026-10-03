package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.DailyForecast
import com.krad.weather.domain.model.HourlyForecast
import com.krad.weather.domain.model.LangText
import com.krad.weather.util.WeatherFormatters

@Composable
fun HourDetailDialog(
    hour: HourlyForecast,
    timezoneOffsetSeconds: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
        title = {
            Text(
                stringResource(
                    R.string.hour_title_fmt,
                    WeatherFormatters.timeOfDay(hour.dt, timezoneOffsetSeconds)
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherIcon(iconCode = hour.condition.icon, size = 56.dp)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            WeatherFormatters.temp(hour.temperature),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            hour.condition.description.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp
                        )
                    }
                }
                DetailLine(stringResource(R.string.feels_like), WeatherFormatters.temp(hour.feelsLike))
                DetailLine(stringResource(R.string.precipitation), "${(hour.pop * 100).toInt()}%")
                hour.rainMm?.let { DetailLine(stringResource(R.string.rain_amount), "${WeatherFormatters.oneDecimal(it)} mm") }
                DetailLine(stringResource(R.string.wind), "${WeatherFormatters.oneDecimal(hour.windSpeed)} m/s")
                DetailLine(stringResource(R.string.humidity), "${hour.humidity}%")
            }
        }
    )
}

@Composable
fun DayDetailDialog(
    day: DailyForecast,
    onDismiss: () -> Unit,
    lang: String = "uz"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_close)) }
        },
        title = {
            Text(
                stringResource(
                    R.string.day_title_fmt,
                    WeatherFormatters.dayShort(day.dateEpochDay, lang)
                ),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherIcon(iconCode = day.condition.icon, size = 56.dp)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "▲ ${WeatherFormatters.temp(day.maxTemp)}  ▼ ${WeatherFormatters.temp(day.minTemp)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            day.condition.description.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp
                        )
                    }
                }
                DetailLine(stringResource(R.string.precipitation), "${(day.pop * 100).toInt()}%")
                day.rainMm?.let { DetailLine(stringResource(R.string.rain_amount), "${WeatherFormatters.oneDecimal(it)} mm") }
                DetailLine(stringResource(R.string.wind), "${WeatherFormatters.oneDecimal(day.windSpeed)} m/s")
                DetailLine(stringResource(R.string.humidity), "${day.humidity}%")
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

/** Kiyim maslahati — sof funksiya, 3 tilda (testlanadi). */
fun dressAdvice(tempC: Double, pop: Double, windSpeed: Double, lang: String = "uz"): String =
    LangText.dress(lang, tempC, pop, windSpeed)

@Composable
fun DressTipCard(
    tempC: Double,
    pop: Double,
    windSpeed: Double,
    modifier: Modifier = Modifier,
    lang: String = "uz",
    title: String = "Bugun nima kiyaman? 👗"
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                dressAdvice(tempC, pop, windSpeed, lang),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
        }
    }
}
