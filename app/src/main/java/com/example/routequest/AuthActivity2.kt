package com.example.routequest

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class CreateUserActivity : ComponentActivity() {

    private  lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         auth = FirebaseAuth.getInstance()
        setContent {
            RoutequestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting2()
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Created User", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUser(email: String, password: String, auth: FirebaseAuth) {

        if (email.isNotBlank() && password.isNotBlank() && !(email.isNullOrEmpty()) && !(password.isNullOrEmpty())) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        Log.d(TAG, "" + user.toString())
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }

    }

    private fun checkLogin(email: String, password: String): Boolean {
        val emailPT = Regex("^[a-zA-Z0-9_]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}")
        val passwordPT = Regex("^[a-zA-Z0-9/S]{8,20}")
        return emailPT.matches(email) && passwordPT.matches(password)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Greeting2(modifier: Modifier = Modifier) {
        val mContext = LocalContext.current
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear nuevo usuario",
                modifier = modifier.padding(10.dp),
                fontSize = 20.sp
            )

            var email by remember { mutableStateOf("") }
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.padding(5.dp)
            )

            var password by remember { mutableStateOf("") }
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.padding(5.dp)
            )

            Button(onClick = {
                var emailNew = email
                var passwordNew = password
                if (checkLogin(emailNew,passwordNew) && !(auth.currentUser?.email.toString().isNullOrEmpty())){
                    createUser(emailNew, passwordNew, auth = Firebase.auth)
                    //loginUser(emailNew,passwordNew)
                    val intent = Intent(mContext, MainActivity::class.java)
                    mContext.startActivity(intent)
                } else{
                    Toast.makeText(
                        baseContext,
                        "Error de creación de usuario. El email tiene que ser uno válido y la contraseña tener de 8 a 20 caracteres.",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }) {
                Text(text = "Registrarse")
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun Greeting2Preview() {
        RoutequestTheme {
            Greeting2()
        }
    }
}