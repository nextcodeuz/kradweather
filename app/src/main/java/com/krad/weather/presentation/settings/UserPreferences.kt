package com.krad.weather.presentation.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "krad_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val UNITS = stringPreferencesKey("units")
        val LANG = stringPreferencesKey("lang")
        val DYNAMIC_BG = booleanPreferencesKey("dynamic_bg")
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val LAST_CITY = stringPreferencesKey("last_city")
        val LAST_LAT = stringPreferencesKey("last_lat")
        val LAST_LON = stringPreferencesKey("last_lon")
        val THEME = stringPreferencesKey("theme")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val units: Flow<String> = context.dataStore.data.map { it[Keys.UNITS] ?: "metric" }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANG] ?: "uz" }
    val dynamicBackground: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_BG] ?: true }
    val autoRefresh: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_REFRESH] ?: true }
    val lastCity: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_CITY] }
    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS] ?: false }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun currentUnits(): String = units.first()
    suspend fun currentLang(): String = language.first()

    suspend fun lastCoords(): Pair<Double, Double>? {
        val prefs = context.dataStore.data.first()
        val lat = prefs[Keys.LAST_LAT]?.toDoubleOrNull()
        val lon = prefs[Keys.LAST_LON]?.toDoubleOrNull()
        return if (lat != null && lon != null) lat to lon else null
    }

    suspend fun setUnits(value: String) { context.dataStore.edit { it[Keys.UNITS] = value } }
    suspend fun setLanguage(value: String) { context.dataStore.edit { it[Keys.LANG] = value } }
    suspend fun setDynamicBackground(value: Boolean) { context.dataStore.edit { it[Keys.DYNAMIC_BG] = value } }
    suspend fun setAutoRefresh(value: Boolean) { context.dataStore.edit { it[Keys.AUTO_REFRESH] = value } }
    suspend fun setTheme(value: String) { context.dataStore.edit { it[Keys.THEME] = value } }
    suspend fun setNotificationsEnabled(value: Boolean) { context.dataStore.edit { it[Keys.NOTIFICATIONS] = value } }
    suspend fun setOnboardingDone(value: Boolean) { context.dataStore.edit { it[Keys.ONBOARDING_DONE] = value } }
    suspend fun setLastCity(value: String?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(Keys.LAST_CITY) else prefs[Keys.LAST_CITY] = value
        }
    }
    suspend fun setLastCoords(lat: Double, lon: Double) {
        context.dataStore.edit {
            it[Keys.LAST_LAT] = lat.toString()
            it[Keys.LAST_LON] = lon.toString()
        }
    }
}
