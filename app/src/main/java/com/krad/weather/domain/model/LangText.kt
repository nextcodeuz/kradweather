package com.krad.weather.domain.model

import com.krad.weather.util.NativeWeather

/**
 * Dinamik xabarlar (alert, kiyim, xatolik) uchun 3 tilli matnlar.
 * Domain qatlamda Context yo'q — shuning uchun sof Kotlin jadvallar.
 * lang: "uz" | "ru" | "en" (boshqasi → "uz").
 */
object LangText {

    private fun l(lang: String): String = if (lang == "ru" || lang == "en") lang else "uz"

    // ---------- Smart alert sarlavhalar ----------

    fun alertTitle(lang: String, flag: Int): String {
        return when (l(lang)) {
            "ru" -> when (flag) {
                NativeWeather.FLAG_STORM -> "Опасность грозы ⛈"
                NativeWeather.FLAG_HEAT -> "Сильная жара ☀️"
                NativeWeather.FLAG_COLD -> "Сильный мороз ❄️"
                NativeWeather.FLAG_SNOW -> "Идёт снег 🌨"
                NativeWeather.FLAG_RAIN -> "Ожидается дождь 🌧"
                NativeWeather.FLAG_WIND -> "Сильный ветер 💨"
                NativeWeather.FLAG_FOG -> "Густой туман 🌫"
                NativeWeather.FLAG_TEMP_DROP -> "Резкое похолодание 📉"
                else -> "Отличная погода 🌤"
            }
            "en" -> when (flag) {
                NativeWeather.FLAG_STORM -> "Thunderstorm danger ⛈"
                NativeWeather.FLAG_HEAT -> "Extreme heat ☀️"
                NativeWeather.FLAG_COLD -> "Severe cold ❄️"
                NativeWeather.FLAG_SNOW -> "Snowing 🌨"
                NativeWeather.FLAG_RAIN -> "Rain expected 🌧"
                NativeWeather.FLAG_WIND -> "Strong wind 💨"
                NativeWeather.FLAG_FOG -> "Dense fog 🌫"
                NativeWeather.FLAG_TEMP_DROP -> "Sharp temperature drop 📉"
                else -> "Great weather 🌤"
            }
            else -> when (flag) {
                NativeWeather.FLAG_STORM -> "Momaqaldiroq xavfi ⛈"
                NativeWeather.FLAG_HEAT -> "Jazirama issiq ☀️"
                NativeWeather.FLAG_COLD -> "Qattiq sovuq ❄️"
                NativeWeather.FLAG_SNOW -> "Qor yog'moqda 🌨"
                NativeWeather.FLAG_RAIN -> "Yomg'ir kutilmoqda 🌧"
                NativeWeather.FLAG_WIND -> "Kuchli shamol 💨"
                NativeWeather.FLAG_FOG -> "Qalin tuman 🌫"
                NativeWeather.FLAG_TEMP_DROP -> "Keskin sovish 📉"
                else -> "Ajoyib ob-havo 🌤"
            }
        }
    }

