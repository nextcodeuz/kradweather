package com.krad.weather.util

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import com.krad.weather.MainActivity
import java.util.Locale

/**
 * Ilova tilini BARCHA Android versiyalarida almashtirish.
 * AppCompatDelegate oddiy ComponentActivity + API<33 da ishlamaydi,
 * shuning uchun sinxron SharedPreferences + attachBaseContext o'rash ishlatiladi.
 */
object LocaleHelper {

    private const val PREFS = "krad_locale"
    private const val KEY_LANG = "lang"

    fun current(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANG, null) ?: "uz"

    /**
     * Tilni saqlab, joriy resurslarga qo'llaydi va bosh ekranni qayta
     * ishga tushiradi (yangi locale to'liq kuchga kirishi uchun).
     */
    fun setLangAndRestart(context: Context, lang: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANG, lang).apply()
        applyToResources(context, lang)
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
    }

    /** MainActivity.attachBaseContext ichida chaqiriladi. */
    fun wrap(base: Context): Context {
        val lang = current(base)
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }

    private fun applyToResources(context: Context, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
    }
}
