package com.krad.weather.data.remote

import com.krad.weather.data.remote.dto.CurrentWeatherDto
import com.krad.weather.data.remote.dto.ForecastDto
import retrofit2.http.GET
import retrofit2.http.Query

interface KradWeatherApi {

    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): CurrentWeatherDto

    @GET("weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): CurrentWeatherDto

    @GET("forecast")
    suspend fun getForecastByCity(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): ForecastDto

    @GET("forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String,
        @Query("lang") lang: String
    ): ForecastDto
}
