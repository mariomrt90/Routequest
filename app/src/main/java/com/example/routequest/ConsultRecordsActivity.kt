package com.example.routequest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.dbmodels.Record
import com.example.routequest.dbmodels.Route
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize
import kotlin.math.round

class ConsultRecordsActivity : ComponentActivity() {

    private val db = Firebase.firestore
    private var recordsList: MutableState<MutableList<Record>> = mutableStateOf(mutableListOf())
    private var userRouteMap: MutableState<MutableMap<String, Route>> = mutableStateOf(mutableMapOf())
    private val auth = FirebaseAuth.getInstance()

    init {
        getRecords(auth)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = this.baseContext

        Firebase.initialize(ctx)

        setContent {
            RoutequestTheme {
                Surface (modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    DisplayUserRecords(
                        recordsList,
                        userRouteMap,
                        auth
                    )
                }
            }
        }
    }

    private fun getRecords(auth: FirebaseAuth) {

        val user = auth.currentUser

        val arrayList = mutableListOf<Record>()

        if (user == null) {
            Toast.makeText(this.baseContext,
                getString(R.string.mainmenu_auth_null_message), Toast.LENGTH_LONG).show()
            val intent = Intent(this.baseContext, MainActivity::class.java)
            this.baseContext.startActivity(intent)
        } else {

        user?.run {

            db.collection("records").whereEqualTo("userId","/users/"+user.uid).get().addOnSuccessListener { documents ->

                for (document in documents) {
                    val record = Record(routeId = document["routeId"].toString(), userId = document["userId"].toString(),
                        avrgSpeed = document["avrgSpeed"].toString().toDouble(),
                        compMoment = (document["compMoment"] as Timestamp),
                        maxSpeed = document["maxSpeed"].toString().toDouble(),
                        time = document["time"].toString(), distance = document["distance"].toString().toDouble())
                    arrayList.add(record)
                }

                recordsList.value = arrayList
                getUserRoutes(auth)

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

    }

    private fun getUserRoutes(auth: FirebaseAuth){

        val user = auth.currentUser

        val routesId = arrayListOf<String>()
        val mapRoute = mutableMapOf<String, Route>()
        for(record in recordsList.value) {
            routesId.add(record.routeId.substringAfterLast("/"))
        }

        user?.run {
            db.collection("routes").whereIn(FieldPath.documentId(), routesId).get().addOnSuccessListener {
                documents ->

                for (document in documents) {

                    val route = Route(id = document.id, name = document["name"].toString(), description = document["description"].toString(),
                        geopoints = document["geopoints"] as List<GeoPoint>)
                    Log.w("Route", "$route")
                    mapRoute.put(route.id, route)
                }
                userRouteMap.value = mapRoute
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
}

private fun formatTimestamp(timestamp: Timestamp): String {

    val date = timestamp.toDate()
    val string = date.toString()
    val resultArr = string.split(" ")
    val result = "Sesión del día \n ${resultArr[0]} ${resultArr[2]} " +
            "de ${resultArr[1]} ${resultArr[5]} a la hora ${resultArr[3]}"
    return result
}


private fun formatTimeString(time: String): String {

    return "Tiempo que has tardado: \n ${time}"
}

private fun formatDistance(string: String): String {

    if(string.isNotEmpty()) {
        val doubleStr = (string.toDouble())/1000
        val result = ((round(doubleStr*100))/100).toString() + " Km"
        return result
    } else return string

}

private fun formatSpeed(string: String): String {

    if(string.isNotEmpty()) {
        val doubleStr = (string.toDouble())*3.6
        val result = ((round(doubleStr*100))/100).toString() + " Km/h"
        return result
    } else return string
}

@Composable
fun DisplayUserRecords(recordsList: MutableState<MutableList<Record>>, userRouteMap: MutableState<MutableMap<String, Route>>,
                       auth: FirebaseAuth) {

    val mContext = LocalContext.current

    Box {

        Image(
            painter = painterResource(R.drawable.app_background),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                painter = painterResource(R.drawable.logo_no_background),
                contentDescription = stringResource(R.string.content_description_logo_nobackg),
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .absolutePadding(0.dp, 0.dp, 0.dp, 30.dp)
                    .height(40.dp)
                    .width(40.dp)
            )

            Text(text = "Rutas completadas de "+ (auth.currentUser?.email?.substringBefore("@") ?: "Anónimo"),
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, lineHeight = TextUnit(30F, TextUnitType.Sp),
                modifier = Modifier.absolutePadding(0.dp,15.dp,0.dp,10.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.8F)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {


                items(recordsList.value) { record ->

                    Card(
                        shape = RoundedCornerShape(16),
                        border = BorderStroke(1.5.dp, colorResource(R.color.green)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(modifier = Modifier.padding(25.dp, 15.dp)) {
                            Text(
                                text = formatTimestamp(record.compMoment),
                                color = colorResource(R.color.dark_blue),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp
                            )

                            Text(
                                text = "${userRouteMap.value.get(
                                            record.routeId.substringAfterLast("/"))?.name}",
                                modifier = Modifier.absolutePadding(0.dp,10.dp))
                        }

                        Row(
                            modifier = Modifier.absolutePadding(10.dp, 0.dp, 10.dp, 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(0.5F)) {
                                Text(text = formatTimeString(record.time))

                                Text(text = "Distancia recorrida: " + formatDistance("${record.distance}"))
                            }

                            Column {

                                Text(
                                    text = "Velocidad media: " + formatSpeed("${record.avrgSpeed}"),
                                    color = Color.Black
                                )

                                Text(text = "Velocidad máxima: " + formatSpeed("${record.maxSpeed}"))
                            }

                        }

                    }

                }
            }

            Button(onClick = {
                val intent = Intent(mContext, MainMenuActivity::class.java)
                mContext.startActivity(intent)
            }, modifier = Modifier
                .height(50.dp)
                .width(300.dp),
                shape = RoundedCornerShape(30),
                colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue))) {

                Text(text = stringResource(R.string.main_menu_button), fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
            }
        }
    }
}


