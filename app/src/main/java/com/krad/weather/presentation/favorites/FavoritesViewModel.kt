package com.krad.weather.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krad.weather.data.repository.WeatherRepository
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.presentation.common.CitySelectionBus
import com.krad.weather.presentation.home.FavoritesUiState
import com.krad.weather.presentation.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repo: WeatherRepository,
    private val selectionBus: CitySelectionBus,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeFavoritesWithWeather().collect { cities ->
                _state.value = _state.value.copy(cities = cities)
            }
        }
        viewModelScope.launch {
            prefs.units.collect { units ->
                _state.value = _state.value.copy(units = units)
            }
        }
    }

    fun remove(city: CityLocation) {
        viewModelScope.launch { repo.removeFavorite(city) }
    }

    fun select(city: CityLocation) {
        selectionBus.select(city)
    }
}
