package com.krad.weather.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krad.weather.presentation.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(val units: String = "metric")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.units.collect { _state.value = _state.value.copy(units = it) }
        }
    }

    fun setUnits(v: String) = viewModelScope.launch { prefs.setUnits(v) }

    fun finishWithCity(city: String) {
        viewModelScope.launch {
            prefs.setLastCity(city)
            prefs.setOnboardingDone(true)
        }
    }

    fun finishWithLocation() {
        viewModelScope.launch {
            prefs.setLastCity(null)
            prefs.setOnboardingDone(true)
        }
    }
}
