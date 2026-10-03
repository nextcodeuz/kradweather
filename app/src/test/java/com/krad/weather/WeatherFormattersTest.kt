package com.krad.weather

import com.krad.weather.util.BackgroundPalette
import com.krad.weather.util.WeatherFormatters
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherFormattersTest {

    @Test
    fun windDir_cardinalPoints() {
        assertEquals("N", WeatherFormatters.windDir(0))
        assertEquals("N", WeatherFormatters.windDir(360))
        assertEquals("E", WeatherFormatters.windDir(90))
        assertEquals("S", WeatherFormatters.windDir(180))
        assertEquals("W", WeatherFormatters.windDir(270))
        assertEquals("NE", WeatherFormatters.windDir(45))
    }

    @Test
    fun visibility_formats() {
        assertEquals("10.0 km", WeatherFormatters.visibility(10000))
        assertEquals("500 m", WeatherFormatters.visibility(500))
    }

    @Test
    fun iconUrl_format() {
        assertEquals(
            "https://openweathermap.org/img/wn/01d@4x.png",
            WeatherFormatters.iconUrl("01d")
        )
    }

    @Test
    fun background_nightForNightIcons() {
        assertEquals(BackgroundPalette.NIGHT, WeatherFormatters.backgroundForIcon("01n"))
        assertEquals(BackgroundPalette.CLEAR, WeatherFormatters.backgroundForIcon("01d"))
        assertEquals(BackgroundPalette.RAIN, WeatherFormatters.backgroundForIcon("10d"))
        assertEquals(BackgroundPalette.SNOW, WeatherFormatters.backgroundForIcon("13d"))
        assertEquals(BackgroundPalette.STORM, WeatherFormatters.backgroundForIcon("11d"))
    }
}
