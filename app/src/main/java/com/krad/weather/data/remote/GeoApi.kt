package com.krad.weather.data.remote

import com.krad.weather.data.remote.dto.GeoCityDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApi {
    @GET("direct")
    suspend fun geocodeCity(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): List<GeoCityDto>

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 5
    ): List<GeoCityDto>
}
