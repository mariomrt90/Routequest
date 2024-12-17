package com.example.routequest.apiinterface

import com.example.routequest.climateapimodel.ClimateData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiService {
    @GET("v1/forecast?")
    fun getClimateData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentData: String,
        @Query("daily") dailyData: String
    ): Call<ClimateData>?
}

/*
https://api.open-meteo.com/v1/forecast?
latitude=40.4165&longitude=-3.702
6&current=temperature_2m,weather_code,wind_speed_10m,wind_direction_10m
&daily=weather_code,temperature_2m_max,temperature_2m_min,wind_speed_10m_max,wind_direction_10m_dominant
&timezone=Europe%2FBerlin
 */
