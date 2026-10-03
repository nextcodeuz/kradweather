package com.krad.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeather(
    @PrimaryKey val cacheKey: String,
    val payloadJson: String,
    val updatedAt: Long
)
