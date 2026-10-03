package com.krad.weather

import com.krad.weather.domain.model.CityLocation
import com.krad.weather.domain.model.SearchRanker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchRankerTest {

    private fun city(name: String, lat: Double = 41.0, lon: Double = 69.0) =
        CityLocation(name, "UZ", null, lat, lon)

    @Test
    fun exactMatch_first() {
        val list = listOf(
            city("Toshkent Viloyati", 41.0, 69.5),
            city("Toshkent", 41.3, 69.2),
            city("Tottenham", 51.5, -0.07)
        )
        val ranked = SearchRanker.rank("toshkent", list)
        assertEquals("Toshkent", ranked.first().name)
    }

    @Test
    fun dedupes_sameCity() {
        val list = listOf(
            city("Toshkent", 41.3111, 69.2797),
            city("Toshkent", 41.3112, 69.2798),
            city("Toshkent", 41.3111, 69.2797)
        )
        assertEquals(1, SearchRanker.rank("toshkent", list).size)
    }

    @Test
    fun apostropheVariants_match() {
        val list = listOf(city("O'zbekiston"))
        assertTrue(SearchRanker.rank("ozbekiston", list).isNotEmpty())
        assertTrue(SearchRanker.rank("o'zbekiston", list).isNotEmpty())
    }

    @Test
    fun blankNames_filtered() {
        val list = listOf(city("  "), city("Toshkent"))
        val ranked = SearchRanker.rank("tosh", list)
        assertEquals(1, ranked.size)
    }

    @Test
    fun shortQuery_empty() {
        assertTrue(SearchRanker.rank("t", listOf(city("Toshkent"))).isEmpty())
    }
}
