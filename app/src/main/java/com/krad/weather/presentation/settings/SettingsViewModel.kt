package com.krad.weather.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krad.weather.util.LocaleHelper
import com.krad.weather.worker.WeatherWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val units: String = "metric",
    val language: String = "uz",
    val dynamicBackground: Boolean = true,
    val autoRefresh: Boolean = true,
    val theme: String = "system",
    val notifications: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { prefs.units.collect { _state.value = _state.value.copy(units = it) } }
        viewModelScope.launch { prefs.language.collect { _state.value = _state.value.copy(language = it) } }
        viewModelScope.launch { prefs.dynamicBackground.collect { _state.value = _state.value.copy(dynamicBackground = it) } }
        viewModelScope.launch { prefs.autoRefresh.collect { _state.value = _state.value.copy(autoRefresh = it) } }
        viewModelScope.launch { prefs.theme.collect { _state.value = _state.value.copy(theme = it) } }
        viewModelScope.launch { prefs.notificationsEnabled.collect { _state.value = _state.value.copy(notifications = it) } }
    }

    fun setUnits(v: String) = viewModelScope.launch { prefs.setUnits(v) }
    fun setLanguage(v: String) = viewModelScope.launch {
        prefs.setLanguage(v)
        // Ilova tilini darhol almashtirish (barcha Android versiyada ishlaydi)
        LocaleHelper.setLangAndRestart(context, v)
    }
    fun setDynamicBackground(v: Boolean) = viewModelScope.launch { prefs.setDynamicBackground(v) }
    fun setAutoRefresh(v: Boolean) = viewModelScope.launch {
        prefs.setAutoRefresh(v)
        if (v) WeatherWorker.schedule(context) else WeatherWorker.cancel(context)
    }
    fun setTheme(v: String) = viewModelScope.launch { prefs.setTheme(v) }
    fun setNotifications(v: Boolean) = viewModelScope.launch {
        prefs.setNotificationsEnabled(v)
        if (v) WeatherWorker.schedule(context)
    }
}
