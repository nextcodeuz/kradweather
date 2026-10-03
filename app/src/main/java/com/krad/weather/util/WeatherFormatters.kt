package com.krad.weather.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object WeatherFormatters {

    fun temp(value: Double, unit: String = "°C"): String = "${value.roundToInt()}$unit"

    fun unitLabel(units: String): String = when (units) {
        "imperial" -> "°F"
        "standard" -> "K"
        else -> "°C"
    }

    /** Sevimlilardagi eski birlikdagi haroratni joriy birlikka o'tkazadi. */
    fun convertTemp(value: Double, fromUnits: String, toUnits: String): Double {
        if (fromUnits == toUnits) return value
        val celsius = when (fromUnits) {
            "imperial" -> (value - 32.0) * 5.0 / 9.0
            "standard" -> value - 273.15
            else -> value
        }
        return when (toUnits) {
            "imperial" -> celsius * 9.0 / 5.0 + 32.0
            "standard" -> celsius + 273.15
            else -> celsius
        }
    }

    fun oneDecimal(value: Double): String = String.format(Locale.US, "%.1f", value)

    fun windDir(deg: Int): String {
        val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val idx = (((deg % 360) + 360) % 360 + 22) / 45 % 8
        return dirs[idx]
    }

    fun pressure(hPa: Int): String = "$hPa hPa"

    fun visibility(meters: Int): String = if (meters >= 1000) {
        String.format(Locale.US, "%.1f km", meters / 1000.0)
    } else "$meters m"

    fun timeOfDay(epochSeconds: Long, zoneOffsetSeconds: Int): String {
        val zone = ZoneOffset.ofTotalSeconds(zoneOffsetSeconds)
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        return Instant.ofEpochSecond(epochSeconds).atZone(zone).format(formatter)
    }

    fun dayShort(epochDay: Long, lang: String = "uz"): String {
        val dow = java.time.LocalDate.ofEpochDay(epochDay).dayOfWeek
        val table = when (lang) {
            "ru" -> listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            "en" -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            else -> listOf("Dush", "Sesh", "Chor", "Pay", "Jum", "Shan", "Yak")
        }
        // MONDAY=1 … SUNDAY=7 → index 0..6
        return table[(dow.value - 1).coerceIn(0, 6)]
    }

    fun iconUrl(icon: String): String =
        "https://openweathermap.org/img/wn/$icon@4x.png"

    fun backgroundForIcon(icon: String): BackgroundPalette = when {
        icon.endsWith("n") -> BackgroundPalette.NIGHT
        icon.startsWith("01") || icon.startsWith("02") -> BackgroundPalette.CLEAR
        icon.startsWith("09") || icon.startsWith("10") -> BackgroundPalette.RAIN
        icon.startsWith("11") -> BackgroundPalette.STORM
        icon.startsWith("13") -> BackgroundPalette.SNOW
        else -> BackgroundPalette.CLOUD
    }
}

enum class BackgroundPalette { CLEAR, CLOUD, RAIN, STORM, SNOW, NIGHT }
