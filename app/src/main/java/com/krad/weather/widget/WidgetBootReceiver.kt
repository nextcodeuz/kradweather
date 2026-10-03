package com.krad.weather.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.krad.weather.worker.WeatherWorker
import dagger.hilt.android.EntryPointAccessors

/**
 * Telefon yoqilganda / ilova yangilanganda vidjetni jonlantiradi.
 * Tizim WorkManager'ni kechiktirsa ham vidjet yangilanadi.
 */
class WidgetBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return
        // Worker jadvalini tiklash
        runCatching { WeatherWorker.schedule(context) }
        // Vidjetni darhol yangilash
        runCatching {
            val entry = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            entry.widgetRefresh().invoke()
        }
    }
}
