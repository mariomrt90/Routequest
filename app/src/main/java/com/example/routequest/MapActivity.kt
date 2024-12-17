package com.example.routequest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.ui.theme.RoutequestTheme
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.routequest.dbmodels.Record
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.advancedpolyline.MonochromaticPaintList
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MapActivity : ComponentActivity() {

    private val mpView: MutableState<MapView?> = mutableStateOf(null)
    val maxDataMap = mutableMapOf<String, MutableState<String>>(
        "maxSpeed" to mutableStateOf("0.0"),
        "maxDistance" to mutableStateOf("0.0"),
        "routeDistance" to mutableStateOf("0.0")
    )

    val dataStringMap = mutableMapOf<String, MutableState<String>>(
        "speed" to mutableStateOf("0.0"),
        "distanceRemaining" to mutableStateOf("0.0"),
        "distanceTraveled" to mutableStateOf("0.0"))
    val trueTime: MutableState<Duration> = mutableStateOf(0.seconds)
    val speedValues: ArrayList<Double> = arrayListOf()
    private val trackTime: MutableState<Boolean> = mutableStateOf(true)
    private lateinit var routeID: String

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        routeID = intent.getStringExtra("routeID") ?:""
        val geoPointsD = intent.getDoubleArrayExtra("Geopoints")

        val geoPoints = convertGeopointsD(geoPointsD)

        launchTimer()

        setContent {
            RoutequestTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    MapRoute(
                        geoPoints,
                        mpView,
                        this
                    )
                }
            }
        }

        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))

    }

    override fun onStop() {
        super.onStop()
        trackTime.value = false
    }

    override fun onRestart() {
        super.onRestart()

        mpView.value?.overlays?.last()

        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && mpView.value !=null && mpView.value?.overlays?.last() is LocationOverlayCustom){

            addLocation(mpView.value!!, baseContext,dataStringMap, maxDataMap, speedValues)
        }

        trackTime.value = true
        launchTimer()
    }

    fun saveRecord(avrgSpeed: Double) {

        val user = auth.currentUser

        if (user == null){

            Toast.makeText(this.baseContext,
                getString(R.string.mainmenu_auth_null_message), Toast.LENGTH_LONG).show()
            val intent = Intent(this.baseContext, MainActivity::class.java)
            this.baseContext.startActivity(intent)
        } else {

            val record = Record("/routes/$routeID", "/users/${user.uid}", Timestamp.now(),
                (maxDataMap["maxSpeed"]?.value ?: "0.0").toDouble(), avrgSpeed, formatTime(trueTime.value),
                (maxDataMap["maxDistance"]?.value ?: "0.0").toDouble())

            user.run {

                db.collection("records").add(record).addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        Toast.makeText(baseContext,
                            getString(R.string.map_route_completed), Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(baseContext,
                            getString(R.string.map_route_finish_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { exception ->
                if(exception is FirebaseFirestoreException){
                    when(exception.code){
                        FirebaseFirestoreException.Code.UNKNOWN -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_unknown),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.DATA_LOSS -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_data_loss),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.OK -> {}
                        FirebaseFirestoreException.Code.CANCELLED -> {}
                        FirebaseFirestoreException.Code.INVALID_ARGUMENT -> {}
                        FirebaseFirestoreException.Code.ALREADY_EXISTS ->{}
                        FirebaseFirestoreException.Code.NOT_FOUND -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_not_found),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_failed_precondition),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.ABORTED -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_aborted),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.INTERNAL -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_internal),Toast.LENGTH_LONG).show()
                        }
                        FirebaseFirestoreException.Code.UNAVAILABLE -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_unavailable),Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(baseContext,
                                getString(R.string.database_exception_other_exceptions),Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        }

    }

    private fun launchTimer(){

        lifecycleScope.launch {

            while (trackTime.value) {

                delay(1000L)
                trueTime.value += 1.toDuration(DurationUnit.SECONDS)
            }
        }
    }

    private fun convertGeopointsD(geoPointsD: DoubleArray?): ArrayList<GeoPoint> {
        val geoPoints = ArrayList<GeoPoint>()

        if (geoPointsD != null) {
            val s = arrayOf(0.0,0.0)

            for (i in geoPointsD.indices){
                if (i % 2 == 0) s[0] = geoPointsD[i]
                else{
                    s[1] = geoPointsD[i]
                    geoPoints.add(GeoPoint(s[0],s[1]))
                }
            }
        }
        return geoPoints
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun checkLocationPermissions(permissions: MultiplePermissionsState): Boolean{

    val locationRevoked = arrayOf(false, false)

    for (permission in permissions.revokedPermissions){

        when(permission.permission){
            Manifest.permission.ACCESS_FINE_LOCATION -> {
                locationRevoked[0] = true
            }
            Manifest.permission.ACCESS_COARSE_LOCATION -> {
                locationRevoked[1] = true
            }
        }

    }

    return locationRevoked[0]
}


private fun openAppSettings(context: Context){

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null))
    context.startActivity(intent)
}


