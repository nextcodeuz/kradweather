package com.krad.weather.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.domain.model.HourlyForecast
import com.krad.weather.util.WeatherFormatters

@Composable
fun HourlyForecastRow(
    items: List<HourlyForecast>,
    timezoneOffsetSeconds: Int,
    modifier: Modifier = Modifier,
    onItemClick: (HourlyForecast) -> Unit = {}
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.dt }) { hour ->
            GlassCard(
                modifier = Modifier
                    .width(76.dp)
                    .clickable(onClick = { onItemClick(hour) })
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = WeatherFormatters.timeOfDay(hour.dt, timezoneOffsetSeconds),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    WeatherIcon(iconCode = hour.condition.icon, size = 40.dp)
                    Text(
                        text = WeatherFormatters.temp(hour.temperature),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    if (hour.pop > 0.05) {
                        Text(
                            text = "${(hour.pop * 100).toInt()}%",
                            color = Color(0xFF90CAF9),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
