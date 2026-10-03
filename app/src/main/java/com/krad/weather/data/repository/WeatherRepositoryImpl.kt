package com.krad.weather.data.repository

import com.google.gson.Gson
import com.krad.weather.BuildConfig
import com.krad.weather.data.local.CachedWeather
import com.krad.weather.data.local.CachedWeatherDao
import com.krad.weather.data.local.FavoriteCity
import com.krad.weather.data.local.FavoriteCityDao
import com.krad.weather.data.local.SearchHistoryDao
import com.krad.weather.data.local.SearchHistoryItem
import com.krad.weather.data.remote.AirPollutionApi
import com.krad.weather.data.remote.GeoApi
import com.krad.weather.data.remote.KradWeatherApi
import com.krad.weather.data.remote.WeatherApiCom
import com.krad.weather.data.remote.toAppError
import com.krad.weather.domain.model.AirQuality
import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.FavoriteWithWeather
import com.krad.weather.domain.model.UzWeatherTranslator
import com.krad.weather.domain.model.WapiExtras
import com.krad.weather.domain.model.WeatherBundle
import com.krad.weather.domain.model.WeatherResult
import com.krad.weather.domain.model.toDomain
import com.krad.weather.domain.model.toExtras
import com.krad.weather.presentation.settings.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: KradWeatherApi,
    private val geo: GeoApi,
    private val air: AirPollutionApi,
    private val wapi: WeatherApiCom,
    private val favorites: FavoriteCityDao,
    private val cache: CachedWeatherDao,
    private val history: SearchHistoryDao,
    private val gson: Gson,
    private val prefs: UserPreferences
) : WeatherRepository {

    /** Kesh payload: to'plam + ichki AQI (eski keshlar bilan mos). */
    private data class CachedPayload(val bundle: WeatherBundle?, val aqi: AirQuality?)

    override suspend fun weatherByCity(city: String, units: String, lang: String): WeatherResult {
        val cacheKey = "city:${city.trim().lowercase()}|$units|$lang"
        return fetchWithCache(cacheKey, queryForError = city, lang = lang) {
            fetchFresh(city = city, lat = null, lon = null, units = units, lang = lang)
        }
    }

    override suspend fun weatherByCoords(lat: Double, lon: Double, units: String, lang: String): WeatherResult {
        val cacheKey = "coords:%.3f,%.3f|%s|%s".format(lat, lon, units, lang)
        return fetchWithCache(cacheKey, queryForError = null, lang = lang) {
            fetchFresh(city = null, lat = lat, lon = lon, units = units, lang = lang)
        }
    }

    /**
     * 1) OpenWeatherMap ASOSIY — joriy + prognoz (parallel).
     * 2) Xato bo'lsa — WeatherAPI.com to'liq zaxirasi.
     */
    private suspend fun fetchFresh(
        city: String?,
        lat: Double?,
        lon: Double?,
        units: String,
        lang: String
    ): Pair<WeatherBundle, AirQuality?> {
        runCatching {
            return fetchOwmPrimary(city, lat, lon, units, lang)
        }
        return fetchWapiFull(city, lat, lon, units, lang)
    }

    private suspend fun fetchOwmPrimary(
        city: String?,
        lat: Double?,
        lon: Double?,
        units: String,
        lang: String
    ): Pair<WeatherBundle, AirQuality?> = coroutineScope {
        val cur = async {
            if (city != null) api.getCurrentWeatherByCity(city, units, lang).toDomain()
            else api.getCurrentWeatherByCoords(lat!!, lon!!, units, lang).toDomain()
        }
        val fc = async {
            if (city != null) api.getForecastByCity(city, units, lang).toDomain()
            else api.getForecastByCoords(lat!!, lon!!, units, lang).toDomain()
        }
        val bundle = WeatherBundle(cur.await(), fc.await())
        val translated = if (lang == "uz") UzWeatherTranslator.translateBundle(bundle) else bundle
        translated to null
    }

    private suspend fun fetchWapiFull(
        city: String?,
        lat: Double?,
        lon: Double?,
        units: String,
        lang: String
    ): Pair<WeatherBundle, AirQuality?> {
        val q = city ?: "$lat,$lon"
        val w = wapi.forecast(
            key = BuildConfig.WEATHERAPI_KEY,
            query = q,
            days = 8,
            aqi = "yes",
            alerts = "yes",
            lang = lang
        ).toDomain(units)
        val bundle = if (lang == "uz") {
            UzWeatherTranslator.translateBundle(w.bundle)
        } else w.bundle
        return bundle to w.aqi
    }

    override suspend fun wapiExtras(lat: Double, lon: Double, lang: String): WapiExtras? =
        withContext(Dispatchers.IO) {
            runCatching {
                wapi.forecast(
                    key = BuildConfig.WEATHERAPI_KEY,
                    query = "$lat,$lon",
                    days = 1,
                    aqi = "yes",
                    alerts = "yes",
                    lang = lang
                ).toExtras()
            }.getOrNull()
        }

    override suspend fun cachedByCity(city: String, units: String, lang: String): WeatherResult? =
        readCache("city:${city.trim().lowercase()}|$units|$lang")

    override suspend fun cachedByCoords(lat: Double, lon: Double, units: String, lang: String): WeatherResult? =
        readCache("coords:%.3f,%.3f|%s|%s".format(lat, lon, units, lang))

    override suspend fun cachedForLastLocation(): WeatherResult? {
        val units = prefs.currentUnits()
        val lang = prefs.currentLang()
        val city = prefs.lastCity.first()
        val coords = prefs.lastCoords()
        if (city.isNullOrBlank() && coords == null) return null
        return if (coords != null) {
            cachedByCoords(coords.first, coords.second, units, lang)
        } else {
            cachedByCity(city!!, units, lang)
        }
    }

    override suspend fun airQuality(lat: Double, lon: Double): AirQuality? = withContext(Dispatchers.IO) {
        val key = "aqi:%.3f,%.3f".format(lat, lon)
        try {
            val fresh = air.currentAirPollution(lat, lon).toDomain()
            if (fresh != null) {
                cache.put(CachedWeather(key, gson.toJson(fresh), System.currentTimeMillis()))
            }
            fresh
        } catch (t: Throwable) {
            cache.get(key)?.let {
                runCatching { gson.fromJson(it.payloadJson, AirQuality::class.java) }.getOrNull()
            }
        }
    }

    override suspend fun searchCities(query: String): List<CityLocation> {
        val lang = prefs.currentLang()
        // 1) OpenWeather Geocoding ASOSIY
        runCatching {
            geo.geocodeCity(query.trim()).map { it.toDomain() }
                .filter { it.name.isNotBlank() }
                .takeIf { it.isNotEmpty() }
        }.getOrNull()?.let { return it }
        // 2) WeatherAPI.com qidiruvi zaxira (mintaqa bilan)
        runCatching {
            wapi.search(BuildConfig.WEATHERAPI_KEY, query.trim())
                .map {
                    CityLocation(
                        name = it.name ?: "",
                        country = it.country ?: "",
                        state = it.region,
                        lat = it.lat ?: 0.0,
                        lon = it.lon ?: 0.0
                    )
                }
                .filter { it.name.isNotBlank() }
                .takeIf { it.isNotEmpty() }
        }.getOrNull()?.let { return it }
        // 3) Ikkalasi ham ishlamasa — xatolik
        return try {
            geo.geocodeCity(query.trim()).map { it.toDomain() }
        } catch (t: Throwable) {
            throw t.toAppError(query, lang)
        }
    }

    override suspend fun reverseGeocode(lat: Double, lon: Double): CityLocation? {
        return try {
            geo.reverseGeocode(lat, lon).firstOrNull()?.toDomain()
        } catch (t: Throwable) {
            null
        }
    }

    override fun observeFavorites(): Flow<List<CityLocation>> =
        favorites.observeAll().map { list ->
            list.map { CityLocation(it.name, it.country, it.state, it.lat, it.lon) }
        }

    override fun observeFavoritesWithWeather(): Flow<List<FavoriteWithWeather>> =
        favorites.observeAll().map { list ->
            list.map {
                FavoriteWithWeather(
                    CityLocation(it.name, it.country, it.state, it.lat, it.lon),
                    it.lastTemp,
                    it.lastIcon,
                    it.tempUnits
                )
            }
        }

    override suspend fun isFavorite(city: CityLocation): Boolean =
        favorites.findById(favId(city)) != null

    override suspend fun addFavorite(city: CityLocation) {
        val existing = favorites.findById(favId(city))
        favorites.upsert(
            FavoriteCity(
                id = favId(city),
                name = city.name,
                country = city.country,
                state = city.state,
                lat = city.lat,
                lon = city.lon,
                addedAt = existing?.addedAt ?: System.currentTimeMillis(),
                lastTemp = existing?.lastTemp,
                lastIcon = existing?.lastIcon,
                lastUpdated = existing?.lastUpdated,
                tempUnits = existing?.tempUnits ?: "metric"
            )
        )
    }

    override suspend fun removeFavorite(city: CityLocation) {
        favorites.deleteById(favId(city))
    }

    override suspend fun updateFavoriteSnapshot(city: CityLocation, temp: Double, icon: String, units: String) {
        val existing = favorites.findById(favId(city)) ?: return
        favorites.upsert(
            existing.copy(
                lastTemp = temp,
                lastIcon = icon,
                lastUpdated = System.currentTimeMillis(),
                tempUnits = units
            )
        )
    }

    override fun observeSearchHistory(): Flow<List<CityLocation>> =
        history.observeRecent().map { list ->
            list.map { CityLocation(it.name, it.country, it.state, it.lat, it.lon) }
        }

    override suspend fun saveSearch(city: CityLocation) {
        history.upsert(
            SearchHistoryItem(
                id = favId(city),
                name = city.name,
                country = city.country,
                state = city.state,
                lat = city.lat,
                lon = city.lon
            )
        )
        history.purgeOlderThan(System.currentTimeMillis() - 30L * 24 * 3600 * 1000)
    }

    override suspend fun clearSearchHistory() = history.clear()

    private fun favId(city: CityLocation): String =
        "${city.name.lowercase()}|${city.country.lowercase()}|%.4f|%.4f".format(city.lat, city.lon)

    private suspend fun fetchWithCache(
        cacheKey: String,
        queryForError: String?,
        lang: String,
        fetcher: suspend () -> Pair<WeatherBundle, AirQuality?>
    ): WeatherResult = withContext(Dispatchers.IO) {
        try {
            val (bundle, aqi) = fetcher()
            val now = System.currentTimeMillis()
            cache.put(CachedWeather(cacheKey, gson.toJson(CachedPayload(bundle, aqi)), now))
            cache.purgeOlderThan(now - CACHE_TTL_MS * 4)
            WeatherResult(bundle, fromCache = false, updatedAt = now, aqi = aqi)
        } catch (t: Throwable) {
            val cached = readCache(cacheKey)
            if (cached != null) cached.copy(fromCache = true)
            else throw t.toAppError(queryForError, lang)
        }
    }

    private suspend fun readCache(cacheKey: String): WeatherResult? = withContext(Dispatchers.IO) {
        val cached = cache.get(cacheKey)
            ?: return@withContext null
        if (System.currentTimeMillis() - cached.updatedAt > CACHE_TTL_MS * 4) return@withContext null
        // Yangi format (bundle + aqi)
        runCatching {
            val payload = gson.fromJson(cached.payloadJson, CachedPayload::class.java)
            val bundle = payload.bundle ?: return@runCatching null
            WeatherResult(bundle, fromCache = true, updatedAt = cached.updatedAt, aqi = payload.aqi)
        }.getOrNull() ?: runCatching {
            // Eski format (faqat bundle) — moslik uchun
            val bundle = gson.fromJson(cached.payloadJson, WeatherBundle::class.java)
            WeatherResult(bundle, fromCache = true, updatedAt = cached.updatedAt)
        }.getOrNull()
    }

    companion object {
        const val CACHE_TTL_MS = 30 * 60 * 1000L
    }
}
