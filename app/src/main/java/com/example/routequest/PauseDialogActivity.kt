package com.example.routequest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.routequest.ui.theme.RoutequestTheme

class PauseDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RoutequestTheme {

                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {

                    PauseAlertDialog( this )
                }

            }
        }
    }
}

@Composable
fun PauseAlertDialog( pauseDialogActivity: PauseDialogActivity) {

    val mutOpenDialog = remember { mutableStateOf(true)}

    Image(
        painter = painterResource(R.drawable.app_background),
        contentDescription = "",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )

    when {
        mutOpenDialog.value ->
            AlertDialog(onDismissRequest = {
                mutOpenDialog.value = false
                pauseDialogActivity.finish()
            },
                title = { Text(text = stringResource(R.string.pause_dialog_title)) },
                text = { Text(text = stringResource(R.string.pause_dialog_text)) },
                confirmButton = {
                    Button(onClick = {
                        mutOpenDialog.value = false
                        pauseDialogActivity.finish()
                    }) { Text(text = stringResource(R.string.pause_dialog_resume)) }
                },
                dismissButton = {
                    Button(onClick = {

                        mutOpenDialog.value = false
                        Toast.makeText(pauseDialogActivity.baseContext,
                            pauseDialogActivity.baseContext.getString(R.string.pause_dialog_quitted), Toast.LENGTH_SHORT).show()
                        val intent = Intent(pauseDialogActivity.baseContext, MainMenuActivity::class.java)
                        pauseDialogActivity.startActivity(intent)
                    }) { Text(text = stringResource(R.string.pause_dialog_quit_button)) }

                })
    }
}
