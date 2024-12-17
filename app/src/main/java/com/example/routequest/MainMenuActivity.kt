package com.example.routequest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.routequest.ui.theme.RoutequestTheme
import com.google.firebase.auth.FirebaseAuth

class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoutequestTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background){
                    MainMenu()

                }
            }
        }
    }

    private val currentUser = FirebaseAuth.getInstance().currentUser

    public override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (currentUser != null) FirebaseAuth.getInstance().signOut()
    }

    public override fun onStop() {
        super.onStop()
        if (currentUser != null) FirebaseAuth.getInstance().signOut()
    }
}

@Composable
fun MainMenu( modifier: Modifier = Modifier) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Menu", modifier = modifier)
        Text(text = FirebaseAuth.getInstance().currentUser?.email.toString(), modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    RoutequestTheme {
        MainMenu()
    }
}