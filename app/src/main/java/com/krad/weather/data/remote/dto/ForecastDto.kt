package com.krad.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForecastDto(
    @SerializedName("cod") val cod: String?,
    @SerializedName("message") val message: Int?,
    @SerializedName("cnt") val cnt: Int?,
    @SerializedName("list") val list: List<ForecastItemDto>?,
    @SerializedName("city") val city: CityDto?
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long?,
    @SerializedName("main") val main: MainDto?,
    @SerializedName("weather") val weather: List<WeatherConditionDto>?,
    @SerializedName("clouds") val clouds: CloudsDto?,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("pop") val pop: Double?,
    @SerializedName("rain") val rain: RainDto?,
    @SerializedName("snow") val snow: SnowDto?,
    @SerializedName("sys") val sys: ForecastSysDto?,
    @SerializedName("dt_txt") val dtTxt: String?
)

data class ForecastSysDto(
    @SerializedName("pod") val pod: String?
)

data class CityDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("country") val country: String?,
    @SerializedName("population") val population: Int?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)
