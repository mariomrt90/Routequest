package com.example.routequest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        baseContext.resources

        setContent {

            RoutequestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthCompose(auth, this)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        auth.signOut()
    }

}

private fun loginUser(email: String, password: String, activity: MainActivity, auth: FirebaseAuth, context: Context) {

        if(email.isNotBlank() && password.isNotBlank() && email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity.baseContext,
                            auth.currentUser?.email.toString().substringBefore("@")
                                    + activity.baseContext.getString(R.string.correct_login),Toast.LENGTH_SHORT).show()

                        val intent = Intent(context, MainMenuActivity::class.java)
                        context.startActivity(intent)

                    } else {

                        Toast.makeText(activity.baseContext,
                            context.getString(R.string.login_db_error), Toast.LENGTH_LONG).show()
                    }
                }
        } else Toast.makeText(activity.baseContext, activity.baseContext.getString(R.string.empty_login), Toast.LENGTH_LONG).show()



}

private fun checkLogin(email: String, password: String): Boolean {
    val emailPT = Regex("^[a-zA-Z0-9_]+@[a-zA-Z0-9.-]+.[a-z]{2,3}")
    val passwordPT = Regex("^[a-zA-Z0-9/S]{8,20}")
    return emailPT.matches(email) && passwordPT.matches(password)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ShowAlertDialog(permissions: MultiplePermissionsState, context: Context){

    var dialogText = stringResource(R.string.auth_location_alert_base_body)
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

    if (locationRevoked[0] && locationRevoked[1]){
        dialogText += stringResource(R.string.auth_location_body_location_missing)
    } else if (locationRevoked[0]) dialogText += stringResource(R.string.auth_location_body_precise_location_missing)

    val openAlertDialog = remember { mutableStateOf(true) }


    when{
        openAlertDialog.value -> AlertDialog(
            onDismissRequest = {

                openAlertDialog.value = false

            },
            title = { Text(text = stringResource(R.string.auth_alertdialog_title), modifier = Modifier) },
            text = { Text(text = dialogText, modifier = Modifier) },
            confirmButton = {
                Button(onClick = {

                    openAlertDialog.value = false
                    permissions.launchMultiplePermissionRequest()
                }, modifier = Modifier) { Text(text = stringResource(R.string.auth_allow_location)) }
            },
            dismissButton = {
                Button(onClick = {
                    openAlertDialog.value = false
                    Toast.makeText(context,
                        context.getString(R.string.auth_location_rejected_message), Toast.LENGTH_LONG).show()
                }, modifier = Modifier) { Text(text = stringResource(R.string.button_text_cancel)) }
            }
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AuthCompose(auth: FirebaseAuth, mainActivity: MainActivity) {

    val mContext = LocalContext.current

    val permissions = rememberMultiplePermissionsState(permissions = listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))

    if(permissions.revokedPermissions.isNotEmpty()){
        ShowAlertDialog(permissions, mContext)
    }

    Box (modifier = Modifier.fillMaxSize()){

        Image( painter = painterResource(R.drawable.login_background),
            contentDescription = stringResource(R.string.content_description_login_backgroud),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Row (modifier = Modifier.absolutePadding(20.dp, 40.dp, 50.dp, 100.dp)){
                Icon(
                    painter = painterResource(R.drawable.logo_no_background),
                    contentDescription = stringResource(R.string.content_description_logo_nobackg),
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(24.dp)
                        .height(40.dp)
                        .width(40.dp)
                )

                Text(text = stringResource(R.string.app_title),
                    color = Color.White, fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )

            }
            Text(
                text = "Usuario",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .absolutePadding(0.dp, 0.dp, 200.dp, 10.dp),
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif
            )

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp),
                shape = RoundedCornerShape(40),
                colors =
                TextFieldDefaults.textFieldColors(
                    colorResource(R.color.grey),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    containerColor = colorResource(R.color.light_gray)
                )
            )

            Text(
                text = "Contraseña",
                modifier = Modifier.absolutePadding(0.dp,30.dp,170.dp,10.dp),
                fontSize = 20.sp,
                fontFamily = FontFamily.SansSerif
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(40),
                colors =
                TextFieldDefaults.textFieldColors(
                    colorResource(R.color.grey),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    containerColor = colorResource(R.color.light_gray))

            )

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(0.dp, 100.dp)){
                Button(onClick = {
                    val intent = Intent(mContext, RegisterActivity::class.java)
                    mContext.startActivity(intent)
                },
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue)),
                    modifier = Modifier
                        .height(50.dp)
                        .width(300.dp)) {
                    Text(text = "Registrarse", fontSize = 24.sp, modifier = Modifier.padding(10.dp, 0.dp),
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }

                Button(onClick = {
                    val emailLogin = email
                    val passwordLogin = password
                    if(checkLogin(emailLogin,passwordLogin)){

                        loginUser(emailLogin, passwordLogin, mainActivity, auth, mContext)
                    } else{
                        Toast.makeText(
                            mContext,
                            mContext.getString(R.string.input_text_error_text),
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }, modifier = Modifier
                    .padding(15.dp)
                    .height(50.dp)
                    .width(300.dp),
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.green))
                ) {
                    Text(text = stringResource(R.string.login_button_text), fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

}




