package com.example.routequest.climateapimodel

data class Daily(
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val time: List<String>,
    val weather_code: List<Int>,
    val wind_direction_10m_dominant: List<Int>,
    val wind_speed_10m_max: List<Double>
)