    /**
     * @param temp hozirgi harorat matni (masalan "38°C"), [feels] his qilinishi,
     * [popPct] yog'ingarchilik foizi, [wind] shamol matni, [vis] ko'rinish matni,
     * [delta] harorat o'zgarishi matni (masalan "-10°C").
     */
    fun alertText(
        lang: String, flag: Int,
        temp: String, feels: String, popPct: Int,
        wind: String, windDir: String, vis: String, delta: String
    ): String {
        return when (l(lang)) {
            "ru" -> when (flag) {
                NativeWeather.FLAG_STORM -> "В ближайшие часы гроза с молнией. Выключите электроприборы, не выходите на улицу!"
                NativeWeather.FLAG_HEAT -> "Сейчас $temp — держитесь в тени, пейте больше воды, не оставляйте детей на солнце."
                NativeWeather.FLAG_COLD -> "Сейчас $temp, ощущается как $feels — одевайтесь тепло!"
                NativeWeather.FLAG_SNOW -> "Дороги скользкие — водителям осторожно, пешеходам нужна нескользящая обувь."
                NativeWeather.FLAG_RAIN -> "Вероятность осадков $popPct% — возьмите зонт или дождевик."
                NativeWeather.FLAG_WIND -> "Ветер $wind $windDir — не стойте под деревьями."
                NativeWeather.FLAG_FOG -> "Видимость $vis — включите фары, снизьте скорость."
                NativeWeather.FLAG_TEMP_DROP -> "За 6 часов похолодает на $delta — приготовьте тёплую одежду."
                else -> "$temp, ветер слабый — идеальный день для прогулки!"
            }
            "en" -> when (flag) {
                NativeWeather.FLAG_STORM -> "Thunderstorm with lightning in the coming hours. Turn off appliances, stay indoors!"
                NativeWeather.FLAG_HEAT -> "It's $temp now — stay in the shade, drink plenty of water, keep kids out of the sun."
                NativeWeather.FLAG_COLD -> "It's $temp, feels like $feels — dress warmly!"
                NativeWeather.FLAG_SNOW -> "Roads are slippery — drivers be careful, wear non-slip shoes."
                NativeWeather.FLAG_RAIN -> "Precipitation chance $popPct% — take an umbrella or raincoat."
                NativeWeather.FLAG_WIND -> "Wind $wind $windDir — avoid standing under trees."
                NativeWeather.FLAG_FOG -> "Visibility $vis — turn on headlights, slow down."
                NativeWeather.FLAG_TEMP_DROP -> "Cooling by $delta in 6 hours — prepare warm clothes."
                else -> "$temp, light wind — a perfect day for a walk!"
            }
            else -> when (flag) {
                NativeWeather.FLAG_STORM -> "Yaqin soatlarda chaqmoqli yomg'ir kutilmoqda. Elektr jihozlarini o'chiring, ko'chaga chiqmang!"
                NativeWeather.FLAG_HEAT -> "Havo $temp — soyada bo'ling, ko'p suv iching, bolalarni quyoshda qoldirmang."
                NativeWeather.FLAG_COLD -> "Havo $temp, his qilinishi $feels — qalin kiyining!"
                NativeWeather.FLAG_SNOW -> "Yo'llar sirpanchiq bo'ladi — haydovchilar ehtiyot bo'lsin, sirpanmaydigan oyoq kiyim kiying."
                NativeWeather.FLAG_RAIN -> "Yog'ingarchilik ehtimoli $popPct% — soyabon yoki yomg'irpo'sh oling."
                NativeWeather.FLAG_WIND -> "Shamol $wind $windDir — daraxtlar tagida turmang."
                NativeWeather.FLAG_FOG -> "Ko'rinish $vis — faralarni yoqing, tezlikni kamaytiring."
                NativeWeather.FLAG_TEMP_DROP -> "6 soatda $delta ga soviyapti — iliq kiyim tayyorlab qo'ying."
                else -> "$temp, shamol yengil — sayrga chiqish uchun ideal kun!"
            }
        }
    }

    // ---------- Kiyim maslahati ----------

    fun dress(lang: String, tempC: Double, pop: Double, windSpeed: Double): String {
        val base = when (l(lang)) {
            "ru" -> when {
                tempC >= 30 -> "👕 Носите футболку и лёгкую одежду. Не забудьте головной убор!"
                tempC >= 22 -> "👔 Лёгкой рубашки достаточно. На вечер возьмите тонкую верхнюю одежду."
                tempC >= 15 -> "🧥 Наденьте куртку или свитер."
                tempC >= 5 -> "🧣 Нужны тёплая куртка, шарф и перчатки."
                tempC >= -5 -> "🥶 Зимняя куртка, шапка и тёплая обувь!"
                else -> "🧊 Одевайтесь очень тепло — сильный мороз!"
            }
            "en" -> when {
                tempC >= 30 -> "👕 Wear a T-shirt and light clothes. Don't forget a hat!"
                tempC >= 22 -> "👔 A light shirt is enough. Take a thin layer for the evening."
                tempC >= 15 -> "🧥 Wear a jacket or sweater."
                tempC >= 5 -> "🧣 You need a warm jacket, scarf and gloves."
                tempC >= -5 -> "🥶 Winter jacket, hat and warm footwear!"
                else -> "🧊 Dress very warmly — severe frost!"
            }
            else -> when {
                tempC >= 30 -> "👕 Futbolka va yengil kiyim kiying. Bosh kiyim unutmang!"
                tempC >= 22 -> "👔 Yengil ko'ylak yetadi. Kechqurun uchun yupqa ustki oling."
                tempC >= 15 -> "🧥 Kurtka yoki sviter kiying."
                tempC >= 5 -> "🧣 Issiq kurtka, sharf va qo'lqop kerak."
                tempC >= -5 -> "🥶 Qishki kurtka, qalpoq va issiq oyoq kiyim!"
                else -> "🧊 Juda qalin kiyining — qattiq sovuq!"
            }
        }
        val extras = when (l(lang)) {
            "ru" -> buildList {
                if (pop >= 0.5) add("Возьмите зонт ☂️")
                if (windSpeed >= 10) add("Ветрозащитная верхняя одежда 🧥")
                if (tempC >= 28) add("Пейте больше воды 💧")
            }
            "en" -> buildList {
                if (pop >= 0.5) add("Take an umbrella ☂️")
                if (windSpeed >= 10) add("Windproof outer layer 🧥")
                if (tempC >= 28) add("Drink plenty of water 💧")
            }
            else -> buildList {
                if (pop >= 0.5) add("Soyabon oling ☂️")
                if (windSpeed >= 10) add("Shamolga chidamli ustki kiyim 🧥")
                if (tempC >= 28) add("Ko'p suv iching 💧")
            }
        }
        return if (extras.isEmpty()) base else "$base ${extras.joinToString(" ")}"
    }

