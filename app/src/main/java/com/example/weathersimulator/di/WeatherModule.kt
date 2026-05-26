package com.example.weathersimulator.di

import com.example.weathersimulator.data.remote.weather.OpenMeteoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit
import com.example.weathersimulator.data.remote.city.CitySearchApi
import com.example.weathersimulator.data.remote.weather.OpenMeteoArchiveApi
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.example.weathersimulator.data.remote.weather.HourlyDto
import com.example.weathersimulator.data.remote.weather.DailyDto
import com.google.gson.JsonObject

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    private val weatherGson = GsonBuilder()
        .registerTypeAdapter(
            HourlyDto::class.java,
            JsonDeserializer<HourlyDto> { json, _, _ ->
                val obj = json.asJsonObject

                HourlyDto(
                    time = obj.stringList("time"),
                    temperature = obj.doubleList("temperature_2m"),
                    precipitation = obj.doubleList("precipitation"),
                    rain = obj.doubleList("rain"),
                    snowfall = obj.doubleList("snowfall"),
                    weatherCode = obj.intList("weather_code"),
                    cloudCover = obj.intList("cloud_cover"),
                    windSpeed = obj.doubleList("wind_speed_10m"),
                    windGusts = obj.doubleList("wind_gusts_10m"),
                    humidity = obj.intList("relative_humidity_2m"),
                    pressure = obj.doubleList("pressure_msl").ifEmpty {
                        obj.doubleList("surface_pressure")
                    },
                    isDay = obj.intList("is_day")
                )
            }
        )
        .registerTypeAdapter(
            DailyDto::class.java,
            JsonDeserializer<DailyDto> { json, _, _ ->
                val obj = json.asJsonObject

                DailyDto(
                    time = obj.stringList("time"),
                    weatherCode = obj.intList("weather_code"),
                    tempMax = obj.doubleList("temperature_2m_max"),
                    tempMin = obj.doubleList("temperature_2m_min"),
                    sunrise = obj.stringList("sunrise"),
                    sunset = obj.stringList("sunset")
                )
            }
        )
        .create()

    private fun JsonObject.stringList(name: String): List<String> {
        val array = getAsJsonArray(name) ?: return emptyList()
        return array.map { element ->
            if (element == null || element.isJsonNull) "" else element.asString
        }
    }

    private fun JsonObject.doubleList(name: String): List<Double> {
        val array = getAsJsonArray(name) ?: return emptyList()
        return array.map { element ->
            if (element == null || element.isJsonNull) 0.0 else element.asDouble
        }
    }

    private fun JsonObject.intList(name: String): List<Int> {
        val array = getAsJsonArray(name) ?: return emptyList()
        return array.map { element ->
            if (element == null || element.isJsonNull) 0 else element.asInt
        }
    }

    @Provides
    @Singleton
    fun provideOpenMeteoApi(): OpenMeteoApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(weatherGson))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOpenMeteoArchiveApi(): OpenMeteoArchiveApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://archive-api.open-meteo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(weatherGson))
            .build()
            .create(OpenMeteoArchiveApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCitySearchApi(): CitySearchApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .callTimeout(35, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CitySearchApi::class.java)
    }
}
