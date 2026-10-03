package com.krad.weather.presentation.home

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krad.weather.R
import com.krad.weather.domain.model.DailyForecast
import com.krad.weather.domain.model.HourlyForecast
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.krad.weather.presentation.common.rememberCompactHeight
import com.krad.weather.presentation.components.AlertsBanner
import com.krad.weather.presentation.components.AqiCard
import com.krad.weather.presentation.components.DailyForecastRow
import com.krad.weather.presentation.components.DayDetailDialog
import com.krad.weather.presentation.components.DetailsGrid
import com.krad.weather.presentation.components.DressTipCard
import com.krad.weather.presentation.components.GlassCard
import com.krad.weather.presentation.components.HourDetailDialog
import com.krad.weather.presentation.components.HomeTopBar
import com.krad.weather.presentation.components.HourlyForecastRow
import com.krad.weather.presentation.components.SunArc
import com.krad.weather.presentation.components.TemperatureChart
import com.krad.weather.presentation.components.UvCard
import com.krad.weather.presentation.components.WeatherBackground
import com.krad.weather.presentation.components.WeatherIcon
import com.krad.weather.presentation.components.weatherAlerts
import com.krad.weather.presentation.components.WindCompass
import com.krad.weather.util.BackgroundPalette
import com.krad.weather.util.WeatherFormatters

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val units by viewModel.units.collectAsStateWithLifecycle()
    val lang by viewModel.lang.collectAsStateWithLifecycle()
    val dynamicBg by viewModel.dynamicBg.collectAsStateWithLifecycle()
    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!locationPermission.status.isGranted) {
            locationPermission.launchPermissionRequest()
        }
    }

    // Dinamik fon o'chirilgan bo'lsa — tinch tungi gradient
    val palette = if (!dynamicBg) {
        BackgroundPalette.NIGHT
    } else {
        (state as? HomeUiState.Content)
            ?.bundle?.current?.condition?.icon?.let {
                WeatherFormatters.backgroundForIcon(it)
            } ?: BackgroundPalette.NIGHT
    }

    WeatherBackground(palette = palette) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when (val s = state) {
                HomeUiState.Loading -> LoadingView()
                is HomeUiState.Error -> ErrorView(message = s.message, onRetry = { viewModel.retry() })
                is HomeUiState.Content -> PullToRefreshBox(
                    isRefreshing = s.isRefreshing,
                    onRefresh = { viewModel.refresh() }
                ) {
                    ContentView(
                        content = s,
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onMyLocation = { viewModel.loadMyLocation() },
                        onRefresh = { viewModel.refresh() },
                        units = units,
                        lang = lang
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.loading_weather), color = Color.White.copy(alpha = 0.75f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = message, color = Color.White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRetry) { Text("Qayta urinish", color = Color(0xFF90CAF9)) }
        }
    }
}

