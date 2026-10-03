package com.krad.weather.widget

import com.krad.weather.data.repository.WeatherRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun weatherRepository(): WeatherRepository
    fun widgetUpdater(): KradWidgetUpdater
    fun widgetRefresh(): WidgetRefresh
}
