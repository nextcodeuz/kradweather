package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.domain.model.DailyForecast
import com.krad.weather.util.WeatherFormatters

/**
 * Og'irlik (weight) asosida — kichik ekran va katta shriftda ham siqilib ketmaydi.
 */
@Composable
fun DailyForecastRow(item: DailyForecast, modifier: Modifier = Modifier, lang: String = "uz") {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = WeatherFormatters.dayShort(item.dateEpochDay, lang),
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        WeatherIcon(iconCode = item.condition.icon, size = 38.dp)
        Spacer(Modifier.width(4.dp))
        if (item.pop > 0.05) {
            Text(
                text = "${(item.pop * 100).toInt()}%",
                color = Color(0xFF90CAF9),
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.size(width = 44.dp, height = 20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(width = 44.dp, height = 20.dp))
        }
        Text(
            text = WeatherFormatters.temp(item.minTemp),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            text = WeatherFormatters.temp(item.maxTemp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.8f)
        )
    }
}
