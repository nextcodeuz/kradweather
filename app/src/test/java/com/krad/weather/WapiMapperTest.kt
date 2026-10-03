package com.krad.weather

import com.krad.weather.data.remote.dto.WapiAstro
import com.krad.weather.data.remote.dto.WapiCondition
import com.krad.weather.data.remote.dto.WapiCurrent
import com.krad.weather.data.remote.dto.WapiDay
import com.krad.weather.data.remote.dto.WapiForecast
import com.krad.weather.data.remote.dto.WapiForecastDay
import com.krad.weather.data.remote.dto.WapiForecastResponse
import com.krad.weather.data.remote.dto.WapiHour
import com.krad.weather.data.remote.dto.WapiLocation
import com.krad.weather.domain.model.toDomain
import com.krad.weather.domain.model.toExtras
import com.krad.weather.domain.model.wapiIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WapiMapperTest {

    private fun hour(epoch: Long, tempC: Double) = WapiHour(
        timeEpoch = epoch,
        time = null,
        tempC = tempC,
        tempF = tempC * 9 / 5 + 32,
        isDay = 1,
        condition = WapiCondition("Sunny", null, 1000),
        windKph = 10.0,
        windDegree = 90,
        pressureMb = 1013.0,
        precipMm = 0.0,
        humidity = 40,
        cloud = 0,
        feelslikeC = tempC,
        feelslikeF = tempC,
        windchillC = null,
        heatindexC = null,
        dewpointC = null,
        willItRain = 0,
        chanceOfRain = 0,
        willItSnow = 0,
        chanceOfSnow = 0,
        visKm = 10.0,
        gustKph = 12.0,
        uv = 5.0
    )

    private fun day(dateEpoch: Long) = WapiForecastDay(
        date = "2026-10-02",
        dateEpoch = dateEpoch,
        day = WapiDay(
            maxtempC = 25.0, maxtempF = 77.0,
            mintempC = 15.0, mintempF = 59.0,
            avgtempC = 20.0, avgtempF = 68.0,
            totalprecipMm = 0.0, avghumidity = 45.0,
            chanceOfRain = 10, chanceOfSnow = 0,
            condition = WapiCondition("Sunny", null, 1000),
            uv = 5.0
        ),
        astro = WapiAstro(
            sunrise = "06:30 AM", sunset = "06:30 PM",
            moonrise = null, moonset = null,
            moonPhase = null, moonIllumination = null, isSunUp = 1
        ),
        hour = (0 until 24).map { hour(dateEpoch + it * 3600L, 20.0 + it * 0.2) }
    )

    private fun response() = WapiForecastResponse(
        location = WapiLocation(
            "Tashkent", null, "Uzbekistan",
            41.3, 69.2, "Asia/Tashkent", 1_759_300_000L
        ),
        current = WapiCurrent(
            lastUpdatedEpoch = 1_759_300_000L,
            tempC = 22.0, tempF = 71.6, isDay = 1,
            condition = WapiCondition("Sunny", null, 1000),
            windMph = 5.0, windKph = 8.0, windDegree = 90, windDir = "E",
            pressureMb = 1015.0, precipMm = 0.0, humidity = 40, cloud = 0,
            feelslikeC = 22.0, feelslikeF = 71.6,
            visKm = 10.0, uv = 5.0,
            gustMph = 7.0, gustKph = 11.0, airQuality = null
        ),
        forecast = WapiForecast(
            forecastday = (0 until 8).map { day(1_759_300_000L + it * 86400L) }
        ),
        alerts = null
    )

    @Test
    fun iconMapping_dayNight() {
        assertEquals("01d", wapiIcon(1000, 1))
        assertEquals("01n", wapiIcon(1000, 0))
        assertEquals("10d", wapiIcon(1183, 1))
        assertEquals("13d", wapiIcon(1210, 1))
        assertEquals("11n", wapiIcon(1273, 0))
        assertEquals("50d", wapiIcon(1135, 1))
    }

    @Test
    fun fullMapping_unitsAndCounts() {
        val w = response().toDomain("metric")
        assertEquals("Tashkent", w.bundle.current.city.name)
        assertEquals(22.0, w.bundle.current.temperature, 0.001)
        // m/s ga o'tkazilgan
        assertEquals(8.0 / 3.6, w.bundle.current.windSpeed, 0.001)
        assertEquals(5.0, w.bundle.current.uvIndex ?: -1.0, 0.001)
        // Haqiqiy soatlik: 24 ta
        assertEquals(24, w.bundle.forecast.hourly.size)
        // Kunlik: 7 kun
        assertEquals(7, w.bundle.forecast.daily.size)
        assertNotNull(w.bundle.current.sunrise)
        assertTrue(w.bundle.current.sunset > w.bundle.current.sunrise)
    }

    @Test
    fun imperial_temps() {
        val w = response().toDomain("imperial")
        assertEquals(71.6, w.bundle.current.temperature, 0.01)
    }

    @Test
    fun extras_onlyMissingParts() {
        val ex = response().toExtras()
        assertEquals(5.0, ex.uv ?: -1.0, 0.001)
        // air_quality yo'q → null (OWM zaxirasi ishlatiladi)
        assertEquals(null, ex.aqi)
        assertTrue(ex.alerts.isEmpty())
    }
}
