package com.example.individualproject1

import SignUpScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        /** so since i had first used composable screen to make my app,
         * to convert it into using activites, i made 3 different activites.
         * the main activity, where the user can choose to sign in or log in.
         * so in the activity, the composable screen is set into an empty activity.
         * when the back button in clicked you go back to the home screen
         */
        setContent {

            SignUpScreen(
            onBackClick = { finish() },
            onSuccess = {
                Toast.makeText(this, "User has been registered", Toast.LENGTH_LONG).show()
                finish() // go back to MainActivity after toast
            }
            )


        }

    }
}
