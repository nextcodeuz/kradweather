package com.krad.weather.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krad.weather.domain.model.WeatherBundle

/**
 * Rasmiy ogohlantirishlar (WeatherAPI.com, bo'lsa) + SmartAlertAnalyzer tahlili.
 */
fun weatherAlerts(bundle: WeatherBundle, lang: String = "uz"): List<String> {
    val official = bundle.officialAlerts.map { it.text }.filter { it.isNotBlank() }
    val analyzed = com.krad.weather.domain.model.SmartAlertAnalyzer
        .analyze(bundle.current, bundle.forecast.hourly, lang)
        .map { it.text }
    return (official + analyzed).distinct().take(4)
}

@Composable
fun AlertsBanner(bundle: WeatherBundle, lang: String = "uz", modifier: Modifier = Modifier) {
    val alerts = remember(bundle, lang) { weatherAlerts(bundle, lang) }
    if (alerts.isEmpty()) return
    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            alerts.forEach { msg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = msg,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
