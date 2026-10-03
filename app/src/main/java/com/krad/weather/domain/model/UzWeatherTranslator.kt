package com.krad.weather.domain.model

/**
 * OpenWeatherMap o'zbek tilini qo'llamaydi — "uz" tanlanganda tavsiflar
 * mijoz tomonda tarjima qilinadi. Rus/ingliz tillarida API ning o'zi beradi.
 */
object UzWeatherTranslator {

    private val byDescription = mapOf(
        "clear sky" to "Ochiq osmon",
        "few clouds" to "Biroz bulutli",
        "scattered clouds" to "Tarqoq bulutlar",
        "broken clouds" to "Bulutli",
        "overcast clouds" to "Qalin bulutlar",
        "shower rain" to "Jala",
        "light rain" to "Yengil yomg'ir",
        "moderate rain" to "O'rtacha yomg'ir",
        "heavy intensity rain" to "Kuchli yomg'ir",
        "very heavy rain" to "Juda kuchli yomg'ir",
        "extreme rain" to "Haddan tashqari yomg'ir",
        "freezing rain" to "Muzli yomg'ir",
        "light intensity shower rain" to "Yengil jala",
        "shower rain and drizzle" to "Jala va mayda yomg'ir",
        "heavy intensity shower rain" to "Kuchli jala",
        "ragged shower rain" to "Uzlukli jala",
        "light intensity drizzle" to "Yengil mayda yomg'ir",
        "drizzle" to "Mayda yomg'ir",
        "heavy intensity drizzle" to "Kuchli mayda yomg'ir",
        "drizzle rain" to "Mayda yomg'ir",
        "shower drizzle" to "Jalali mayda yomg'ir",
        "thunderstorm" to "Momaqaldiroq",
        "thunderstorm with light rain" to "Yengil yomg'irli momaqaldiroq",
        "thunderstorm with rain" to "Yomg'irli momaqaldiroq",
        "thunderstorm with heavy rain" to "Kuchli yomg'irli momaqaldiroq",
        "light thunderstorm" to "Yengil momaqaldiroq",
        "heavy thunderstorm" to "Kuchli momaqaldiroq",
        "ragged thunderstorm" to "Uzlukli momaqaldiroq",
        "light snow" to "Yengil qor",
        "snow" to "Qor",
        "heavy snow" to "Kuchli qor",
        "sleet" to "Yomg'ir aralash qor",
        "light shower sleet" to "Yengil yomg'irli qor",
        "shower sleet" to "Jalali qor",
        "light rain and snow" to "Yengil yomg'ir va qor",
        "rain and snow" to "Yomg'ir va qor",
        "light shower snow" to "Yengil jalali qor",
        "shower snow" to "Jalali qor",
        "heavy shower snow" to "Kuchli jalali qor",
        "mist" to "Tuman",
        "smoke" to "Tutun",
        "haze" to "G'ubor",
        "sand/dust whirls" to "Qum bo'roni",
        "fog" to "Qalin tuman",
        "sand" to "Qum",
        "dust" to "Chang",
        "volcanic ash" to "Vulqon kuli",
        "squalls" to "Kuchli shamol",
        "tornado" to "Tornado"
    )

    private val byMain = mapOf(
        "Thunderstorm" to "Momaqaldiroq",
        "Drizzle" to "Mayda yomg'ir",
        "Rain" to "Yomg'ir",
        "Snow" to "Qor",
        "Mist" to "Tuman",
        "Smoke" to "Tutun",
        "Haze" to "G'ubor",
        "Dust" to "Chang",
        "Fog" to "Tuman",
        "Sand" to "Qum",
        "Ash" to "Kul",
        "Squall" to "Kuchli shamol",
        "Tornado" to "Tornado",
        "Clear" to "Ochiq osmon",
        "Clouds" to "Bulutli"
    )

    fun translate(condition: WeatherCondition): WeatherCondition {
        val uz = byDescription[condition.description.lowercase()]
            ?: byMain[condition.main]
            ?: return condition
        return condition.copy(
            description = uz.replaceFirstChar { it.lowercase() }
        )
    }

    fun translateBundle(bundle: WeatherBundle): WeatherBundle {
        val current = bundle.current.copy(
            condition = translate(bundle.current.condition)
        )
        val forecast = bundle.forecast.copy(
            hourly = bundle.forecast.hourly.map { it.copy(condition = translate(it.condition)) },
            daily = bundle.forecast.daily.map { it.copy(condition = translate(it.condition)) }
        )
        return bundle.copy(current = current, forecast = forecast)
    }
}
