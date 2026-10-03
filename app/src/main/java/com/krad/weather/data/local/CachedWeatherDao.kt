package com.krad.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedWeatherDao {
    @Query("SELECT * FROM cached_weather WHERE cacheKey = :key LIMIT 1")
    suspend fun get(key: String): CachedWeather?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(cached: CachedWeather)

    @Query("DELETE FROM cached_weather WHERE updatedAt < :cutoff")
    suspend fun purgeOlderThan(cutoff: Long)
}
