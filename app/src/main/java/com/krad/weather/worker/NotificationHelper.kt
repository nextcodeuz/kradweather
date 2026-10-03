package com.krad.weather.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.krad.weather.MainActivity
import com.krad.weather.R
import com.krad.weather.domain.model.WeatherBundle
import com.krad.weather.util.WeatherFormatters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showDaily(bundle: WeatherBundle) {
        ensureChannel()
        val current = bundle.current
        val today = bundle.forecast.daily.firstOrNull()
        val text = buildString {
            append(WeatherFormatters.temp(current.temperature))
            append(" · ")
            append(current.condition.description.replaceFirstChar { it.uppercase() })
            if (today != null) {
                append(" · ▲${WeatherFormatters.temp(today.maxTemp)} ▼${WeatherFormatters.temp(today.minTemp)}")
            }
        }
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(context.getString(R.string.notif_daily_fmt, current.city.name))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        manager().notify(DAILY_ID, notif)
    }

    /** Aqlli tahlil natijasi: bitta yoki guruhlangan xabar. */
    fun showSmart(alerts: List<com.krad.weather.domain.model.SmartAlert>, city: String, urgent: Boolean) {
        if (alerts.isEmpty()) return
        ensureChannel()
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val top = alerts.first()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(
                if (urgent) android.R.drawable.ic_dialog_alert
                else android.R.drawable.ic_menu_compass
            )
            .setContentTitle("$city — ${top.title}")
            .setContentText(top.text)
            .setContentIntent(pending)
            .setAutoCancel(true)
        if (urgent) {
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setDefaults(android.app.Notification.DEFAULT_ALL)
        }
        if (alerts.size > 1) {
            val inbox = NotificationCompat.InboxStyle()
                .setBigContentTitle(context.getString(R.string.notif_digest_fmt, city))
            alerts.forEach { inbox.addLine("• ${it.text}") }
            builder.setStyle(inbox)
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(top.text))
        }
        manager().notify(if (urgent) SEVERE_ID else DAILY_ID, builder.build())
    }

    fun showSevereAlert(message: String, city: String) {
        ensureChannel()
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$city — ogohlantirish!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        manager().notify(SEVERE_ID, notif)
    }

    private fun manager(): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notif_channel_desc) }
        manager().createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "krad_weather"
        const val DAILY_ID = 1001
        const val SEVERE_ID = 1002
    }
}
