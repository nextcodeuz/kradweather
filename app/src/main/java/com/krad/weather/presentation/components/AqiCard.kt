package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
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
import com.krad.weather.domain.model.AirQuality
import com.krad.weather.util.NativeWeather
import com.krad.weather.util.WeatherFormatters

@Composable
fun AqiCard(aqi: AirQuality, modifier: Modifier = Modifier, lang: String = "uz") {
    val color = when (aqi.aqi) {
        1 -> Color(0xFF66BB6A)
        2 -> Color(0xFFDCE775)
        3 -> Color(0xFFFFB74D)
        4 -> Color(0xFFFF7043)
        else -> Color(0xFFE53935)
    }
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.aqi_title), color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                Text(
                    text = "${aqi.aqi}/5 · ${aqi.label(lang)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            LinearProgressIndicator(
                progress = { aqi.aqi / 5f },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AqiValue("PM2.5", aqi.pm25)
                AqiValue("PM10", aqi.pm10)
                AqiValue("NO₂", aqi.no2)
                AqiValue("O₃", aqi.o3)
            }
            val epa = remember(aqi) {
                NativeWeather.epaAqi(aqi.pm25, aqi.pm10)
            }
            if (epa != null) {
                Text(
                    text = stringResource(R.string.aqi_us_fmt, epa),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AqiValue(label: String, value: Double?) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(
            value?.let { WeatherFormatters.oneDecimal(it) } ?: "—",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}
