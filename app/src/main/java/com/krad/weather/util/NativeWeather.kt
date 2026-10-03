package com.krad.weather.util

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.round

/**
 * C++ (NDK) hisoblashlar ko'prigi.
 * Kutubxona yuklanmasa yoki qurilma qo'llamasa — sof Kotlin fallback ishlaydi,
 * ilova hech qachon qotmaydi/o'chmaydi.
 */
object NativeWeather {

    // C++ dagi bayroqlar bilan BIR XIL qiymatlar
    const val FLAG_HEAT = 1
    const val FLAG_COLD = 2
    const val FLAG_RAIN = 4
    const val FLAG_SNOW = 8
    const val FLAG_STORM = 16
    const val FLAG_WIND = 32
    const val FLAG_FOG = 64
    const val FLAG_TEMP_DROP = 128
    const val FLAG_GOOD = 256

    val available: Boolean by lazy {
        runCatching {
            System.loadLibrary("kradnative")
            true
        }.getOrDefault(false)
    }

    private external fun analyzeWeatherNative(
        temp: Double, feelsLike: Double, windSpeed: Double,
        precipMm: Double, visibility: Int, maxPop: Double,
        conditionId: Int, tempChange6h: Double
    ): Int

    private external fun smoothTempsNative(temps: DoubleArray): DoubleArray

    private external fun heatIndexNative(tempC: Double, humidity: Double): Double

    private external fun dewPointNative(tempC: Double, humidity: Double): Double

    private external fun epaAqiNative(pm25: Double, pm10: Double): Int

    private external fun moonPhaseNative(epochMillis: Long): Double

    private external fun moonIlluminationNative(epochMillis: Long): Double

    private external fun scoreCityNative(query: String, name: String): Int

    fun analyzeWeather(
        temp: Double, feelsLike: Double, windSpeed: Double,
        precipMm: Double, visibility: Int, maxPop: Double,
        conditionId: Int, tempChange6h: Double
    ): Int {
        if (available) {
            return runCatching {
                analyzeWeatherNative(
                    temp, feelsLike, windSpeed, precipMm,
                    visibility, maxPop, conditionId, tempChange6h
                )
            }.getOrNull() ?: fallbackFlags(
                temp, feelsLike, windSpeed, precipMm,
                visibility, maxPop, conditionId, tempChange6h
            )
        }
        return fallbackFlags(
            temp, feelsLike, windSpeed, precipMm,
            visibility, maxPop, conditionId, tempChange6h
        )
    }

    fun smoothTemps(temps: List<Double>): List<Double> {
        if (temps.size < 2) return temps
        if (available) {
            runCatching { smoothTempsNative(temps.toDoubleArray()).toList() }
                .getOrNull()?.let { return it }
        }
        return temps.windowed(3, 1, partialWindows = true) { it.average() }
    }

    fun heatIndex(tempC: Double, humidity: Double): Double {
        if (available) {
            runCatching { heatIndexNative(tempC, humidity) }.getOrNull()?.let { return it }
        }
        if (tempC < 26.7) return tempC
        val tF = tempC * 9.0 / 5.0 + 32.0
        val hi = -42.379 + 2.04901523 * tF + 10.14333127 * humidity -
            0.22475541 * tF * humidity
        return (hi - 32.0) * 5.0 / 9.0
    }

    /** Shudring nuqtasi, °C (Magnus formulasi). */
    fun dewPoint(tempC: Double, humidity: Double): Double {
        if (available) {
            runCatching { dewPointNative(tempC, humidity) }.getOrNull()?.let { return it }
        }
        return fallbackDewPoint(tempC, humidity)
    }

    fun fallbackDewPoint(tempC: Double, humidity: Double): Double {
        val rh = humidity.coerceIn(1.0, 100.0)
        val a = 17.27
        val b = 237.7
        val alpha = (a * tempC) / (b + tempC) + ln(rh / 100.0)
        return (b * alpha) / (a - alpha)
    }

    /** US EPA AQI (PM2.5/PM10 dan kattasi), ma'lumot yo'q bo'lsa null. */
    fun epaAqi(pm25: Double?, pm10: Double?): Int? {
        if (pm25 == null && pm10 == null) return null
        if (available) {
            runCatching { epaAqiNative(pm25 ?: -1.0, pm10 ?: -1.0) }.getOrNull()
                ?.takeIf { it >= 0 }?.let { return it }
        }
        return fallbackEpaAqi(pm25, pm10)
    }

