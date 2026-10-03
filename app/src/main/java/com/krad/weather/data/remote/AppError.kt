package com.krad.weather.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.krad.weather.domain.model.LangText
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

sealed class AppError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class CityNotFound(city: String, lang: String = "uz") :
        AppError(LangText.error(lang, "city", city))
    class InvalidApiKey(lang: String = "uz") :
        AppError(LangText.error(lang, "key"))
    class NoInternet(lang: String = "uz", cause: Throwable? = null) :
        AppError(LangText.error(lang, "offline"), cause)
    class Timeout(lang: String = "uz", cause: Throwable? = null) :
        AppError(LangText.error(lang, "timeout"), cause)
    class RateLimited(lang: String = "uz") :
        AppError(LangText.error(lang, "rate"))
    class Server(lang: String = "uz", cause: Throwable? = null) :
        AppError(LangText.error(lang, "server"), cause)
    class Unknown(message: String, cause: Throwable? = null) : AppError(message, cause)
}

private data class OwmErrorDto(
    @SerializedName("cod") val cod: String?,
    @SerializedName("message") val message: String?
)

fun Throwable.toAppError(query: String? = null, lang: String = "uz"): AppError {
    if (this is AppError) return this
    if (this is IOException && this !is SocketTimeoutException && cause == null) {
        val msg = message.orEmpty().lowercase()
        if ("unable to resolve host" in msg || "network" in msg || "failed to connect" in msg) {
            return AppError.NoInternet(lang, this)
        }
    }
    if (this is SocketTimeoutException) return AppError.Timeout(lang, this)
    if (this is HttpException) {
        return when (code()) {
            401 -> AppError.InvalidApiKey(lang)
            404 -> AppError.CityNotFound(query ?: "", lang)
            429 -> AppError.RateLimited(lang)
            in 500..599 -> AppError.Server(lang, this)
            else -> {
                val body = runCatching {
                    response()?.errorBody()?.string()?.let { Gson().fromJson(it, OwmErrorDto::class.java) }
                }.getOrNull()
                AppError.Unknown(
                    body?.message?.takeIf { it.isNotBlank() }
                        ?: LangText.error(lang, "unknown", "HTTP ${code()}"),
                    this
                )
            }
        }
    }
    return AppError.Unknown(message ?: LangText.error(lang, "unknown"), this)
}
