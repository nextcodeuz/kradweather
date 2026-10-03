package com.krad.weather.data.repository

import com.krad.weather.domain.model.AirQuality
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.FavoriteWithWeather
import com.krad.weather.domain.model.WapiExtras
import com.krad.weather.domain.model.WeatherResult
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun weatherByCity(city: String, units: String, lang: String): WeatherResult
    suspend fun weatherByCoords(lat: Double, lon: Double, units: String, lang: String): WeatherResult

    /** Keshlangan nusxa (bo'lsa). Bir zumda ko'rsatish uchun. */
    suspend fun cachedByCity(city: String, units: String, lang: String): WeatherResult?
    suspend fun cachedByCoords(lat: Double, lon: Double, units: String, lang: String): WeatherResult?

    /** Vidjet/worker uchun: oxirgi shaharning keshlangan ob-havosi. */
    suspend fun cachedForLastLocation(): WeatherResult?

    suspend fun airQuality(lat: Double, lon: Double): AirQuality?

    /**
     * WeatherAPI.com'dan FAQAT OWM'da yo'q narsalar (UV + ichki AQI +
     * rasmiy alerts). Yengil so'rov, kontentni to'smaydi.
     */
    suspend fun wapiExtras(lat: Double, lon: Double, lang: String): WapiExtras?

    suspend fun searchCities(query: String): List<CityLocation>
    suspend fun reverseGeocode(lat: Double, lon: Double): CityLocation?

    fun observeFavorites(): Flow<List<CityLocation>>
    fun observeFavoritesWithWeather(): Flow<List<FavoriteWithWeather>>
    suspend fun isFavorite(city: CityLocation): Boolean
    suspend fun addFavorite(city: CityLocation)
    suspend fun removeFavorite(city: CityLocation)
    suspend fun updateFavoriteSnapshot(city: CityLocation, temp: Double, icon: String, units: String)

    fun observeSearchHistory(): Flow<List<CityLocation>>
    suspend fun saveSearch(city: CityLocation)
    suspend fun clearSearchHistory()
}
