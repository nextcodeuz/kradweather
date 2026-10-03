package com.krad.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteCity>>

    @Query("SELECT * FROM favorite_cities WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): FavoriteCity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(city: FavoriteCity)

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteById(id: String)
}
