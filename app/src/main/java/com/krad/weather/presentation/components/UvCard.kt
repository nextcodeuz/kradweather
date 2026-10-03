package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.R
import com.krad.weather.domain.model.LangText
import com.krad.weather.util.WeatherFormatters

@Composable
fun UvCard(uv: Double, modifier: Modifier = Modifier, lang: String = "uz") {
    val color = when {
        uv < 3 -> Color(0xFF66BB6A)
        uv < 6 -> Color(0xFFDCE775)
        uv < 8 -> Color(0xFFFFB74D)
        uv < 11 -> Color(0xFFFF7043)
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
                Text(
                    stringResource(R.string.uv_title),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
                Text(
                    text = "${WeatherFormatters.oneDecimal(uv)} · ${LangText.uvLabel(lang, uv)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            LinearProgressIndicator(
                progress = { (uv / 11.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}
