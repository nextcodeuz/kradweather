package com.krad.weather.domain.model

import com.krad.weather.util.NativeWeather

/**
 * Shahar qidiruv natijalarini saralash algoritmi.
 * Muammolar fix'i: dublikatlar, tartibsiz ro'yxat, bo'sh nomlar,
 * o'zbekcha tutuq belgilari (o' / ' / ’) farqi.
 */
object SearchRanker {

    fun normalize(s: String): String =
        s.trim().lowercase()
            .replace("'", "")
            .replace("’", "")
            .replace("‘", "")
            .replace("`", "")
            .replace("ʼ", "")
            .replace("\\s+".toRegex(), " ")

    fun rank(query: String, cities: List<CityLocation>, limit: Int = 8): List<CityLocation> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()
        return cities
            .filter { it.name.isNotBlank() }
            .distinctBy {
                "${normalize(it.name)}|${it.country.lowercase()}|" +
                    "%.2f|%.2f".format(it.lat, it.lon)
            }
            // Ball C++ engine'da hisoblanadi (mumkin bo'lmasa Kotlin fallback)
            .map { city ->
                city to NativeWeather.scoreCity(q, normalize(city.name))
            }
            .filter { it.second > 0 }
            .sortedWith(
                compareByDescending<Pair<CityLocation, Int>> { it.second }
                    .thenBy { it.first.name }
            )
            .take(limit)
            .map { it.first }
    }

}
