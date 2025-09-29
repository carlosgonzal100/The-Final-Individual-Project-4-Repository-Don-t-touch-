package com.example.individualproject1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class LogInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {

            LoggingInScreen(

                //if the user just clicks the back arrow, the user is sent to the
                //main screena and the activity ends
                onBackClick = {
                    finish() },

                /** when the user registers succesfully, a toast is shown and then
                 * the activity ends
                 */
                onSuccess = {
                    Toast.makeText(this, "User has logged in", Toast.LENGTH_LONG).show()
                    finish()

                }
            )
        }
    }
}
