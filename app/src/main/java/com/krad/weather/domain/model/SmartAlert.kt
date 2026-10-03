package com.krad.weather.domain.model

import com.krad.weather.util.NativeWeather
import com.krad.weather.util.WeatherFormatters

/**
 * Aqlli ogohlantirish: yaqin soatlardagi ob-havo tahlil qilinib,
 * har bir holatga mos matn tanlanadi.
 */
data class SmartAlert(
    val flag: Int,
    val title: String,
    val text: String,
    /** 1 = ma'lumot, 2 = ogohlantirish, 3 = xavfli */
    val severity: Int
)

object SmartAlertAnalyzer {

    fun analyze(
        current: CurrentWeather,
        hourly: List<HourlyForecast>,
        lang: String = "uz"
    ): List<SmartAlert> {
        val next = hourly.take(8)
        val maxPop = (next.maxOfOrNull { it.pop } ?: 0.0)
        val futureTemp = next.getOrNull(2)?.temperature ?: current.temperature
        val change6h = futureTemp - current.temperature
        val flags = NativeWeather.analyzeWeather(
            temp = current.temperature,
            feelsLike = current.feelsLike,
            windSpeed = current.windSpeed,
            precipMm = current.precipMm,
            visibility = current.visibility,
            maxPop = maxPop,
            conditionId = current.condition.id,
            tempChange6h = change6h
        )
        val temp = WeatherFormatters.temp(current.temperature)
        val feels = WeatherFormatters.temp(current.feelsLike)
        val wind = WeatherFormatters.oneDecimal(current.windSpeed) + " m/s"
        val params = { flag: Int ->
            LangText.alertText(
                lang = lang,
                flag = flag,
                temp = temp,
                feels = feels,
                popPct = (maxPop * 100).toInt(),
                wind = wind,
                windDir = WeatherFormatters.windDir(current.windDeg),
                vis = WeatherFormatters.visibility(current.visibility),
                delta = WeatherFormatters.temp(change6h)
            )
        }
        val out = mutableListOf<SmartAlert>()
        if (flags has NativeWeather.FLAG_STORM) out += SmartAlert(
            NativeWeather.FLAG_STORM, LangText.alertTitle(lang, NativeWeather.FLAG_STORM),
            params(NativeWeather.FLAG_STORM), 3
        )
        if (flags has NativeWeather.FLAG_HEAT) out += SmartAlert(
            NativeWeather.FLAG_HEAT, LangText.alertTitle(lang, NativeWeather.FLAG_HEAT),
            params(NativeWeather.FLAG_HEAT), 3
        )
        if (flags has NativeWeather.FLAG_COLD) out += SmartAlert(
            NativeWeather.FLAG_COLD, LangText.alertTitle(lang, NativeWeather.FLAG_COLD),
            params(NativeWeather.FLAG_COLD), 3
        )
        if (flags has NativeWeather.FLAG_SNOW) out += SmartAlert(
            NativeWeather.FLAG_SNOW, LangText.alertTitle(lang, NativeWeather.FLAG_SNOW),
            params(NativeWeather.FLAG_SNOW), 2
        )
        if (flags has NativeWeather.FLAG_RAIN) out += SmartAlert(
            NativeWeather.FLAG_RAIN, LangText.alertTitle(lang, NativeWeather.FLAG_RAIN),
            params(NativeWeather.FLAG_RAIN), 2
        )
        if (flags has NativeWeather.FLAG_WIND) out += SmartAlert(
            NativeWeather.FLAG_WIND, LangText.alertTitle(lang, NativeWeather.FLAG_WIND),
            params(NativeWeather.FLAG_WIND), 2
        )
        if (flags has NativeWeather.FLAG_FOG) out += SmartAlert(
            NativeWeather.FLAG_FOG, LangText.alertTitle(lang, NativeWeather.FLAG_FOG),
            params(NativeWeather.FLAG_FOG), 2
        )
        if (flags has NativeWeather.FLAG_TEMP_DROP) out += SmartAlert(
            NativeWeather.FLAG_TEMP_DROP, LangText.alertTitle(lang, NativeWeather.FLAG_TEMP_DROP),
            params(NativeWeather.FLAG_TEMP_DROP), 2
        )
        if (flags has NativeWeather.FLAG_GOOD) out += SmartAlert(
            NativeWeather.FLAG_GOOD, LangText.alertTitle(lang, NativeWeather.FLAG_GOOD),
            params(NativeWeather.FLAG_GOOD), 1
        )
        return out.sortedByDescending { it.severity }.take(3)
    }

    private infix fun Int.has(flag: Int): Boolean = (this and flag) != 0
}
