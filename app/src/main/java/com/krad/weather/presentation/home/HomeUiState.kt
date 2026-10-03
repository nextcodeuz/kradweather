package com.krad.weather.presentation.home

import com.krad.weather.domain.model.AirQuality
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.FavoriteWithWeather
import com.krad.weather.domain.model.WeatherBundle
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String, val canRetry: Boolean = true) : HomeUiState
    data class Content(
        val bundle: WeatherBundle,
        val fromCache: Boolean = false,
        val isFavorite: Boolean = false,
        val airQuality: AirQuality? = null,
        val lastUpdated: Long = System.currentTimeMillis(),
        val isRefreshing: Boolean = false
    ) : HomeUiState
}

data class SearchUiState(
    val query: String = "",
    val results: List<CityLocation> = emptyList(),
    val history: List<CityLocation> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class FavoritesUiState(
    val cities: List<FavoriteWithWeather> = emptyList(),
    val units: String = "metric"
)

fun HomeUiState.Content.updatedLabel(): String {
    val current = bundle.current
    val zone = ZoneOffset.ofTotalSeconds(current.timezoneOffsetSeconds)
    val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    return Instant.ofEpochMilli(lastUpdated).atZone(zone).format(fmt)
}
