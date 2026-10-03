package com.krad.weather.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KradDatabase =
        Room.databaseBuilder(context, KradDatabase::class.java, "krad_weather.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFavoriteCityDao(db: KradDatabase): FavoriteCityDao = db.favoriteCityDao()

    @Provides
    fun provideCachedWeatherDao(db: KradDatabase): CachedWeatherDao = db.cachedWeatherDao()

    @Provides
    fun provideSearchHistoryDao(db: KradDatabase): SearchHistoryDao = db.searchHistoryDao()
}
