package com.krad.weather

import com.krad.weather.util.NativeWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeEngineTest {

    @Test
    fun dewPoint_knownValue() {
        // 25°C, 50% → ~13.8°C (Magnus)
        val dp = NativeWeather.fallbackDewPoint(25.0, 50.0)
        assertTrue(dp > 13.0 && dp < 14.6)
        // Native (agar yuklansa) fallback bilan bir xil bo'lishi kerak
        assertEquals(dp, NativeWeather.dewPoint(25.0, 50.0), 0.001)
    }

    @Test
    fun epaAqi_breakpoints() {
        // PM2.5 = 10 → (50/12)*10 ≈ 42
        assertEquals(42, NativeWeather.fallbackEpaAqi(10.0, null))
        // PM2.5 = 40 → 101 + (150-101)/(55.4-35.5)*(40-35.5) ≈ 112
        assertEquals(112, NativeWeather.fallbackEpaAqi(40.0, null))
        // Ikkalasidan kattasi olinadi (PM10=200 → ~123)
        assertEquals(123, NativeWeather.fallbackEpaAqi(10.0, 200.0))
        // Native izchil
        assertEquals(
            NativeWeather.fallbackEpaAqi(40.0, 200.0),
            NativeWeather.epaAqi(40.0, 200.0)
        )
    }

    @Test
    fun scoreCity_ranking() {
        assertEquals(100, NativeWeather.fallbackScore("toshkent", "toshkent"))
        assertEquals(80, NativeWeather.fallbackScore("tosh", "toshkent"))
        assertEquals(60, NativeWeather.fallbackScore("shk", "toshkent"))
        assertEquals(0, NativeWeather.fallbackScore("zzz", "toshkent"))
        assertEquals(
            NativeWeather.fallbackScore("tosh", "toshkent"),
            NativeWeather.scoreCity("tosh", "toshkent")
        )
    }

    @Test
    fun moonPhase_newMoonIsZero() {
        // 2000-01-06 18:14 UTC — yangi oy
        assertEquals(0.0, NativeWeather.fallbackMoonPhase(947182440000L), 0.001)
        assertEquals(
            NativeWeather.fallbackMoonPhase(947182440000L),
            NativeWeather.moonPhase(947182440000L),
            0.000001
        )
    }

    @Test
    fun moonPhase_fullMoonIsHalf() {
        // ~14.77 kun keyin to'lin oy
        val full = 947182440000L + (14.77 * 86400000).toLong()
        val phase = NativeWeather.moonPhase(full)
        assertTrue(phase > 0.45 && phase < 0.55)
        val illum = NativeWeather.moonIllumination(full)
        assertTrue(illum > 0.95)
    }
}
