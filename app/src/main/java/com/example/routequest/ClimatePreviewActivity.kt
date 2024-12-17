package com.example.routequest

import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.routequest.apiinterface.ApiService
import com.example.routequest.climateapimodel.ClimateData
import com.example.routequest.ui.theme.RoutequestTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClimatePreviewActivity : ComponentActivity() {

    private lateinit var currentTemp: String
    private lateinit var currentWeather: String
    private lateinit var currentWindSp: String
    private lateinit var currentWindDir: String
    private lateinit var dailyWeather: String
    private lateinit var dailyMaxTemp: String
    private lateinit var dailyMinTemp : String
    private lateinit var dailyWindSp: String
    private lateinit var dailyWindDir : String

    private val data: MutableState<ClimateData?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routeID = intent.getStringExtra("routeID")
        val geoPointsD = intent.getDoubleArrayExtra("Geopoints")

        currentTemp = getString(R.string.api_request_current_temperature)
        currentWeather = getString(R.string.api_request_current_weather_code)
        currentWindSp = getString(R.string.api_request_current_wind_speed)
        currentWindDir = getString(R.string.api_request_current_wind_direction)
        dailyWeather = getString(R.string.api_request_daily_weather_code)
        dailyMaxTemp = getString(R.string.api_request_daily_max_temperature)
        dailyMinTemp = getString(R.string.api_request_daily_min_temperature)
        dailyWindSp = getString(R.string.api_request_daily_max_wind_speed)
        dailyWindDir = getString(R.string.api_request_daily_wind_direction_dominant)

        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.climate_api_base_url))
            .addConverterFactory(GsonConverterFactory.create()).build()

        lifecycleScope.launch {
            getClimate(retrofit, geoPointsD!!)
        }

        setContent {
            RoutequestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box {
                        RoutePreview(
                            geoPointsD, data, routeID)
                    }
                }
            }
        }

    }

    private fun setClimateData(nData: ClimateData){
        data.value = nData
    }

    private fun getClimate(retrofit: Retrofit, geoPoints: DoubleArray) {
        val apiService = retrofit.create(ApiService::class.java)
        val call = apiService.getClimateData(
            geoPoints[0],
            geoPoints[1],
            "$currentTemp,$currentWeather,$currentWindSp,$currentWindDir",
            "$dailyWeather,$dailyMaxTemp,$dailyMinTemp,$dailyWindSp,$dailyWindDir"
        )

        if (call == null) {

            Toast.makeText(this.baseContext,
                getString(R.string.climate_api_error), Toast.LENGTH_LONG).show()
        } else {

            call.enqueue(object : Callback<ClimateData?> {

                override fun onResponse(
                    p0: Call<ClimateData?>,
                    p1: Response<ClimateData?>
                ) {
                    if (p1.isSuccessful && p1.body() != null && p1.code() != 204) {


                        val result = p1.body()!!
                        setClimateData(result)

                    } else if(p1.code() == 204){
                        Toast.makeText(baseContext,
                            getString(R.string.api_no_data_recieved),Toast.LENGTH_LONG).show()

                    }
                    else {

                        when(p1.code()){

                            in 300..<400 -> Toast.makeText(baseContext,
                                getString(R.string.api_error_300),Toast.LENGTH_LONG).show()
                            in 400..<500 -> Toast.makeText(baseContext,
                                getString(R.string.api_error_400),Toast.LENGTH_LONG).show()
                            in 500..600 -> Toast.makeText(baseContext,
                                getString(R.string.api_error_500),Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(p0: Call<ClimateData?>, p1: Throwable) {

                    Toast.makeText(baseContext, getString(R.string.climate_api_error), Toast.LENGTH_LONG).show()
                }

            })

        }
    }

}

private fun getCardBackground(weatherCode:Int): Int {

    var result = 0
    when(weatherCode){
        0 -> result = R.drawable.soleado
        in 40..49 -> result = R.drawable.fog
        in 50..69 -> result = R.drawable.lluvia2
        in 70..79 -> result = R.drawable.nieve
        in 83..94 -> result = R.drawable.nieve
        in 80..82 -> result = R.drawable.lluvia2
    }

    if (result == 0) result = R.drawable.nublado

    return result
}

private fun getWeatherIcon(weatherCode: Int, windSpeed: Double): Int{

    var result = 0

    when(weatherCode){
        0 -> result = R.drawable.sunny
        in 1..2 -> result = R.drawable.sunny_cloudy
        in 50..69 -> result = R.drawable.rain_light
        in 70..79 -> result = R.drawable.snow
        in 80..82 -> result = R.drawable.rain_light
        in 83..94 -> result = R.drawable.snow
        in 95..99 -> result = R.drawable.storm
    }

    if (result == 0 && windSpeed > 20.0) result = R.drawable.wind
    else if (result == 0) result = R.drawable.cloudy

    return result
}

private fun getWeatherText(weatherCode: Int, windSpeed: Double, context: Context): String{

    var result = ""

    when(weatherCode){
        0 -> result = context.getString(R.string.climate_sunny)
        in 1..2 -> result = context.getString(R.string.climate_sun_cloud)
        in 50..69 -> result = context.getString(R.string.climate_rain)
        in 70..79 -> result = context.getString(R.string.climate_snow)
        in 80..82 -> result = context.getString(R.string.climate_rain)
        in 83..94 -> result = context.getString(R.string.climate_snow)
        in 95..99 -> result = context.getString(R.string.climate_storm)
    }

    if (result == "" && windSpeed > 20.0) result = context.getString(R.string.climate_wind)
    else if (result == "") result = context.getString(R.string.climate_cloud)

    return result
}

private fun formatDailyDate(date: String): String {

    if (date.length == 10) {
        val month = date.substring(5..6)
        val day = date.substring(8..9)
        return "$day.$month"
    } else return ""
}

@Composable
fun RoutePreview(
    geoPointsD: DoubleArray?,
    climateData: MutableState<ClimateData?>,
    routeID: String?
) {
    val indexCs = listOf(1,2,3,4,5,6)
    val mContext = LocalContext.current

    Box {

        Image( painter = painterResource(R.drawable.app_background),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            Icon(
                painter = painterResource(R.drawable.logo_no_background),
                contentDescription = stringResource(R.string.content_description_logo_nobackg),
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(24.dp)
                    .height(40.dp)
                    .width(40.dp)
            )

            Text(text = stringResource(R.string.climate_title),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .absolutePadding(0.dp, 40.dp, 0.dp, 20.dp),
                fontSize = 32.sp,
                fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold)

            LazyColumn(
                modifier = Modifier
                    .padding(0.dp, 16.dp)
                    .height(400.dp)
                    .width(350.dp)
                    .align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {

                    Card(elevation = CardDefaults.outlinedCardElevation(8.dp),
                        modifier = Modifier
                            .height(150.dp)
                            .width(350.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(16)) {

                        Box {

                            Image(
                                painter = painterResource(
                                    getCardBackground(
                                        climateData.value?.current?.weather_code
                                            ?: R.drawable.nublado)),
                                contentDescription = "",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Column(verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)) {

                                    Text(text = stringResource(R.string.climate_today_card_title),
                                        color = Color.White, fontSize = 24.sp,
                                        modifier = Modifier.absolutePadding(0.dp,10.dp,0.dp,20.dp),
                                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Left)
                                    Text(text = (climateData.value?.current?.temperature_2m ?: 0.0).toString() +
                                            climateData.value?.current_units?.temperature_2m,color = Color.White,
                                        fontSize = 36.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Column(verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .absolutePadding(30.dp, 5.dp, 10.dp, 0.dp)
                                        .width(100.dp)) {

                                    Icon(
                                        painter = painterResource(
                                            getWeatherIcon(
                                                climateData.value?.current?.weather_code ?: 3,
                                                climateData.value?.current?.wind_speed_10m ?: 0.0)),
                                        contentDescription = "",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .absolutePadding(0.dp, 0.dp, 0.dp, 10.dp)
                                            .height(40.dp)
                                            .width(40.dp))

                                    Text(text = getWeatherText(
                                        climateData.value?.current?.weather_code?: 3,
                                        climateData.value?.current?.wind_speed_10m ?: 0.0
                                    , mContext), modifier = Modifier,color = Color.White)

                                    Text(text = stringResource(R.string.climate_temp_max)+
                                            (climateData.value?.daily?.temperature_2m_max?.get(0) ?: 0.0) +
                                            climateData.value?.daily_units?.temperature_2m_max,color = Color.White)
                                    Text(stringResource(R.string.climate_temp_min_text)+
                                            (climateData.value?.daily?.temperature_2m_min?.get(0) ?: 0.0) +
                                            climateData.value?.daily_units?.temperature_2m_min,color = Color.White)
                                }

                                Column (verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp, 10.dp)){

                                    Text(text = mContext.getString(R.string.climate_wind)+": \n"+
                                            (climateData.value?.current?.wind_speed_10m ?:0.0)+
                                            (climateData.value?.current_units?.wind_speed_10m),
                                        color = Color.White)

                                    Box(contentAlignment = Alignment.Center){

                                        Image(
                                            painter = painterResource(R.drawable.brujula),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .height(70.dp)
                                                .width(70.dp))

                                        Icon(painter = painterResource(R.drawable.flecha),
                                            contentDescription = "",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .absolutePadding(0.dp, 0.dp, 0.dp, 10.dp)
                                                .height(20.dp)
                                                .width(20.dp)
                                                .rotate(
                                                    (climateData.value?.current?.wind_direction_10m
                                                        ?: 0).toFloat()
                                                ))
                                    }
                                }
                            }
                        }



                    }
                }

                items(indexCs) { indexC ->

                    Card(elevation = CardDefaults.outlinedCardElevation(0.dp),
                        modifier = Modifier
                            .height(120.dp)
                            .width(350.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(16),
                        border = BorderStroke(1.dp, colorResource(R.color.green)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {

                        Box(modifier = Modifier,
                            contentAlignment = Alignment.Center) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {

                                Text(text = formatDailyDate(climateData.value?.daily?.time?.get(indexC) ?: ""),
                                    modifier = Modifier.absolutePadding(10.dp,0.dp,0.dp,0.dp))

                                Column (verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .width(70.dp)){

                                    Icon(
                                        painter = painterResource(
                                            getWeatherIcon(
                                                climateData.value?.daily?.weather_code?.get(indexC) ?: 3,
                                                climateData.value?.daily?.wind_speed_10m_max?.get(indexC) ?: 0.0
                                            )
                                        ),
                                        contentDescription = "",
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .height(30.dp)
                                            .width(30.dp)
                                    )

                                    Text(text = getWeatherText(
                                        climateData.value?.daily?.weather_code?.get(indexC)?: 3,
                                        climateData.value?.daily?.wind_speed_10m_max?.get(indexC) ?: 0.0
                                    , mContext), modifier = Modifier)
                                }

                                Column (verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(10.dp)
                                        .height(100.dp)
                                        .width(100.dp)){

                                    Text(text = stringResource(R.string.climate_temp_max) +
                                                (climateData.value?.daily?.temperature_2m_max?.get(indexC) ?: 0.0) +
                                            climateData.value?.daily_units?.temperature_2m_max)
                                    Text(
                                        stringResource(R.string.climate_temp_min_text) +
                                            (climateData.value?.daily?.temperature_2m_min?.get(indexC) ?: 0.0) +
                                            climateData.value?.daily_units?.temperature_2m_min)
                                    Text(text = stringResource(R.string.climate_wind_speed_text) +"\n"+
                                            climateData.value?.daily?.wind_speed_10m_max?.get(indexC)+
                                            (climateData.value?.daily_units?.wind_speed_10m_max)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.padding(10.dp,0.dp)) {

                                    Text(text = stringResource(R.string.climate_wind_direction),
                                        fontSize = 12.sp)

                                    Box(contentAlignment = Alignment.Center){

                                        Image(
                                            painter = painterResource(R.drawable.brujula),
                                            contentDescription = "",
                                            modifier = Modifier
                                                .height(70.dp)
                                                .width(70.dp))

                                        Icon(painter = painterResource(R.drawable.flecha),
                                            contentDescription = "",
                                            tint = Color.Black,
                                            modifier = Modifier
                                                .absolutePadding(0.dp, 0.dp, 0.dp, 10.dp)
                                                .height(20.dp)
                                                .width(20.dp)
                                                .rotate(
                                                    (climateData.value?.daily?.wind_direction_10m_dominant?.get(
                                                        indexC
                                                    ) ?: 0).toFloat()
                                                ))
                                    }

                                }

                            }
                        }

                    }

                }

            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(25.dp,10.dp)
            ) {

                Button(onClick = {

                    val intent = Intent(mContext, MapActivity::class.java)
                    intent.putExtra("Geopoints", geoPointsD)
                    intent.putExtra("routeID",routeID)
                    mContext.startActivity(intent)

                }, modifier = Modifier
                    .padding(10.dp)
                    .height(50.dp)
                    .width(300.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.green)))
                { Text(text = stringResource(R.string.button_text_start), fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium) }

                Button(onClick = {
                    val intent = Intent(mContext, MainMenuActivity::class.java)
                    mContext.startActivity(intent)
                }, modifier = Modifier
                    .height(50.dp)
                    .width(300.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue))) {

                    Text(text = mContext.getString(R.string.return_button_text), fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }
            }
        }

    }

}

