package com.krad.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

// ---------- WeatherAPI.com: forecast.json ----------

data class WapiForecastResponse(
    @SerializedName("location") val location: WapiLocation?,
    @SerializedName("current") val current: WapiCurrent?,
    @SerializedName("forecast") val forecast: WapiForecast?,
    @SerializedName("alerts") val alerts: WapiAlerts?
)

data class WapiLocation(
    @SerializedName("name") val name: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("tz_id") val tzId: String?,
    @SerializedName("localtime_epoch") val localtimeEpoch: Long?
)

data class WapiCondition(
    @SerializedName("text") val text: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("code") val code: Int?
)

data class WapiAirQuality(
    @SerializedName("co") val co: Double?,
    @SerializedName("no2") val no2: Double?,
    @SerializedName("o3") val o3: Double?,
    @SerializedName("so2") val so2: Double?,
    @SerializedName("pm2_5") val pm25: Double?,
    @SerializedName("pm10") val pm10: Double?,
    @SerializedName("us-epa-index") val usEpaIndex: Int?,
    @SerializedName("gb-defra-index") val gbDefraIndex: Int?
)

data class WapiCurrent(
    @SerializedName("last_updated_epoch") val lastUpdatedEpoch: Long?,
    @SerializedName("temp_c") val tempC: Double?,
    @SerializedName("temp_f") val tempF: Double?,
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("condition") val condition: WapiCondition?,
    @SerializedName("wind_mph") val windMph: Double?,
    @SerializedName("wind_kph") val windKph: Double?,
    @SerializedName("wind_degree") val windDegree: Int?,
    @SerializedName("wind_dir") val windDir: String?,
    @SerializedName("pressure_mb") val pressureMb: Double?,
    @SerializedName("precip_mm") val precipMm: Double?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("cloud") val cloud: Int?,
    @SerializedName("feelslike_c") val feelslikeC: Double?,
    @SerializedName("feelslike_f") val feelslikeF: Double?,
    @SerializedName("vis_km") val visKm: Double?,
    @SerializedName("uv") val uv: Double?,
    @SerializedName("gust_mph") val gustMph: Double?,
    @SerializedName("gust_kph") val gustKph: Double?,
    @SerializedName("air_quality") val airQuality: WapiAirQuality?
)

data class WapiForecast(
    @SerializedName("forecastday") val forecastday: List<WapiForecastDay>?
)

data class WapiForecastDay(
    @SerializedName("date") val date: String?,
    @SerializedName("date_epoch") val dateEpoch: Long?,
    @SerializedName("day") val day: WapiDay?,
    @SerializedName("astro") val astro: WapiAstro?,
    @SerializedName("hour") val hour: List<WapiHour>?
)

data class WapiDay(
    @SerializedName("maxtemp_c") val maxtempC: Double?,
    @SerializedName("maxtemp_f") val maxtempF: Double?,
    @SerializedName("mintemp_c") val mintempC: Double?,
    @SerializedName("mintemp_f") val mintempF: Double?,
    @SerializedName("avgtemp_c") val avgtempC: Double?,
    @SerializedName("avgtemp_f") val avgtempF: Double?,
    @SerializedName("totalprecip_mm") val totalprecipMm: Double?,
    @SerializedName("avghumidity") val avghumidity: Double?,
    @SerializedName("daily_chance_of_rain") val chanceOfRain: Int?,
    @SerializedName("daily_chance_of_snow") val chanceOfSnow: Int?,
    @SerializedName("condition") val condition: WapiCondition?,
    @SerializedName("uv") val uv: Double?
)

data class WapiAstro(
    @SerializedName("sunrise") val sunrise: String?,
    @SerializedName("sunset") val sunset: String?,
    @SerializedName("moonrise") val moonrise: String?,
    @SerializedName("moonset") val moonset: String?,
    @SerializedName("moon_phase") val moonPhase: String?,
    @SerializedName("moon_illumination") val moonIllumination: String?,
    @SerializedName("is_sun_up") val isSunUp: Int?
)

data class WapiHour(
    @SerializedName("time_epoch") val timeEpoch: Long?,
    @SerializedName("time") val time: String?,
    @SerializedName("temp_c") val tempC: Double?,
    @SerializedName("temp_f") val tempF: Double?,
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("condition") val condition: WapiCondition?,
    @SerializedName("wind_kph") val windKph: Double?,
    @SerializedName("wind_degree") val windDegree: Int?,
    @SerializedName("pressure_mb") val pressureMb: Double?,
    @SerializedName("precip_mm") val precipMm: Double?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("cloud") val cloud: Int?,
    @SerializedName("feelslike_c") val feelslikeC: Double?,
    @SerializedName("feelslike_f") val feelslikeF: Double?,
    @SerializedName("windchill_c") val windchillC: Double?,
    @SerializedName("heatindex_c") val heatindexC: Double?,
    @SerializedName("dewpoint_c") val dewpointC: Double?,
    @SerializedName("will_it_rain") val willItRain: Int?,
    @SerializedName("chance_of_rain") val chanceOfRain: Int?,
    @SerializedName("will_it_snow") val willItSnow: Int?,
    @SerializedName("chance_of_snow") val chanceOfSnow: Int?,
    @SerializedName("vis_km") val visKm: Double?,
    @SerializedName("gust_kph") val gustKph: Double?,
    @SerializedName("uv") val uv: Double?
)

data class WapiAlerts(
    @SerializedName("alert") val alert: List<WapiAlert>?
)

data class WapiAlert(
    @SerializedName("headline") val headline: String?,
    @SerializedName("severity") val severity: String?,
    @SerializedName("urgency") val urgency: String?,
    @SerializedName("event") val event: String?,
    @SerializedName("desc") val desc: String?,
    @SerializedName("instruction") val instruction: String?,
    @SerializedName("areas") val areas: String?
)

// ---------- WeatherAPI.com: search.json ----------

data class WapiSearchItem(
    @SerializedName("name") val name: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?
)
