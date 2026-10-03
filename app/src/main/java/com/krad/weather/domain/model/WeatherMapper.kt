package com.krad.weather.domain.model

import com.krad.weather.data.remote.dto.AirPollutionResponse
import com.krad.weather.data.remote.dto.CityDto
import com.krad.weather.data.remote.dto.CurrentWeatherDto
import com.krad.weather.data.remote.dto.ForecastDto
import com.krad.weather.data.remote.dto.ForecastItemDto
import com.krad.weather.data.remote.dto.GeoCityDto
import com.krad.weather.data.remote.dto.WeatherConditionDto
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private fun WeatherConditionDto.toDomain(): WeatherCondition = WeatherCondition(
    id = id ?: 800,
    main = main ?: "Clear",
    description = description ?: "",
    icon = icon ?: "01d"
)

fun GeoCityDto.toDomain(): CityLocation = CityLocation(
    name = name ?: "",
    country = country ?: "",
    state = state,
    lat = lat ?: 0.0,
    lon = lon ?: 0.0
)

fun CurrentWeatherDto.toDomain(): CurrentWeather {
    val main = this.main ?: error("main missing")
    val condition = weather?.firstOrNull()?.toDomain() ?: WeatherCondition(800, "Clear", "", "01d")
    val cityName = name ?: ""
    val country = sys?.country ?: ""
    return CurrentWeather(
        city = CityLocation(
            name = cityName,
            country = country,
            state = null,
            lat = coord?.lat ?: 0.0,
            lon = coord?.lon ?: 0.0
        ),
        condition = condition,
        temperature = main.temp ?: 0.0,
        feelsLike = main.feelsLike ?: 0.0,
        tempMin = main.tempMin ?: 0.0,
        tempMax = main.tempMax ?: 0.0,
        pressure = main.pressure ?: 0,
        humidity = main.humidity ?: 0,
        visibility = visibility ?: 0,
        windSpeed = wind?.speed ?: 0.0,
        windDeg = wind?.deg ?: 0,
        windGust = wind?.gust,
        clouds = clouds?.all ?: 0,
        rainLastHour = rain?.oneHour ?: rain?.threeHours,
        snowLastHour = snow?.oneHour ?: snow?.threeHours,
        rainLast3h = rain?.threeHours,
        snowLast3h = snow?.threeHours,
        sunrise = sys?.sunrise ?: 0L,
        sunset = sys?.sunset ?: 0L,
        timezoneOffsetSeconds = timezone ?: 0,
        dt = dt ?: 0L
    )
}

fun ForecastDto.toDomain(): ForecastBundle {
    val items = list ?: emptyList()
    val tzSeconds = city?.timezone ?: 0
    val zone = ZoneOffset.ofTotalSeconds(tzSeconds)

    fun localDateOf(epochSeconds: Long): LocalDate =
        Instant.ofEpochSecond(epochSeconds).atZone(zone).toLocalDate()

    // 3-soatlik qadamda keyingi 8 ta nuqta = ~24 soat
    val hourly = items.take(8).map { it.toHourly() }

    val daily = items
        .groupBy { localDateOf(it.dt ?: 0L) }
        .toSortedMap()
        .map { (date, dayItems) ->
            val minT = dayItems.minOfOrNull { it.main?.tempMin ?: it.main?.temp ?: 0.0 } ?: 0.0
            val maxT = dayItems.maxOfOrNull { it.main?.tempMax ?: it.main?.temp ?: 0.0 } ?: 0.0
            // Kunduzgi (eng issiq) nuqta ikonasi kunni yaxshiroq ifodalaydi
            val repr = dayItems.maxByOrNull { it.main?.temp ?: Double.NEGATIVE_INFINITY }
            val cond = repr?.weather?.firstOrNull()?.toDomain()
                ?: WeatherCondition(800, "Clear", "", "01d")
            val pops = dayItems.mapNotNull { it.pop }.maxOrNull() ?: 0.0
            val humAvg = dayItems.mapNotNull { it.main?.humidity }.average()
            val hum = if (humAvg.isNaN()) 0 else humAvg.toInt()
            val wind = dayItems.mapNotNull { it.wind?.speed }.maxOrNull() ?: 0.0
            val rain = dayItems.mapNotNull { it.rain?.threeHours }.sum().takeIf { it > 0 }
            DailyForecast(
                dateEpochDay = date.toEpochDay(),
                minTemp = minT,
                maxTemp = maxT,
                condition = cond,
                pop = pops,
                humidity = hum,
                windSpeed = wind,
                rainMm = rain,
                sunrise = null,
                sunset = null
            )
        }
        .take(5)

    return ForecastBundle(hourly = hourly, daily = daily)
}

private fun ForecastItemDto.toHourly(): HourlyForecast {
    val main = this.main
    val cond = weather?.firstOrNull()?.toDomain() ?: WeatherCondition(800, "Clear", "", "01d")
    return HourlyForecast(
        dt = dt ?: 0L,
        temperature = main?.temp ?: 0.0,
        feelsLike = main?.feelsLike ?: main?.temp ?: 0.0,
        condition = cond,
        pop = pop ?: 0.0,
        windSpeed = wind?.speed ?: 0.0,
        humidity = main?.humidity ?: 0,
        rainMm = rain?.threeHours
    )
}

fun CityDto.toCityLocation(): CityLocation = CityLocation(
    name = name ?: "",
    country = country ?: "",
    state = null,
    lat = coord?.lat ?: 0.0,
    lon = coord?.lon ?: 0.0
)

fun AirPollutionResponse.toDomain(): AirQuality? {
    val item = list?.firstOrNull() ?: return null
    return AirQuality(
        aqi = item.main?.aqi ?: return null,
        co = item.components?.co,
        no2 = item.components?.no2,
        o3 = item.components?.o3,
        so2 = item.components?.so2,
        pm25 = item.components?.pm25,
        pm10 = item.components?.pm10
    )
}
