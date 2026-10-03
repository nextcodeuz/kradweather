package com.krad.weather.domain.model

import com.krad.weather.data.remote.dto.WapiCondition
import com.krad.weather.data.remote.dto.WapiForecastResponse
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WapiBundle(
    val bundle: WeatherBundle,
    val aqi: AirQuality?,
    val officialAlerts: List<SmartAlert>
)

/** WeatherAPI condition code → bizning ikonka (kunduz/tun). */
fun wapiIcon(code: Int?, isDay: Int?): String {
    val night = if (isDay == 0) "n" else "d"
    val day = when (code) {
        1000 -> "01"
        1003 -> "02"
        1006 -> "03"
        1009 -> "04"
        1030, 1135, 1147 -> "50"
        1063, 1150, 1153, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243, 1246 -> "10"
        1066, 1069, 1072, 1114, 1117, 1171, 1198, 1201, 1204, 1207, 1210, 1213,
        1216, 1219, 1222, 1225, 1237, 1249, 1252, 1255, 1258, 1261, 1264, 1266 -> "13"
        1087, 1273, 1276, 1279, 1282 -> "11"
        else -> "03"
    }
    return day + night
}

private fun WapiCondition.toDomainCondition(isDay: Int?): WeatherCondition = WeatherCondition(
    id = code ?: 800,
    main = text ?: "",
    description = text ?: "",
    icon = wapiIcon(code, isDay)
)

private fun pickTemp(c: Double?, f: Double?, units: String): Double {
    val base = when (units) {
        "imperial" -> f ?: c ?: 0.0
        else -> c ?: f ?: 0.0
    }
    return if (units == "standard") base + 273.15 else base
}

private fun parseAstroEpoch(date: String?, time: String?, zone: ZoneId): Long {
    return try {
        if (date == null || time == null) return 0L
        val d = LocalDate.parse(date)
        val t = LocalTime.parse(
            time.trim().uppercase(Locale.US),
            DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        )
        ZonedDateTime.of(d, t, zone).toEpochSecond()
    } catch (e: Exception) {
        0L
    }
}

