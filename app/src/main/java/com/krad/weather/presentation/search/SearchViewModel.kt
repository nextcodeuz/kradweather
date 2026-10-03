package com.krad.weather.presentation.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krad.weather.R
import com.krad.weather.data.repository.WeatherRepository
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.SearchRanker
import com.krad.weather.presentation.common.CitySelectionBus
import com.krad.weather.presentation.home.SearchUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: WeatherRepository,
    private val selectionBus: CitySelectionBus,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var job: Job? = null
    private var lastSubmitted = ""

    init {
        viewModelScope.launch {
            repo.observeSearchHistory().collect { history ->
                _state.value = _state.value.copy(history = history)
            }
        }
    }

    fun onQueryChange(value: String) {
        // Faqat o'zgarganda ishlaymiz (keraksiz so'rovlar = qotishlar manbai)
        if (value == _state.value.query) return
        _state.value = _state.value.copy(query = value)
        job?.cancel()
        if (value.trim().length < 2) {
            _state.value = _state.value.copy(results = emptyList(), loading = false, error = null)
            return
        }
        job = viewModelScope.launch {
            delay(350)
            fetchNow(value.trim())
        }
    }

    /** Klaviaturadagi Search tugmasi — kutmasdan darhol qidiradi. */
    fun submitSearch() {
        val q = _state.value.query.trim()
        if (q.length < 2) return
        if (q == lastSubmitted && _state.value.results.isNotEmpty()) return
        job?.cancel()
        job = viewModelScope.launch { fetchNow(q) }
    }

    fun select(city: CityLocation) {
        viewModelScope.launch { repo.saveSearch(city) }
        selectionBus.select(city)
        // Qaytib kirganda eski so'rov qolmasligi uchun tozalaymiz
        job?.cancel()
        _state.value = _state.value.copy(query = "", results = emptyList(), loading = false, error = null)
        lastSubmitted = ""
    }

    fun useMyLocation() {
        selectionBus.requestLocation()
    }

    fun clearHistory() {
        viewModelScope.launch { repo.clearSearchHistory() }
    }

    private suspend fun fetchNow(query: String) {
        lastSubmitted = query
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { repo.searchCities(query) }
            .onSuccess { list ->
                _state.value = _state.value.copy(
                    results = SearchRanker.rank(query, list),
                    loading = false
                )
            }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: context.getString(R.string.error_generic)
                    )
                }
    }
}
