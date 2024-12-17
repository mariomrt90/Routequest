package com.example.routequest

import android.location.Location
import androidx.compose.runtime.MutableState
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class LocationOverlayCustom(
    gpsMyLocationProvider: GpsMyLocationProvider,
    mapView: MapView,
    mapString: MutableMap<String, MutableState<String>>,
    maxData: MutableMap<String, MutableState<String>>,
    private var speedValues: ArrayList<Double>,
) : MyLocationNewOverlay(gpsMyLocationProvider, mapView) {

    private var dataStringMap: MutableMap<String,MutableState<String>> = mapString
    private val maxDataMap = maxData
    private val mpView = mapView
    private var remainingPoints = mutableListOf<GeoPoint>()
    private var needPolyline = true
    private var mpPolyline: Polyline? = null
    private var remainingDistance: Double = 0.0

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        super.onLocationChanged(location, source)

        getPolyline(needPolyline)

        if (location != null && remainingPoints.isNotEmpty()) {

            setValues(location.speed, GeoPoint(location.latitude,location.longitude))
        }
    }

    private fun getPolyline(start: Boolean) {

        if (start) {
            for(overlay in mpView.overlays){
                if (overlay is Polyline){
                    mpPolyline = overlay
                    break
                }
            }

            if (mpPolyline != null && remainingPoints.isEmpty()) {
                remainingPoints = mpPolyline!!.actualPoints.toList().toMutableList()
                remainingDistance = mpPolyline!!.distance
                maxDataMap["routeDistance"]!!.value = remainingDistance.toString()
                needPolyline = false
            }
        }

    }

    private fun getDistance(geoPoint: GeoPoint): Double {

        if (checkGeopointIsNear(geoPoint, remainingPoints[0]) && remainingPoints.size>1) {
            remainingPoints.removeAt(0)
            val remainingPolyline = Polyline()
            remainingPolyline.setPoints(remainingPoints)
            remainingDistance = remainingPolyline.distance

        }
        return remainingDistance
    }

    private fun checkGeopointIsNear(geoPoint1: GeoPoint, geoPoint2: GeoPoint): Boolean {

        return (geoPoint1.latitude in (geoPoint2.latitude-0.0007)..(geoPoint2.latitude+0.0007)
                && geoPoint1.longitude in (geoPoint2.longitude-0.0007)..(geoPoint2.longitude+0.0007))

    }

    private fun setValues(speed: Float, geoPoint: GeoPoint){

        if (speed.toString() != dataStringMap["speed"]!!.value){

            dataStringMap["speed"]!!.value = speed.toString()
        }

        dataStringMap["distanceRemaining"]!!.value = (getDistance(geoPoint) +
                geoPoint.distanceToAsDouble(remainingPoints[0])).toString()

        dataStringMap["distanceTraveled"]!!.value = ((mpPolyline?.distance ?: 0.0) -
                (getDistance(geoPoint) + geoPoint.distanceToAsDouble(remainingPoints[0]))).toString()
        checkMaxValues(speed, (dataStringMap["distanceTraveled"]!!.value).toDouble())

    }
    private fun checkMaxValues(speed: Float, distance: Double) {

        if (speed >(maxDataMap["maxSpeed"]!!.value).toFloat()){
            maxDataMap["maxSpeed"]!!.value = speed.toString()
        }

        if (distance > (maxDataMap["maxDistance"]!!.value).toDouble())
            maxDataMap["maxDistance"]!!.value = distance.toString()

        speedValues.add(speed.toDouble())
    }


}