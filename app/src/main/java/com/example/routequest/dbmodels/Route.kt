package com.example.routequest.dbmodels

import com.google.firebase.firestore.GeoPoint

data class Route(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val geopoints: List<GeoPoint> = emptyList<GeoPoint>()

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        return geopoints == other.geopoints
    }

    override fun hashCode(): Int {
        return geopoints.hashCode()
    }
}
