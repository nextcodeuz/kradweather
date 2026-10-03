package com.krad.weather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.krad.weather.MainActivity
import com.krad.weather.R
import com.krad.weather.domain.model.WeatherBundle
import com.krad.weather.util.WeatherFormatters
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KradWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun updateAll(bundle: WeatherBundle) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, KradWidgetProvider::class.java))
        if (ids.isEmpty()) return
        val views = RemoteViews(context.packageName, R.layout.widget_krad).apply {
            setTextViewText(R.id.widget_city, bundle.current.city.name)
            setTextViewText(R.id.widget_temp, WeatherFormatters.temp(bundle.current.temperature))
            setTextViewText(
                R.id.widget_desc,
                bundle.current.condition.description.replaceFirstChar { it.uppercase() }
            )
            val today = bundle.forecast.daily.firstOrNull()
            if (today != null) {
                setTextViewText(
                    R.id.widget_minmax,
                    "▲ ${WeatherFormatters.temp(today.maxTemp)}  ▼ ${WeatherFormatters.temp(today.minTemp)}"
                )
            }
            val zone = ZoneOffset.ofTotalSeconds(bundle.current.timezoneOffsetSeconds)
            val time = Instant.now().atZone(zone).format(
                DateTimeFormatter.ofPattern("HH:mm", Locale.US)
            )
            setTextViewText(R.id.widget_updated, context.getString(R.string.widget_updated_fmt, time))
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setOnClickPendingIntent(R.id.widget_root, pending)
        }
        ids.forEach { manager.updateAppWidget(it, views) }
    }
}