    // ---------- AQI darajalari ----------

    fun aqiLabel(lang: String, aqi: Int): String {
        return when (l(lang)) {
            "ru" -> when (aqi) {
                1 -> "Хорошо"
                2 -> "Удовлетв."
                3 -> "Умеренно"
                4 -> "Плохо"
                5 -> "Очень плохо"
                else -> "Неизвестно"
            }
            "en" -> when (aqi) {
                1 -> "Good"
                2 -> "Fair"
                3 -> "Moderate"
                4 -> "Poor"
                5 -> "Very poor"
                else -> "Unknown"
            }
            else -> when (aqi) {
                1 -> "Yaxshi"
                2 -> "Qoniqarli"
                3 -> "O'rtacha"
                4 -> "Yomon"
                5 -> "Juda yomon"
                else -> "Noma'lum"
            }
        }
    }

    // ---------- UV darajalari ----------

    fun uvLabel(lang: String, uv: Double): String {
        val level = when {
            uv < 3 -> 0
            uv < 6 -> 1
            uv < 8 -> 2
            uv < 11 -> 3
            else -> 4
        }
        return when (l(lang)) {
            "ru" -> listOf("Низкий", "Умеренный", "Высокий", "Очень высокий", "Экстремальный")[level]
            "en" -> listOf("Low", "Moderate", "High", "Very high", "Extreme")[level]
            else -> listOf("Past", "O'rtacha", "Yuqori", "Juda yuqori", "Xavfli")[level]
        }
    }

    // ---------- Tarmoq xatoliklari ----------

    fun error(lang: String, key: String, arg: String = ""): String {
        return when (l(lang)) {
            "ru" -> when (key) {
                "city" -> "Город не найден: $arg"
                "key" -> "Неверный API-ключ. Проверьте ключ."
                "offline" -> "Нет соединения с интернетом"
                "timeout" -> "Время запроса вышло. Попробуйте ещё раз."
                "rate" -> "Превышен лимит запросов. Попробуйте позже."
                "server" -> "Ошибка сервера. Попробуйте позже."
                else -> if (arg.isNotBlank()) arg else "Произошла ошибка"
            }
            "en" -> when (key) {
                "city" -> "City not found: $arg"
                "key" -> "Invalid API key. Please check the key."
                "offline" -> "No internet connection"
                "timeout" -> "Request timed out. Please try again."
                "rate" -> "Request limit exceeded. Try again later."
                "server" -> "Server error. Try again later."
                else -> if (arg.isNotBlank()) arg else "Something went wrong"
            }
            else -> when (key) {
                "city" -> "Shahar topilmadi: $arg"
                "key" -> "API kalit xato. Iltimos, kalitni tekshiring."
                "offline" -> "Internet aloqasi yo'q"
                "timeout" -> "So'rov vaqti tugadi. Qayta urinib ko'ring."
                "rate" -> "So'rovlar limiti oshdi. Birozdan keyin urinib ko'ring."
                "server" -> "Server xatosi. Birozdan keyin urinib ko'ring."
                else -> if (arg.isNotBlank()) arg else "Xatolik yuz berdi"
            }
        }
    }
}
