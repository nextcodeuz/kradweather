package com.krad.weather.domain.model

import java.time.Instant
import java.time.ZoneOffset

data class CityLocation(
    val name: String,
    val country: String,
    val state: String? = null,
    val lat: Double,
    val lon: Double
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class CurrentWeather(
    val city: CityLocation,
    val condition: WeatherCondition,
    val temperature: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Int,
    val humidity: Int,
    val visibility: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val windGust: Double?,
    val clouds: Int,
    val rainLastHour: Double?,
    val snowLastHour: Double?,
    val rainLast3h: Double?,
    val snowLast3h: Double?,
    val sunrise: Long,
    val sunset: Long,
    val timezoneOffsetSeconds: Int,
    val dt: Long,
    /** Faqat WeatherAPI.com beradi (OWM bepulda yo'q). */
    val uvIndex: Double? = null
) {
    val localTime: Instant get() = Instant.ofEpochSecond(dt)
    val localZone: ZoneOffset get() = ZoneOffset.ofTotalSeconds(timezoneOffsetSeconds)
    val precipMm: Double get() = (rainLastHour ?: rainLast3h ?: 0.0) + (snowLastHour ?: snowLast3h ?: 0.0)
}

data class HourlyForecast(
    val dt: Long,
    val temperature: Double,
    val feelsLike: Double,
    val condition: WeatherCondition,
    val pop: Double,
    val windSpeed: Double,
    val humidity: Int,
    val rainMm: Double? = null
)

data class DailyForecast(
    val dateEpochDay: Long,
    val minTemp: Double,
    val maxTemp: Double,
    val condition: WeatherCondition,
    val pop: Double,
    val humidity: Int,
    val windSpeed: Double,
    val rainMm: Double?,
    val sunrise: Long?,
    val sunset: Long?
)

data class ForecastBundle(
    /** Keyingi ~24 soat: 3-soatlik qadamda 8 ta nuqta. */
    val hourly: List<HourlyForecast>,
    /** Bepul tarifda maksimal 5 kun. */
    val daily: List<DailyForecast>
)

data class WeatherBundle(
    val current: CurrentWeather,
    val forecast: ForecastBundle,
    /** WeatherAPI.com rasmiy ogohlantirishlari (bo'lsa). */
    val officialAlerts: List<SmartAlert> = emptyList()
)

/** WeatherAPI.com'dan OWM'da yo'q qo'shimchalar: UV + AQI + rasmiy alerts. */
data class WapiExtras(
    val uv: Double? = null,
    val aqi: AirQuality? = null,
    val alerts: List<SmartAlert> = emptyList()
)

/** Repository natijasi: keshdanmi, qachon yangilangan + ichki AQI. */
data class WeatherResult(
    val bundle: WeatherBundle,
    val fromCache: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val aqi: AirQuality? = null
)

data class AirQuality(
    val aqi: Int,
    val co: Double?,
    val no2: Double?,
    val o3: Double?,
    val so2: Double?,
    val pm25: Double?,
    val pm10: Double?
) {
    fun label(lang: String = "uz"): String = LangText.aqiLabel(lang, aqi)
}

data class FavoriteWithWeather(
    val city: CityLocation,
    val temp: Double? = null,
    val icon: String? = null,
    val tempUnits: String = "metric"
)