@Composable
private fun ShowLocationAlertDialog(context: Context){

    val dialogText = stringResource(R.string.location_alert_text)

    val openAlertDialog = remember { mutableStateOf(true) }

    when{
        openAlertDialog.value ->
            AlertDialog(
            onDismissRequest = {

                openAlertDialog.value = false

            },
            title = { Text(text = stringResource(R.string.map_permission_rejected), modifier = Modifier) },
            text = { Text(text = dialogText, modifier = Modifier) },
            confirmButton = {
                Button(onClick = {

                    openAlertDialog.value = false

                    openAppSettings(context)
                }, modifier = Modifier) { Text(text = stringResource(R.string.map_dialog_goto_settings)) }
            },
            dismissButton = {
                Button(onClick = {

                    openAlertDialog.value = false
                }) { Text(text = stringResource(R.string.button_text_cancel)) }
            }
        )
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun MapRoute(
    geoPoints: ArrayList<GeoPoint>,
    mpView: MutableState<MapView?>,
    mapActivity: MapActivity
) {

    val mContext = LocalContext.current

    val permissions = rememberMultiplePermissionsState(permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))

    if(checkLocationPermissions(permissions)){
        ShowLocationAlertDialog(mContext)
    }

    Box {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
                    setBuiltInZoomControls(true)
                    setMultiTouchControls(true)

                    addRTGestures(this)

                    addCompass(this, context)

                    addMarker(this, geoPoints[0], context,
                        context.getString(R.string.map_marker_start_title))
                    addMarker(this, geoPoints[geoPoints.size - 1], context,
                        context.getString(R.string.map_marker_final_title))

                    addPolyline(this, geoPoints, context)

                    focusRoute(this, geoPoints)

                    addLocation(this, context, mapActivity.dataStringMap, mapActivity.maxDataMap,
                        mapActivity.speedValues)


                }
            },
            update = { view ->
                mpView.value = view

            }

        )

        Row(horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.absolutePadding(130.dp, 20.dp)){

            Button(onClick = {

                var avrgSpeed = 0.0

                for (speed in mapActivity.speedValues) {
                    avrgSpeed += speed
                }
                avrgSpeed /= mapActivity.speedValues.size

                mapActivity.saveRecord(avrgSpeed)

                val intent = Intent(mContext, ConsultRecordsActivity::class.java)
                mContext.startActivity(intent)
            }, shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.green)),
                modifier = Modifier.padding(10.dp, 0.dp))
            { Text(text = stringResource(R.string.map_end_route_button), fontWeight = FontWeight.Medium) }

            Button(onClick = {

                val intent = Intent(mContext, PauseDialogActivity::class.java)
                mContext.startActivity(intent)
            }, shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue)))
            { Text(text = stringResource(R.string.map_button_pause), fontWeight = FontWeight.Medium) }
        }

        Box(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color.White, RoundedCornerShape(20.dp))) {

            Text(text = formatSpeed(mapActivity.dataStringMap["speed"]!!.value) + mContext.getString(R.string.map_speed_unit_kmh),
                modifier = Modifier.padding(20.dp, 20.dp), color = Color(ContextCompat.getColor(mContext, R.color.green)),
                fontSize = 30.sp, fontWeight = FontWeight.SemiBold
            )

            Text(text = formatTime(mapActivity.trueTime.value),
                modifier = Modifier.padding(20.dp, 60.dp), color = Color(ContextCompat.getColor(mContext, R.color.dark_blue)),
                fontSize = 24.sp, fontWeight = FontWeight.SemiBold
            )

            Slider(enabled = false, value = (mapActivity.dataStringMap["distanceTraveled"]?.value ?: "0.0").toDouble().toFloat(),
                onValueChange = {}, valueRange = 0F..(mapActivity.maxDataMap["routeDistance"]?.value?: "1.0").toDouble().toFloat(),
                colors = SliderDefaults.colors(disabledThumbColor = Color(ContextCompat.getColor(mContext, R.color.green)),
                        disabledActiveTrackColor = Color(ContextCompat.getColor(mContext, R.color.green)),
                        disabledInactiveTrackColor = Color.Gray), modifier = Modifier.absolutePadding(10.dp, 95.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.absolutePadding(20.dp, 130.dp)) {
                Text(text = formatDistance(mapActivity.dataStringMap["distanceTraveled"]!!.value) +
                        mContext.getString(R.string.map_distance_unit_km),
                    color = Color.Black)

                Text(text = formatDistance(mapActivity.dataStringMap["distanceRemaining"]!!.value) +
                        mContext.getString(R.string.map_distance_unit_km),
                    modifier = Modifier.absolutePadding(250.dp, 0.dp), color = Color.Black)
            }

            FilledIconButton(onClick = {

                val locOverlay = mpView.value?.overlays?.last() as MyLocationNewOverlay

                mpView.value?.controller?.setCenter(locOverlay.myLocation)
            }, modifier = Modifier.align(Alignment.TopEnd),
                colors = IconButtonDefaults.iconButtonColors(Color.White, Color.Black, Color.Gray, Color.DarkGray),
                shape = IconButtonDefaults.outlinedShape
            ) {
                Icon( painter = painterResource(R.drawable.my_location_24px),
                    contentDescription = stringResource(R.string.map_center_on_location_icon_description),
                    tint = Color(ContextCompat.getColor(mContext, R.color.green))
                )}

        }
    }

}

