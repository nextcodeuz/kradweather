package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.CurrentWeather
import com.krad.weather.util.NativeWeather
import com.krad.weather.util.WeatherFormatters

@Composable
fun DetailsGrid(current: CurrentWeather, modifier: Modifier = Modifier, units: String = "metric") {
    val tempC = remember(current.temperature, units) {
        WeatherFormatters.convertTemp(current.temperature, units, "metric")
    }
    val dew = remember(tempC, current.humidity) {
        NativeWeather.dewPoint(tempC, current.humidity.toDouble())
    }
    val moonPhase = remember { NativeWeather.moonPhase() }
    val moonEmoji = when {
        moonPhase < 0.03 || moonPhase > 0.97 -> "🌑"
        moonPhase < 0.22 -> "🌒"
        moonPhase < 0.28 -> "🌓"
        moonPhase < 0.47 -> "🌔"
        moonPhase < 0.53 -> "🌕"
        moonPhase < 0.72 -> "🌖"
        moonPhase < 0.78 -> "🌗"
        else -> "🌘"
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DetailTile(
                label = stringResource(R.string.feels_like),
                value = WeatherFormatters.temp(current.feelsLike),
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = stringResource(R.string.humidity),
                value = "${current.humidity}%",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DetailTile(
                label = stringResource(R.string.pressure),
                value = WeatherFormatters.pressure(current.pressure),
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = stringResource(R.string.visibility),
                value = WeatherFormatters.visibility(current.visibility),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DetailTile(
                label = stringResource(R.string.clouds),
                value = "${current.clouds}%",
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = stringResource(R.string.precipitation),
                value = if (current.precipMm > 0) "${WeatherFormatters.oneDecimal(current.precipMm)} mm" else stringResource(R.string.precip_none),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DetailTile(
                label = stringResource(R.string.sunrise),
                value = WeatherFormatters.timeOfDay(current.sunrise, current.timezoneOffsetSeconds),
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = stringResource(R.string.sunset),
                value = WeatherFormatters.timeOfDay(current.sunset, current.timezoneOffsetSeconds),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DetailTile(
                label = stringResource(R.string.dew_point),
                value = WeatherFormatters.temp(dew),
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = stringResource(R.string.moon_phase),
                value = "$moonEmoji ${(moonPhase * 100).toInt()}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailTile(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
