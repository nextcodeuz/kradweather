package com.krad.weather.data.remote

import com.krad.weather.data.remote.dto.AirPollutionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirPollutionApi {
    @GET("air_pollution")
    suspend fun currentAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): AirPollutionResponse
}
