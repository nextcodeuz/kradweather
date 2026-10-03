package com.krad.weather.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krad.weather.R
import com.krad.weather.presentation.components.WeatherBackground
import com.krad.weather.util.BackgroundPalette

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var city by remember { mutableStateOf("") }

    WeatherBackground(palette = BackgroundPalette.NIGHT) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⛅", fontSize = 64.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.app_name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                stringResource(R.string.ob_sub),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp
            )
            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.ob_city_hint)) },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("metric" to "°C", "imperial" to "°F").forEach { (key, label) ->
                    FilterChip(
                        selected = state.units == key,
                        onClick = { viewModel.setUnits(key) },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (city.isNotBlank()) viewModel.finishWithCity(city.trim())
                    else viewModel.finishWithLocation()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
            ) {
                Text(if (city.isNotBlank()) stringResource(R.string.start) else stringResource(R.string.use_my_location))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.finishWithCity("Tashkent") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White.copy(alpha = 0.8f)
                )
            ) {
                Text(stringResource(R.string.skip))
            }
        }
    }
}
