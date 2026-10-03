package com.krad.weather

import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.CurrentWeather
import com.krad.weather.domain.model.HourlyForecast
import com.krad.weather.domain.model.SmartAlertAnalyzer
import com.krad.weather.domain.model.WeatherCondition
import com.krad.weather.presentation.components.dressAdvice
import com.krad.weather.util.NativeWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartAlertTest {

    private fun current(
        temp: Double = 22.0,
        feels: Double = 22.0,
        wind: Double = 3.0,
        pop: Double = 0.0,
        id: Int = 800,
        visibility: Int = 10000
    ) = CurrentWeather(
        city = CityLocation("T", "UZ", null, 41.0, 69.0),
        condition = WeatherCondition(id, "Clear", "ochiq", "01d"),
        temperature = temp,
        feelsLike = feels,
        tempMin = temp - 2,
        tempMax = temp + 2,
        pressure = 1013,
        humidity = 50,
        visibility = visibility,
        windSpeed = wind,
        windDeg = 90,
        windGust = null,
        clouds = 10,
        rainLastHour = if (pop > 0) 1.0 else null,
        snowLastHour = null,
        rainLast3h = null,
        snowLast3h = null,
        sunrise = 0L,
        sunset = 0L,
        timezoneOffsetSeconds = 18000,
        dt = 0L
    )

    private fun hour(temp: Double, pop: Double = 0.0) = HourlyForecast(
        dt = 0L,
        temperature = temp,
        feelsLike = temp,
        condition = WeatherCondition(800, "Clear", "", "01d"),
        pop = pop,
        windSpeed = 3.0,
        humidity = 50
    )

    @Test
    fun heat_detected() {
        val alerts = SmartAlertAnalyzer.analyze(current(temp = 38.0), listOf(hour(38.0)))
        assertTrue(alerts.any { it.flag == NativeWeather.FLAG_HEAT && it.severity == 3 })
    }

    @Test
    fun rain_detectedFromPop() {
        val alerts = SmartAlertAnalyzer.analyze(
            current(pop = 0.0),
            listOf(hour(20.0, pop = 0.8))
        )
        assertTrue(alerts.any { it.flag == NativeWeather.FLAG_RAIN })
    }

    @Test
    fun goodWeather_detected() {
        val alerts = SmartAlertAnalyzer.analyze(current(), listOf(hour(22.0)))
        assertTrue(alerts.any { it.flag == NativeWeather.FLAG_GOOD })
    }

    @Test
    fun dressAdvice_coldRecommendsWarm() {
        val tip = dressAdvice(-3.0, 0.0, 3.0)
        assertTrue(tip.contains("Qishki"))
        assertTrue(dressAdvice(-20.0, 0.0, 3.0).contains("qalin"))
        assertTrue(dressAdvice(32.0, 0.0, 2.0).contains("suv"))
        assertTrue(dressAdvice(20.0, 0.8, 3.0).contains("Soyabon"))
    }

    @Test
    fun nativeFallback_matchesFlags() {
        // JVM'da .so yo'q — fallback ishlashi va izchil bo'lishi kerak
        val a = NativeWeather.analyzeWeather(38.0, 38.0, 3.0, 0.0, 10000, 0.0, 800, 0.0)
        val b = NativeWeather.fallbackFlags(38.0, 38.0, 3.0, 0.0, 10000, 0.0, 800, 0.0)
        assertEquals(b, a)
        assertTrue(a and NativeWeather.FLAG_HEAT != 0)
    }
}
