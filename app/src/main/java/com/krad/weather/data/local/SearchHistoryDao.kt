package com.krad.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 10")
    fun observeRecent(): Flow<List<SearchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SearchHistoryItem)

    @Query("DELETE FROM search_history")
    suspend fun clear()

    @Query("DELETE FROM search_history WHERE searchedAt < :cutoff")
    suspend fun purgeOlderThan(cutoff: Long)
}