fun WapiForecastResponse.toDomain(units: String): WapiBundle {
    val loc = location
    val cur = current ?: error("current missing")
    val zone = runCatching { ZoneId.of(loc?.tzId ?: "UTC") }.getOrDefault(ZoneId.of("UTC"))
    val nowEpoch = loc?.localtimeEpoch ?: (System.currentTimeMillis() / 1000)
    val tzOffset = zone.rules.getOffset(Instant.ofEpochSecond(nowEpoch)).totalSeconds

    val wcond = cur.condition
    val condition = WeatherCondition(
        id = wcond?.code ?: 1000,
        main = wcond?.text ?: "",
        description = wcond?.text ?: "",
        icon = wapiIcon(wcond?.code, cur.isDay)
    )
    val city = CityLocation(
        name = loc?.name ?: "",
        country = loc?.country ?: "",
        state = loc?.region,
        lat = loc?.lat ?: 0.0,
        lon = loc?.lon ?: 0.0
    )
    val current = CurrentWeather(
        city = city,
        condition = condition,
        temperature = pickTemp(cur.tempC, cur.tempF, units),
        feelsLike = pickTemp(cur.feelslikeC, cur.feelslikeF, units),
        tempMin = pickTemp(cur.tempC, cur.tempF, units),
        tempMax = pickTemp(cur.tempC, cur.tempF, units),
        pressure = (cur.pressureMb ?: 1013.0).toInt(),
        humidity = cur.humidity ?: 0,
        visibility = ((cur.visKm ?: 10.0) * 1000).toInt(),
        windSpeed = (cur.windKph ?: 0.0) / 3.6,
        windDeg = cur.windDegree ?: 0,
        windGust = cur.gustKph?.div(3.6),
        clouds = cur.cloud ?: 0,
        rainLastHour = cur.precipMm,
        snowLastHour = null,
        rainLast3h = cur.precipMm,
        snowLast3h = null,
        sunrise = 0L,
        sunset = 0L,
        timezoneOffsetSeconds = tzOffset,
        dt = nowEpoch,
        uvIndex = cur.uv
    )

    // Soatlik: barcha kunlardan kelajak soatlar, 24 ta (haqiqiy soatlik!)
    val allHours = forecast?.forecastday.orEmpty().flatMap { it.hour.orEmpty() }
    val hourly = allHours
        .filter { (it.timeEpoch ?: 0L) >= nowEpoch - 1800 }
        .take(24)
        .map { h ->
            val pop = maxOf(h.chanceOfRain ?: 0, h.chanceOfSnow ?: 0) / 100.0
            HourlyForecast(
                dt = h.timeEpoch ?: 0L,
                temperature = pickTemp(h.tempC, h.tempF, units),
                feelsLike = pickTemp(h.feelslikeC, h.feelslikeF, units),
                condition = h.condition.toDomainOrDefault(h.isDay),
                pop = pop,
                windSpeed = (h.windKph ?: 0.0) / 3.6,
                humidity = h.humidity ?: 0,
                rainMm = h.precipMm
            )
        }

    // Kunlik: 7 kun (WeatherAPI 14 kungacha beradi)
    val daily = forecast?.forecastday.orEmpty().take(7).map { fd ->
        val d = fd.day
        val dayHours = fd.hour.orEmpty()
        val maxWind = dayHours.mapNotNull { it.windKph }.maxOrNull() ?: 0.0
        val hum = dayHours.mapNotNull { it.humidity }.average()
            .let { if (it.isNaN()) (d?.avghumidity?.toInt() ?: 0) else it.toInt() }
        val pop = maxOf(d?.chanceOfRain ?: 0, d?.chanceOfSnow ?: 0) / 100.0
        val repr = d?.condition
        DailyForecast(
            // Shahar timezone bo'yicha kun (UTC bo'yicha emas!)
            dateEpochDay = Instant.ofEpochSecond(fd.dateEpoch ?: 0L)
                .atZone(zone).toLocalDate().toEpochDay(),
            minTemp = pickTemp(d?.mintempC, d?.mintempF, units),
            maxTemp = pickTemp(d?.maxtempC, d?.maxtempF, units),
            condition = repr.toDomainOrDefault(1),
            pop = pop,
            humidity = hum,
            windSpeed = maxWind / 3.6,
            rainMm = d?.totalprecipMm?.takeIf { it > 0 },
            sunrise = null,
            sunset = null
        )
    }

    val firstAstro = forecast?.forecastday?.firstOrNull()?.astro
    val firstDate = forecast?.forecastday?.firstOrNull()?.date
    val withSun = current.copy(
        sunrise = parseAstroEpoch(firstDate, firstAstro?.sunrise, zone),
        sunset = parseAstroEpoch(firstDate, firstAstro?.sunset, zone)
    )
    // Kunlik min/max ni joriy kunga ham qo'llaymiz (WAPI current'da min/max yo'q)
    val todayMinMax = daily.firstOrNull()
    val final = if (todayMinMax != null) {
        withSun.copy(tempMin = todayMinMax.minTemp, tempMax = todayMinMax.maxTemp)
    } else withSun

    val extras = toExtras()

    return WapiBundle(
        bundle = WeatherBundle(final, ForecastBundle(hourly, daily), extras.alerts),
        aqi = extras.aqi,
        officialAlerts = extras.alerts
    )
}

private fun WapiCondition?.toDomainOrDefault(isDay: Int?): WeatherCondition =
    this?.toDomainCondition(isDay) ?: WeatherCondition(800, "", "", wapiIcon(null, isDay))

/**
 * Faqat OWM'da yo'q narsalar: UV + ichki AQI + rasmiy ogohlantirishlar.
 * Yengil so'rov (days=1) uchun ham shu mapper ishlaydi.
 */
fun WapiForecastResponse.toExtras(): WapiExtras {
    val aq = current?.airQuality
    val aqi = if (aq == null) null else AirQuality(
        aqi = (aq.usEpaIndex ?: 1).coerceIn(1, 6).coerceAtMost(5),
        co = aq.co,
        no2 = aq.no2,
        o3 = aq.o3,
        so2 = aq.so2,
        pm25 = aq.pm25,
        pm10 = aq.pm10
    )
    val official = alerts?.alert.orEmpty().take(3).map { a ->
        val sev = when (a.severity?.lowercase(Locale.US)) {
            "extreme", "severe" -> 3
            "moderate" -> 2
            else -> 1
        }
        SmartAlert(
            flag = -1,
            title = (a.event ?: a.headline ?: "Ogohlantirish").take(120),
            text = (a.desc ?: a.instruction ?: a.headline ?: "").take(400),
            severity = sev
        )
    }
    return WapiExtras(uv = current?.uv, aqi = aqi, alerts = official)
}
