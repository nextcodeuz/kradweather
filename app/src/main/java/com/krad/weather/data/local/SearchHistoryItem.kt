package com.krad.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistoryItem(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val state: String?,
    val lat: Double,
    val lon: Double,
    val searchedAt: Long = System.currentTimeMillis()
)