private fun formatTime(duration: Duration): String {
    return duration.toComponents{ hours, minutes, seconds, nanoseconds ->
        "$hours:$minutes:$seconds" }.toString()
}

private fun formatDistance(string: String): String {

    if(string.isNotEmpty()) {
        val doubleStr = (string.toDouble())/1000
        val result = ((round(doubleStr*100))/100).toString()
        return result
    } else return string

}

private fun formatSpeed(string: String): String {

    if(string.isNotEmpty()) {
        val doubleStr = (string.toDouble())*3.6
        val result = ((round(doubleStr*100))/100).toString()
        return result
    } else return string
}


private fun addLocation(mapView: MapView, context: Context, mapString: MutableMap<String,MutableState<String>>,
                        maxData: MutableMap<String,MutableState<String>>, speedValues: ArrayList<Double>){
    val locationOverlay = LocationOverlayCustom(GpsMyLocationProvider(context), mapView, mapString, maxData, speedValues)

    locationOverlay.enableMyLocation()
    val newIcon = getBitmapFromDrawable(context, R.drawable.baseline_navigation_24)
    locationOverlay.setDirectionIcon(newIcon)
    mapView.overlays.add(locationOverlay)

    mapView.invalidate()

}

private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, drawableId)
    val bitmap = Bitmap.createBitmap(
        drawable!!.intrinsicWidth,
        drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

private fun focusRoute(mapView: MapView, geoPoints: ArrayList<GeoPoint>){

    var maxLon = -180.0
    var minLon = 180.0
    var maxLat = -90.0
    var minLat = 90.0

    for (geoPoint in geoPoints) {

        if (geoPoint.longitude < minLon) minLon = geoPoint.longitude
        if (geoPoint.longitude > maxLon) maxLon = geoPoint.longitude

        if (geoPoint.latitude < minLat) minLat = geoPoint.latitude
        if (geoPoint.latitude > maxLat) maxLat = geoPoint.latitude
    }

    mapView.addOnFirstLayoutListener(MapView.OnFirstLayoutListener(
        function = fun(v: View, left: Int, top: Int, right: Int, bottom: Int){
            val boundingBox = BoundingBox(maxLat,maxLon,minLat,minLon)
            mapView.zoomToBoundingBox(boundingBox, true, 100)
            mapView.invalidate()
        }
    ))

}

private fun addMarker(mapView: MapView, geoPoint: GeoPoint, context: Context, title: String){

    val marker = Marker(mapView)
    marker.position = geoPoint
    marker.icon = ContextCompat.getDrawable(context, R.drawable.circulo)
    marker.title = title
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    mapView.overlays.add(marker)
    mapView.invalidate()
}

private fun addPolyline(mapView: MapView, geoPoints: ArrayList<GeoPoint>, context: Context){

    val paintInside = Paint()
    paintInside.strokeWidth = 15F
    paintInside.style = Paint.Style.FILL
    paintInside.color = ContextCompat.getColor(context, R.color.green)
    paintInside.strokeCap = Paint.Cap.ROUND

    val line = Polyline()
    line.setPoints(geoPoints)
    line.outlinePaintLists.add(MonochromaticPaintList(paintInside))

    mapView.overlays.add(line)
}

private fun addRTGestures(mapView: MapView) {

    val rotationGestureOverlay = RotationGestureOverlay(mapView)
    rotationGestureOverlay.isEnabled
    mapView.overlays.add(rotationGestureOverlay)
}

private fun addCompass(mapView: MapView, context: Context) {

    val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), mapView)
    compassOverlay.enableCompass()
    mapView.overlays.add(compassOverlay)
}
