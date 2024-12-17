package com.example.routequest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RegisterActivity : ComponentActivity() {

    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         auth = FirebaseAuth.getInstance()
        setContent {
            RoutequestTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Register( auth, this)
                }
            }
        }
    }

}

private fun checkLogin(email: String, password: String): Boolean {
    val emailPT = Regex("^[a-zA-Z0-9_]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}")
    val passwordPT = Regex("^[a-zA-Z0-9/S]{8,20}")
    return emailPT.matches(email) && passwordPT.matches(password)
}

private fun createUser(email: String, password: String, auth: FirebaseAuth, context: Context, activity: RegisterActivity) {

    if (email.isNotBlank() && password.isNotBlank() && email.isNotEmpty() && password.isNotEmpty()) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context,
                        "Usuario ${email.substringBefore("@")} creado correctamente", Toast.LENGTH_LONG).show()
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                } else {

                    if (task.exception is FirebaseAuthUserCollisionException){
                        Toast.makeText(context,
                            context.getString(R.string.register_email_in_use), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(activity.baseContext,
                            context.getString(R.string.login_db_error), Toast.LENGTH_LONG).show()
                    }

                }
            }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register(
    auth: FirebaseAuth,
    registerActivity: RegisterActivity
) {
    val mContext = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = stringResource(R.string.content_description_login_backgroud),
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(modifier = Modifier.absolutePadding(20.dp, 30.dp, 50.dp, 100.dp)) {
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

                Text(
                    text = stringResource(R.string.app_title),
                    color = Color.White, fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold
                )

            }

            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Column (horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.absolutePadding(0.dp,0.dp,0.dp,50.dp)){
                Text(
                    text = stringResource(R.string.create_account),
                    modifier = Modifier.absolutePadding(0.dp, 0.dp, 150.dp, 10.dp),
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif
                )

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.padding(10.dp),
                    shape = RoundedCornerShape(40),
                    colors =
                    TextFieldDefaults.textFieldColors(
                        colorResource(R.color.grey),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        containerColor = colorResource(R.color.light_gray)
                    )
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.padding(5.dp),
                    shape = RoundedCornerShape(40),
                    colors =
                    TextFieldDefaults.textFieldColors(
                        colorResource(R.color.grey),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        containerColor = colorResource(R.color.light_gray)
                    )
                )
            }

            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.absolutePadding(0.dp,90.dp,0.dp,100.dp)) {

                Button(
                    onClick = {
                        val emailNew = email
                        val passwordNew = password
                        if (checkLogin(emailNew, passwordNew) && auth.currentUser?.email.toString()
                                .isNotEmpty()
                        ) {
                            createUser(
                                email = emailNew,
                                password = passwordNew,
                                auth = Firebase.auth,
                                context = mContext,
                                activity = registerActivity
                            )

                        } else {
                            Toast.makeText(
                                mContext,
                                mContext.getString(R.string.input_text_error_text),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.green)),
                    modifier = Modifier
                        .height(50.dp)
                        .width(300.dp)
                ) {
                    Text(text = stringResource(R.string.register_button_text), fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        val intent = Intent(mContext, MainActivity::class.java)
                        mContext.startActivity(intent)
                    },
                    shape = RoundedCornerShape(30),
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.dark_blue)),
                    modifier = Modifier
                        .padding(15.dp)
                        .height(50.dp)
                        .width(300.dp)
                ) {

                    Text(text = stringResource(R.string.return_button_text), fontSize = 24.sp,
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium)
                }
            }

        }
    }
}
