package com.krad.weather.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.krad.weather.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KradWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val result = goAsync()
        // Har bir chaqiruvga alohida scope — instance almashsa ham xavfsiz
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val entry = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    WidgetEntryPoint::class.java
                )
                // 1) Keshdan bir zumda (miltilashsiz)
                val cached = runCatching {
                    entry.weatherRepository().cachedForLastLocation()
                }.getOrNull()
                if (cached != null) {
                    runCatching { entry.widgetUpdater().updateAll(cached.bundle) }
                } else {
                    showPlaceholder(context, appWidgetManager, appWidgetIds)
                }
                // 2) Tarmoqdan yangilash (tayyor bo'lganda o'zi chiziladi)
                runCatching { entry.widgetRefresh().invoke() }
            } finally {
                result.finish()
            }
        }
    }

    private fun showPlaceholder(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_krad).apply {
            setTextViewText(R.id.widget_city, context.getString(R.string.app_name))
            setTextViewText(R.id.widget_temp, "--°")
            setTextViewText(R.id.widget_desc, context.getString(R.string.widget_updating))
            setTextViewText(R.id.widget_minmax, "")
            setTextViewText(R.id.widget_updated, "")
        }
        appWidgetIds.forEach {
            runCatching { appWidgetManager.updateAppWidget(it, views) }
        }
    }
}
