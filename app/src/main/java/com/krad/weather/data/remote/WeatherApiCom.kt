package com.krad.weather.data.remote

import com.krad.weather.data.remote.dto.WapiForecastResponse
import com.krad.weather.data.remote.dto.WapiSearchItem
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * WeatherAPI.com — asosiy manba: bitta so'rovda joriy + 14 kunlik
 * soatlik prognoz + AQI + astronomiya + rasmiy ogohlantirishlar.
 */
interface WeatherApiCom {

    @GET("forecast.json")
    suspend fun forecast(
        @Query("key") key: String,
        @Query("q") query: String,
        @Query("days") days: Int = 8,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes",
        @Query("lang") lang: String = "en"
    ): WapiForecastResponse

    @GET("search.json")
    suspend fun search(
        @Query("key") key: String,
        @Query("q") query: String
    ): List<WapiSearchItem>
}