@Composable
private fun ContentView(
    content: HomeUiState.Content,
    onToggleFavorite: () -> Unit,
    onMyLocation: () -> Unit,
    onRefresh: () -> Unit,
    units: String,
    lang: String
) {
    val bundle = content.bundle
    val current = bundle.current
    val compactHero = rememberCompactHeight()
    val hasAlerts = remember(bundle, lang) { weatherAlerts(bundle, lang).isNotEmpty() }
    val iconSize = if (compactHero) 96.dp else 140.dp
    val tempSize = if (compactHero) 56.sp else 72.sp
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.topbar_share)

    var selectedHour by remember { mutableStateOf<HourlyForecast?>(null) }
    var selectedDay by remember { mutableStateOf<DailyForecast?>(null) }

    fun shareWeather() {
        val today = bundle.forecast.daily.firstOrNull()
        val text = buildString {
            append("${current.city.name}: ")
            append(WeatherFormatters.temp(current.temperature))
            append(", ${current.condition.description}. ")
            if (today != null) {
                append("▲${WeatherFormatters.temp(today.maxTemp)} ▼${WeatherFormatters.temp(today.minTemp)}. ")
            }
            append("— Krad Weather ⛅")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, shareTitle))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
        ) {
        item(key = "topbar") {
            HomeTopBar(
                isFavorite = content.isFavorite,
                onToggleFavorite = onToggleFavorite,
                onMyLocation = onMyLocation,
                onRefresh = onRefresh,
                onShare = { shareWeather() }
            )
        }

        item(key = "hero") {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${current.city.name}, ${current.city.country}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = stringResource(R.string.updated_fmt, content.updatedLabel()) +
                        if (content.fromCache) stringResource(R.string.cached_badge) else "",
                    color = if (content.fromCache) Color(0xFFFFCC80) else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(8.dp))
                WeatherIcon(iconCode = current.condition.icon, size = iconSize, animated = true)
                Text(
                    text = WeatherFormatters.temp(current.temperature),
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    fontSize = tempSize,
                    maxLines = 1
                )
                Text(
                    text = current.condition.description.replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = "▲ ${WeatherFormatters.temp(current.tempMax)}  ▼ ${WeatherFormatters.temp(current.tempMin)}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        if (hasAlerts) {
            item(key = "alerts") {
                AlertsBanner(
                    bundle = bundle,
                    lang = lang,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        if (bundle.forecast.hourly.isNotEmpty()) {
            item(key = "hourly") {
                SectionTitle(stringResource(R.string.section_hourly_tap))
                HourlyForecastRow(
                    items = bundle.forecast.hourly,
                    timezoneOffsetSeconds = current.timezoneOffsetSeconds,
                    onItemClick = { selectedHour = it }
                )
                Spacer(Modifier.height(16.dp))
            }

            item(key = "chart") {
                TemperatureChart(
                    items = bundle.forecast.hourly,
                    timezoneOffsetSeconds = current.timezoneOffsetSeconds,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "sun") {
            SunArc(
                current = current,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        item(key = "wind") {
            WindCompass(
                current = current,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        item(key = "dress") {
            val tempC = WeatherFormatters.convertTemp(current.temperature, units, "metric")
            val maxPop = bundle.forecast.hourly.maxOfOrNull { it.pop } ?: 0.0
            DressTipCard(
                tempC = tempC,
                pop = maxPop,
                windSpeed = current.windSpeed,
                modifier = Modifier.padding(horizontal = 16.dp),
                lang = lang,
                title = stringResource(R.string.dress_title)
            )
            Spacer(Modifier.height(16.dp))
        }

        content.airQuality?.let { aqi ->
            item(key = "aqi") {
                AqiCard(aqi = aqi, modifier = Modifier.padding(horizontal = 16.dp), lang = lang)
                Spacer(Modifier.height(16.dp))
            }
        }

        current.uvIndex?.let { uv ->
            item(key = "uv") {
                UvCard(uv = uv, modifier = Modifier.padding(horizontal = 16.dp), lang = lang)
                Spacer(Modifier.height(16.dp))
            }
        }

        item(key = "details") {
            SectionTitle(stringResource(R.string.section_details))
            DetailsGrid(current = current, modifier = Modifier.padding(horizontal = 16.dp), units = units)
            Spacer(Modifier.height(20.dp))
        }

        if (bundle.forecast.daily.isNotEmpty()) {
            item(key = "daily") {
                SectionTitle(
                    stringResource(
                        if (bundle.forecast.daily.size > 5) R.string.forecast_7day
                        else R.string.forecast_5day
                    )
                )
                GlassCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Column {
                        bundle.forecast.daily.forEach { day ->
                            DailyForecastRow(
                                item = day,
                                modifier = Modifier.clickable { selectedDay = day },
                                lang = lang
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    selectedHour?.let { hour ->
        HourDetailDialog(
            hour = hour,
            timezoneOffsetSeconds = current.timezoneOffsetSeconds,
            onDismiss = { selectedHour = null }
        )
    }
    selectedDay?.let { day ->
        DayDetailDialog(
            day = day,
            onDismiss = { selectedDay = null },
            lang = lang
        )
    }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        maxLines = 1,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}
