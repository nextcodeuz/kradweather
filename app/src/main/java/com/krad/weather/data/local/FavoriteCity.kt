package com.krad.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_cities")
data class FavoriteCity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val addedAt: Long = System.currentTimeMillis(),
    val lastTemp: Double? = null,
    val lastIcon: String? = null,
    val lastUpdated: Long? = null,
    val tempUnits: String = "metric"
)
