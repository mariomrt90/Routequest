package com.example.routequest.dbmodels

import com.google.firebase.Timestamp


data class Record(
    val routeId: String = "",
    val userId: String = "",
    val compMoment: Timestamp = Timestamp.now(),
    val maxSpeed: Double = 0.0,
    val avrgSpeed: Double = 0.0,
    val time: String = "",
    val distance: Double = 0.0

)