    fun fallbackEpaAqi(pm25: Double?, pm10: Double?): Int? {
        val c25 = doubleArrayOf(0.0, 12.1, 35.5, 55.5, 150.5, 250.5)
        val c25h = doubleArrayOf(12.0, 35.4, 55.4, 150.4, 250.4, 500.0)
        val c10 = doubleArrayOf(0.0, 55.0, 155.0, 255.0, 355.0, 425.0)
        val c10h = doubleArrayOf(54.0, 154.0, 254.0, 354.0, 424.0, 604.0)
        val ilo = doubleArrayOf(0.0, 51.0, 101.0, 151.0, 201.0, 301.0)
        val ihi = doubleArrayOf(50.0, 100.0, 150.0, 200.0, 300.0, 500.0)
        fun idx(conc: Double, hi: DoubleArray): Int {
            val i = hi.indexOfFirst { conc <= it }
            return if (i < 0) 5 else i
        }
        fun calc(conc: Double, lo: DoubleArray, hi: DoubleArray): Double {
            val i = idx(conc, hi)
            return (ihi[i] - ilo[i]) / (hi[i] - lo[i]) * (conc - lo[i]) + ilo[i]
        }
        val candidates = listOfNotNull(
            pm25?.let { calc(it, c25, c25h) },
            pm10?.let { calc(it, c10, c10h) }
        )
        if (candidates.isEmpty()) return null
        return round(candidates.max()).toInt().coerceAtMost(500)
    }

    /** Oy fazasi 0..1 (0=yangi, 0.5=to'lin). */
    fun moonPhase(epochMillis: Long = System.currentTimeMillis()): Double {
        if (available) {
            runCatching { moonPhaseNative(epochMillis) }.getOrNull()?.let { return it }
        }
        return fallbackMoonPhase(epochMillis)
    }

    fun fallbackMoonPhase(epochMillis: Long): Double {
        val days = (epochMillis - 947182440000.0) / 86400000.0
        var phase = (days / 29.53058867) % 1.0
        if (phase < 0) phase += 1.0
        return phase
    }

    /** Oy yoritilganligi 0..1. */
    fun moonIllumination(epochMillis: Long = System.currentTimeMillis()): Double {
        if (available) {
            runCatching { moonIlluminationNative(epochMillis) }.getOrNull()?.let { return it }
        }
        val p = fallbackMoonPhase(epochMillis)
        return (1 - cos(2 * Math.PI * p)) / 2
    }

    /** Shahar nomi moslik balli (0/60/70/80/100) — normalizatsiyalangan matnlar. */
    fun scoreCity(query: String, name: String): Int {
        if (available) {
            runCatching { scoreCityNative(query, name) }.getOrNull()?.let { return it }
        }
        return fallbackScore(query, name)
    }

    fun fallbackScore(query: String, name: String): Int = when {
        name.isEmpty() || query.isEmpty() -> 0
        name == query -> 100
        name.startsWith(query) -> 80
        name.split(' ', '-', '.').any { it.startsWith(query) } -> 70
        query in name -> 60
        else -> 0
    }

    /** C++ dagi mantiqning aynan o'zi — natijalar bir xil bo'ladi. */
    fun fallbackFlags(
        temp: Double, feelsLike: Double, windSpeed: Double,
        precipMm: Double, visibility: Int, maxPop: Double,
        conditionId: Int, tempChange6h: Double
    ): Int {
        var flags = 0
        if (temp >= 35.0 || feelsLike >= 38.0) flags = flags or FLAG_HEAT
        if (temp <= -10.0 || feelsLike <= -15.0) flags = flags or FLAG_COLD
        if (precipMm >= 0.5 || maxPop >= 0.7) flags = flags or FLAG_RAIN
        if (conditionId in 600..622) flags = flags or FLAG_SNOW
        if (conditionId in 200..232) flags = flags or FLAG_STORM
        if (windSpeed >= 12.0) flags = flags or FLAG_WIND
        if (visibility in 1..2000) flags = flags or FLAG_FOG
        if (tempChange6h <= -8.0) flags = flags or FLAG_TEMP_DROP
        if (flags == 0 && maxPop < 0.2 && windSpeed < 8.0 && temp in 15.0..30.0) {
            flags = flags or FLAG_GOOD
        }
        return flags
    }
}
