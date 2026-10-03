package com.krad.weather.data.remote

import com.krad.weather.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url
        if (url.queryParameter("appid") != null) return chain.proceed(original)
        val patched = url.newBuilder()
            .addQueryParameter("appid", BuildConfig.OPEN_WEATHER_API_KEY)
            .build()
        return chain.proceed(original.newBuilder().url(patched).build())
    }
}
