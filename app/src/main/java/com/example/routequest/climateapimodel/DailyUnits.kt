package com.example.routequest.climateapimodel

data class DailyUnits(
    val temperature_2m_max: String,
    val temperature_2m_min: String,
    val time: String,
    val weather_code: String,
    val windDirection_10m_dominant: String,
    val wind_speed_10m_max: String
)