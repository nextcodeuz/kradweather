package com.krad.weather.widget

import com.krad.weather.data.repository.WeatherRepository
import com.krad.weather.presentation.settings.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefresh @Inject constructor(
    private val repo: WeatherRepository,
    private val prefs: UserPreferences,
    private val updater: KradWidgetUpdater
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    operator fun invoke() {
        scope.launch {
            val city = prefs.lastCity.first() ?: return@launch
            val coords = prefs.lastCoords()
            val units = prefs.currentUnits()
            val lang = prefs.currentLang()
            val result = runCatching {
                if (coords != null) repo.weatherByCoords(coords.first, coords.second, units, lang)
                else repo.weatherByCity(city, units, lang)
            }.getOrNull() ?: return@launch
            updater.updateAll(result.bundle)
        }
    }
}
