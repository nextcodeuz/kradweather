package com.krad.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("list") val list: List<AirPollutionItemDto>?
)

data class AirPollutionItemDto(
    @SerializedName("main") val main: AirMainDto?,
    @SerializedName("components") val components: AirComponentsDto?,
    @SerializedName("dt") val dt: Long?
)

data class AirMainDto(
    @SerializedName("aqi") val aqi: Int?
)

data class AirComponentsDto(
    @SerializedName("co") val co: Double?,
    @SerializedName("no") val no: Double?,
    @SerializedName("no2") val no2: Double?,
    @SerializedName("o3") val o3: Double?,
    @SerializedName("so2") val so2: Double?,
    @SerializedName("pm2_5") val pm25: Double?,
    @SerializedName("pm10") val pm10: Double?,
    @SerializedName("nh3") val nh3: Double?
)
