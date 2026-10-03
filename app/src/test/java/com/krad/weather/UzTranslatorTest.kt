package com.krad.weather

import com.krad.weather.domain.model.UzWeatherTranslator
import com.krad.weather.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class UzTranslatorTest {

    private fun cond(main: String, desc: String) =
        WeatherCondition(800, main, desc, "01d")

    @Test
    fun knownDescriptions_translated() {
        assertEquals(
            "ochiq osmon",
            UzWeatherTranslator.translate(cond("Clear", "clear sky")).description
        )
        assertEquals(
            "yengil yomg'ir",
            UzWeatherTranslator.translate(cond("Rain", "light rain")).description
        )
        assertEquals(
            "qor",
            UzWeatherTranslator.translate(cond("Snow", "snow")).description
        )
    }

    @Test
    fun unknownFallsBackToMain_thenOriginal() {
        assertEquals(
            "bulutli",
            UzWeatherTranslator.translate(cond("Clouds", "something strange")).description
        )
        assertEquals(
            "alien",
            UzWeatherTranslator.translate(cond("Alien", "alien")).description
        )
    }
}
