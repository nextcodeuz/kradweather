package com.krad.weather.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krad.weather.R
import com.krad.weather.data.repository.WeatherRepository
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.WeatherResult
import com.krad.weather.presentation.common.CitySelectionBus
import com.krad.weather.presentation.settings.UserPreferences
import com.krad.weather.util.LocationProvider
import com.krad.weather.widget.KradWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val prefs: UserPreferences,
    private val selectionBus: CitySelectionBus,
    private val widgetUpdater: KradWidgetUpdater,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var lastCity: CityLocation? = null
    private var lastUnits = "metric"
    private var lastLang = "uz"
    private var loadJob: Job? = null

    private val _units = MutableStateFlow("metric")
    val units: StateFlow<String> = _units.asStateFlow()

    private val _lang = MutableStateFlow("uz")
    val lang: StateFlow<String> = _lang.asStateFlow()

    private val _dynamicBg = MutableStateFlow(true)
    val dynamicBg: StateFlow<Boolean> = _dynamicBg.asStateFlow()

    /** Oxirgi so'rov — xatolikdan keyin aynan o'shani qaytarish uchun (stress-bardosh). */
    private var lastRequest: Req? = null

    private sealed interface Req {
        data class City(val name: String) : Req
        data class Coords(val name: String, val lat: Double, val lon: Double) : Req
        data object Mine : Req
    }

    init {
        viewModelScope.launch {
            prefs.dynamicBackground.collect { _dynamicBg.value = it }
        }
        viewModelScope.launch {
            prefs.units.collect { units ->
                _units.value = units
                if (units != lastUnits) {
                    lastUnits = units
                    lastCity?.let { reload(it) }
                } else lastUnits = units
            }
        }
        viewModelScope.launch {
            prefs.language.collect { lang ->
                _lang.value = lang
                if (lang != lastLang) {
                    lastLang = lang
                    lastCity?.let { reload(it) }
                } else lastLang = lang
            }
        }
        viewModelScope.launch {
            lastUnits = prefs.currentUnits()
            lastLang = prefs.currentLang()
            val saved = prefs.lastCity.first()
            val coords = prefs.lastCoords()
            if (!saved.isNullOrBlank() && coords != null) {
                loadCity(saved, coords.first, coords.second, showCacheFirst = true)
            } else if (!saved.isNullOrBlank()) {
                loadCity(saved, showCacheFirst = true)
            } else {
                loadMyLocation()
            }
        }
        viewModelScope.launch {
            selectionBus.selected.collect { city ->
                repo.saveSearch(city)
                loadCity(city.name, city.lat, city.lon)
            }
        }
        viewModelScope.launch {
            selectionBus.locationRequested.collect {
                loadMyLocation()
            }
        }
    }

    fun refresh() {
        when (val req = lastRequest) {
            is Req.City -> loadCity(req.name)
            is Req.Coords -> loadCity(req.name, req.lat, req.lon)
            is Req.Mine -> loadMyLocation()
            null -> {
                val current = (_state.value as? HomeUiState.Content)?.bundle?.current?.city
                    ?: lastCity ?: return
                reload(current)
            }
        }
    }

    /** Xatolik ekranidagi «Qayta urinish» — keshdan tez ko'rsatib, tarmoqdan yangilaydi. */
    fun retry() {
        when (val req = lastRequest) {
            is Req.City -> loadCity(req.name, showCacheFirst = true)
            is Req.Coords -> loadCity(req.name, req.lat, req.lon, showCacheFirst = true)
            is Req.Mine, null -> loadMyLocation()
        }
    }

    fun loadCity(city: String, showCacheFirst: Boolean = false) {
        lastRequest = Req.City(city)
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val units = prefs.currentUnits()
            val lang = prefs.currentLang()
            lastUnits = units
            lastLang = lang
            if (showCacheFirst) {
                repo.cachedByCity(city, units, lang)?.let { showResult(it, units, lang) }
                    ?: run { _state.value = HomeUiState.Loading }
            } else {
                markRefreshingOrLoading()
            }
            runCatching { repo.weatherByCity(city, units, lang) }
                .onSuccess { showResult(it, units, lang) }
                .onFailure { e ->
                    if (_state.value !is HomeUiState.Content) {
                        _state.value = HomeUiState.Error(e.message ?: context.getString(R.string.error_generic))
                    }
                }
        }
    }

    fun loadCity(name: String, lat: Double, lon: Double, showCacheFirst: Boolean = false) {
        lastRequest = Req.Coords(name, lat, lon)
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val units = prefs.currentUnits()
            val lang = prefs.currentLang()
            lastUnits = units
            lastLang = lang
            if (showCacheFirst) {
                repo.cachedByCoords(lat, lon, units, lang)?.let { showResult(it, units, lang) }
                    ?: run { _state.value = HomeUiState.Loading }
            } else {
                markRefreshingOrLoading()
            }
            runCatching { repo.weatherByCoords(lat, lon, units, lang) }
                .onSuccess { showResult(it, units, lang) }
                .onFailure { e ->
                    if (_state.value !is HomeUiState.Content) {
                        _state.value = HomeUiState.Error(e.message ?: context.getString(R.string.error_generic))
                    }
                }
        }
    }

    fun loadMyLocation() {
        lastRequest = Req.Mine
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = HomeUiState.Loading
            // GPS uzoq kuttirmasligi uchun timeout: topilmasa kesh/saqlangan shahar
            val coords = withTimeoutOrNull(9000) {
                locationProvider.currentLatLon()
            }
            if (coords == null) {
                // Lokatsiya yo'q bo'lsa oxirgi shahar yoki poytaxt
                val saved = prefs.lastCity.first()
                if (!saved.isNullOrBlank()) loadCity(saved)
                else loadCity("Tashkent")
                return@launch
            }
            val units = prefs.currentUnits()
            val lang = prefs.currentLang()
            repo.cachedByCoords(coords.first, coords.second, units, lang)?.let {
                showResult(it, units, lang)
            }
            runCatching { repo.weatherByCoords(coords.first, coords.second, units, lang) }
                .onSuccess { showResult(it, units, lang) }
                .onFailure { e ->
                    if (_state.value !is HomeUiState.Content) {
                        _state.value = HomeUiState.Error(e.message ?: context.getString(R.string.location_failed))
                    }
                }
        }
    }

    fun toggleFavorite() {
        val content = _state.value as? HomeUiState.Content ?: return
        viewModelScope.launch {
            val city = content.bundle.current.city
            if (content.isFavorite) repo.removeFavorite(city) else {
                repo.addFavorite(city)
                repo.updateFavoriteSnapshot(
                    city,
                    content.bundle.current.temperature,
                    content.bundle.current.condition.icon,
                    lastUnits
                )
            }
            _state.value = content.copy(isFavorite = !content.isFavorite)
        }
    }

    private fun reload(city: CityLocation) {
        if (city.lat != 0.0 || city.lon != 0.0) loadCity(city.name, city.lat, city.lon)
        else loadCity(city.name)
    }

    private fun markRefreshingOrLoading() {
        val content = _state.value as? HomeUiState.Content
        _state.value = if (content != null) content.copy(isRefreshing = true)
        else HomeUiState.Loading
    }

    private suspend fun showResult(result: WeatherResult, units: String, lang: String) {
        val bundle = result.bundle
        val city = bundle.current.city
        lastCity = city
        prefs.setLastCity(city.name)
        prefs.setLastCoords(city.lat, city.lon)
        val fav = repo.isFavorite(city)
        if (fav) {
            runCatching {
                repo.updateFavoriteSnapshot(
                    city,
                    bundle.current.temperature,
                    bundle.current.condition.icon,
                    units
                )
            }
        }
        // Kontentni DARHOL ko'rsatamiz (WeatherAPI AQI ichida keladi,
        // OWM zaxirasida bo'lsa — alohida fonda yuklanadi, qotish yo'q)
        _state.value = HomeUiState.Content(
            bundle = bundle,
            fromCache = result.fromCache,
            isFavorite = fav,
            airQuality = result.aqi,
            lastUpdated = result.updatedAt,
            isRefreshing = false
        )
        // Vidjet yangilanishi — ilova har ochilganda (Worker'ga bog'liq emas)
        if (!result.fromCache) {
            runCatching { widgetUpdater.updateAll(bundle) }
        }
        if (result.aqi != null) return
        viewModelScope.launch {
            // WeatherAPI'dan FAQAT yo'q narsalar: UV + ichki AQI + rasmiy alerts.
            // Topilmasa — OWM havo sifati.
            val extras = runCatching {
                repo.wapiExtras(city.lat, city.lon, lastLang)
            }.getOrNull()
            val aqi = extras?.aqi
                ?: runCatching { repo.airQuality(city.lat, city.lon) }.getOrNull()
            val cur = _state.value as? HomeUiState.Content ?: return@launch
            if (cur.bundle !== bundle) return@launch
            val merged = if (extras != null) {
                cur.bundle.copy(
                    current = cur.bundle.current.copy(
                        uvIndex = cur.bundle.current.uvIndex ?: extras.uv
                    ),
                    officialAlerts = (extras.alerts + cur.bundle.officialAlerts)
                        .distinctBy { it.title to it.text }.take(3)
                )
            } else cur.bundle
            _state.value = cur.copy(bundle = merged, airQuality = aqi ?: cur.airQuality)
        }
    }
}
