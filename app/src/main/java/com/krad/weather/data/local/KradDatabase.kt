package com.krad.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteCity::class, CachedWeather::class, SearchHistoryItem::class],
    version = 3,
    exportSchema = false
)
abstract class KradDatabase : RoomDatabase() {
    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun cachedWeatherDao(): CachedWeatherDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
