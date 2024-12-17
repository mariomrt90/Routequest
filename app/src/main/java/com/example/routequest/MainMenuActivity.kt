package com.example.routequest

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.dbmodels.Route
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore

class MainMenuActivity : ComponentActivity() {

    private val routesList: MutableState<List<Route>> =  mutableStateOf(emptyList())
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebase = Firebase.firestore

    init {
        getRoutes()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RoutequestTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background){
                    MainMenu(Modifier, routesList)

                }
            }
        }
    }

    private fun getRoutes() {

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this.baseContext,
                getString(R.string.mainmenu_auth_null_message), Toast.LENGTH_LONG).show()
            val intent = Intent(this.baseContext, MainActivity::class.java)
            this.baseContext.startActivity(intent)
        } else {

            user.run {

                firebase.collection("routes").get().addOnSuccessListener { documents ->

                    val routes = mutableListOf<Route>()

                    for (document in documents) {
                        val route = Route(
                            document.id,
                            document["name"].toString(),
                            document["description"].toString(),
                            document["geopoints"] as List<GeoPoint>
                        )

                        routes.add(route)
                    }
                    routesList.value = routes
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
                    } else Toast.makeText(baseContext,
                        getString(R.string.database_exception_unknown),Toast.LENGTH_LONG).show()
                }
            }

        }
    }


}

private fun convertGeoListToDoubleArr(geoPoints: List<GeoPoint>): DoubleArray {

    val geoPointD = DoubleArray(geoPoints.size*2)
    var i = 0
    for(geoPoint in geoPoints){
        geoPointD[i] = geoPoint.latitude
        i++
        geoPointD[i] = geoPoint.longitude
        i++
    }

    return geoPointD
}

@Composable
fun MainMenu(
    modifier: Modifier = Modifier,
    routesList: MutableState<List<Route>>
) {

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
                    .absolutePadding(0.dp, 30.dp, 0.dp, 50.dp)
                    .height(40.dp)
                    .width(40.dp)
            )

            Text(text = stringResource(R.string.mainmenu_title), modifier = modifier.padding(10.dp),
                fontSize = 32.sp, fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.mainmenu_welcome) +
                        FirebaseAuth.getInstance().currentUser?.email.toString()
                            .substringBeforeLast("@"), modifier = modifier
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight(0.8F)
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                    items(routesList.value) { route ->

                        Card(
                            shape = RoundedCornerShape(16),
                            border = BorderStroke(1.5.dp, colorResource(R.color.green)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                        Text(
                            text = route.name, modifier = Modifier.absolutePadding(15.dp,10.dp),
                            fontSize = 24.sp, fontWeight = FontWeight.SemiBold
                        )
                        Text(text = route.description, modifier = Modifier.padding(15.dp,5.dp))

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp, 5.dp)) {
                            Button(
                                onClick = {
                                    val intent =
                                        Intent(mContext, ClimatePreviewActivity::class.java)

                                    val geoPointsD = convertGeoListToDoubleArr(route.geopoints)

                                    intent.putExtra("routeID", route.id)
                                    intent.putExtra("Geopoints", geoPointsD)
                                    mContext.startActivity(intent)
                                }, shape = RoundedCornerShape(30),
                                colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue)),
                                modifier = Modifier.fillMaxWidth(0.5F)
                            ) {
                                Text(text = stringResource(R.string.main_menu_climate_text))
                            }

                            Button(
                                onClick = {

                                    val intent = Intent(mContext, MapActivity::class.java)
                                    val geoPointsD = convertGeoListToDoubleArr(route.geopoints)
                                    intent.putExtra("routeID", route.id)
                                    intent.putExtra("Geopoints", geoPointsD)
                                    mContext.startActivity(intent)
                                }, modifier = Modifier
                                    .absolutePadding(10.dp, 0.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(30),
                                colors = ButtonDefaults.buttonColors(colorResource(R.color.green))
                            )
                            { Text(text = stringResource(R.string.button_text_start)) }

                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.absolutePadding(0.dp,10.dp)){
                Button(onClick = {
                    val intent = Intent(mContext, ConsultRecordsActivity::class.java)
                    mContext.startActivity(intent)
                },
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.green)),
                    modifier = Modifier
                        .height(50.dp)
                        .width(300.dp)) {
                    Text(text = stringResource(R.string.main_menu_records_text),
                        fontSize = 24.sp, modifier = Modifier.padding(10.dp, 0.dp),
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }

                Button(onClick = {

                    val intent = Intent(mContext, MainActivity::class.java)
                    mContext.startActivity(intent)

                }, modifier = Modifier.absolutePadding(0.dp, 5.dp,0.dp,0.dp),
                    colors = ButtonDefaults.buttonColors(Color.Transparent)
                ) {
                    Text(text = stringResource(R.string.logout_text), color = Color.Gray,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}




