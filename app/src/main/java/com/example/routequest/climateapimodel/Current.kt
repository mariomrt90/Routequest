package com.example.routequest.climateapimodel

data class Current(
    val interval: Int,
    val temperature_2m: Double,
    val time: String,
    val weather_code: Int,
    val wind_direction_10m: Int,
    val wind_speed_10m: Double
)