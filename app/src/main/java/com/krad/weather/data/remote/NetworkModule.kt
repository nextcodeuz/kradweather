package com.krad.weather.data.remote

import android.content.Context
import com.krad.weather.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache =
        Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        apiKey: ApiKeyInterceptor,
        cache: Cache
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(apiKey)
            .addInterceptor(logging)
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    @Named("weather")
    fun provideWeatherRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("geo")
    fun provideGeoRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.GEO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("air")
    fun provideAirRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideKradWeatherApi(@Named("weather") retrofit: Retrofit): KradWeatherApi =
        retrofit.create(KradWeatherApi::class.java)

    /**
     * WeatherAPI.com uchun alohida klient — OpenWeather appid interceptori
     * bu yerga tegishli emas (kalit `key` parametri bilan uzatiladi).
     */
    @Provides
    @Singleton
    @Named("plain")
    fun providePlainClient(
        logging: HttpLoggingInterceptor,
        cache: Cache
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .cache(cache)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    @Named("wapi")
    fun provideWapiRetrofit(@Named("plain") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideWeatherApiCom(@Named("wapi") retrofit: Retrofit): WeatherApiCom =
        retrofit.create(WeatherApiCom::class.java)

    @Provides
    @Singleton
    fun provideGeoApi(@Named("geo") retrofit: Retrofit): GeoApi =
        retrofit.create(GeoApi::class.java)

    @Provides
    @Singleton
    fun provideAirPollutionApi(@Named("air") retrofit: Retrofit): AirPollutionApi =
        retrofit.create(AirPollutionApi::class.java)
}
