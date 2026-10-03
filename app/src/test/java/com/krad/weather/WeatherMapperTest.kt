package com.krad.weather

import com.krad.weather.data.remote.dto.CityDto
import com.krad.weather.data.remote.dto.CloudsDto
import com.krad.weather.data.remote.dto.CoordDto
import com.krad.weather.data.remote.dto.ForecastDto
import com.krad.weather.data.remote.dto.ForecastItemDto
import com.krad.weather.data.remote.dto.MainDto
import com.krad.weather.data.remote.dto.WeatherConditionDto
import com.krad.weather.data.remote.dto.WindDto
import com.krad.weather.domain.model.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class WeatherMapperTest {

    private fun item(dt: Long, temp: Double, icon: String = "01d", pop: Double = 0.0) =
        ForecastItemDto(
            dt = dt,
            main = MainDto(temp, temp, temp - 1, temp + 1, 1013, 60, null, null),
            weather = listOf(WeatherConditionDto(800, "Clear", "clear", icon)),
            clouds = CloudsDto(10),
            wind = WindDto(3.0, 90, null),
            visibility = 10000,
            pop = pop,
            rain = null,
            snow = null,
            sys = null,
            dtTxt = null
        )

    @Test
    fun hourly_is24h_not3days() {
        // 16 ta 3-soatlik nuqta bersak ham hourly faqat 8 ta bo'lishi kerak
        val base = 1_700_000_000L
        val dto = ForecastDto(
            cod = "200", message = 0, cnt = 16,
            list = (0 until 16).map { item(base + it * 10800L, 20.0 + it) },
            city = CityDto(1, "Tashkent", CoordDto(69.0, 41.0), "UZ", 0, 18000, 0, 0)
        )
        val bundle = dto.toDomain()
        assertEquals(8, bundle.hourly.size)
    }

    @Test
    fun daily_groupsByCityTimezone_notUtc() {
        // Tashkent (UTC+5): 2024-01-01 00:30 (+5) = 2023-12-31 19:30 UTC
        // UTC bo'yicha guruhlansa boshqa kunga tushadi — to'g'risi Toshkent kuni bo'yicha
        val uzbekMidnight = Instant.parse("2023-12-31T19:30:00Z").epochSecond
        val dto = ForecastDto(
            cod = "200", message = 0, cnt = 2,
            list = listOf(
                item(uzbekMidnight, 5.0),
                item(uzbekMidnight + 10800L, 7.0) // +3 soat = 03:30 (+5), o'sha kun
            ),
            city = CityDto(1, "Tashkent", CoordDto(69.0, 41.0), "UZ", 0, 18000, 0, 0)
        )
        val bundle = dto.toDomain()
        assertEquals(1, bundle.daily.size)
        val expectedDay = Instant.ofEpochSecond(uzbekMidnight)
            .atZone(ZoneOffset.ofTotalSeconds(18000)).toLocalDate().toEpochDay()
        assertEquals(expectedDay, bundle.daily.first().dateEpochDay)
    }

    @Test
    fun daily_max5Days() {
        val base = 1_700_000_000L
        val dto = ForecastDto(
            cod = "200", message = 0, cnt = 40,
            list = (0 until 40).map { item(base + it * 10800L, 20.0) },
            city = CityDto(1, "T", CoordDto(0.0, 0.0), "UZ", 0, 0, 0, 0)
        )
        val bundle = dto.toDomain()
        // OWM zaxira yo'li — 5 kundan oshmaydi
        assertTrue(bundle.daily.size <= 5)
    }
}
