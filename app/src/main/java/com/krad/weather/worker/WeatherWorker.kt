package com.krad.weather.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.krad.weather.data.repository.WeatherRepository
import com.krad.weather.domain.model.SmartAlertAnalyzer
import com.krad.weather.presentation.settings.UserPreferences
import com.krad.weather.widget.KradWidgetUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repo: WeatherRepository,
    private val prefs: UserPreferences,
    private val notifications: NotificationHelper,
    private val widgetUpdater: KradWidgetUpdater
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!prefs.notificationsEnabled.first() && !prefs.autoRefresh.first()) {
            return Result.success()
        }
        val city = prefs.lastCity.first() ?: return Result.success()
        val coords = prefs.lastCoords()
        val units = prefs.currentUnits()
        val lang = prefs.currentLang()
        val result = runCatching {
            if (coords != null) repo.weatherByCoords(coords.first, coords.second, units, lang)
            else repo.weatherByCity(city, units, lang)
        }.getOrElse { return Result.retry() }

        // Vidjetni yangilash
        runCatching { widgetUpdater.updateAll(result.bundle) }

        // Aqlli tahlil + bildirishnoma (faqat yoqilgan bo'lsa)
        if (prefs.notificationsEnabled.first()) {
            val notifLang = prefs.currentLang()
            // WeatherAPI'dan faqat yo'q narsalar (UV/AQI/alerts) — bitta yengil so'rov
            val extras = runCatching {
                repo.wapiExtras(
                    result.bundle.current.city.lat,
                    result.bundle.current.city.lon,
                    notifLang
                )
            }.getOrNull()
            val analyzed = runCatching {
                SmartAlertAnalyzer.analyze(
                    result.bundle.current,
                    result.bundle.forecast.hourly,
                    notifLang
                )
            }.getOrDefault(emptyList())
            // Rasmiy ogohlantirishlar eng yuqorida
            val alerts = ((extras?.alerts.orEmpty() + result.bundle.officialAlerts + analyzed)
                .distinctBy { it.title to it.text })
                .sortedByDescending { it.severity }
                .take(3)
            if (alerts.isNotEmpty()) {
                val severe = alerts.filter { it.severity >= 3 }
                val cityName = result.bundle.current.city.name
                when {
                    // Xavfli holat — vaqtdan qat'i nazar darhol
                    severe.isNotEmpty() -> runCatching {
                        notifications.showSmart(severe, cityName, urgent = true)
                    }
                    // Ertalab (6:00–9:00) — kunlik dayjest
                    isMorning(result.bundle.current.timezoneOffsetSeconds) -> runCatching {
                        notifications.showSmart(alerts, cityName, urgent = false)
                    }
                }
            }
        }
        return Result.success()
    }

    private fun isMorning(tzOffsetSeconds: Int): Boolean {
        val hour = Instant.now()
            .atZone(ZoneOffset.ofTotalSeconds(tzOffsetSeconds))
            .hour
        return hour in 6..9
    }

    companion object {
        const val WORK_NAME = "krad_weather_periodic"

        fun schedule(context: Context) {
            // Batareya tejash: faqat internet bor va batareya kritik emas payt
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<WeatherWorker>(3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .addTag(WORK_NAME)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
