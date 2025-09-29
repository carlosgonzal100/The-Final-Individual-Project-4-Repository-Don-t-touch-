package com.example.individualproject1

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api

/** learned how to make a toast in jetpack compose:
 * https://www.geeksforgeeks.org/kotlin/toast-in-android-jetpack-compose/
 */

/**solved problem where an there were 2 app bars, one i created,
 * and the default one right above it:
 * https://www.geeksforgeeks.org/android/different-ways-to-hide-action-bar-in-android-with-examples/
 */

/**
 * learned to use spacers to put space between elements:
 * https://www.geeksforgeeks.org/kotlin/spacer-in-android-jetpack-compose/
 */

/**
 * learned how to validate and email address:
 * https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
 */

/**
 * learned about scrollable screens and how to make them:
 * https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll
 */

/**
 * learned about window insets and how to keep the ui
 * from going behind the system bars:
 * https://developer.android.com/develop/ui/compose/system/insets
 */

/**
 * learning to save values on screen rotation:
 * https://developer.android.com/develop/ui/compose/state-saving
 */

/**
 * learned how to create a back button in the app bar and make it usable
 * in jetpack compose: https://developer.android.com/develop
 * /ui/compose/components/app-bars-navigate
 */

/**
 * used to find out how to customize the app bar
 * using jetpack compose: https://developer.android
 * .com/develop/ui/compose/components/app-bars
 */

/**
 * learned a lot if things from this tutorial:
 * https://www.geeksforgeeks.org/android/android-jetpack-compose-tutorial/
 */

/**
 * learned to load image using this:
 * https://developer.android.com/develop/ui/compose/graphics/images/loading
 */

/** (i did use this but i kept the composable screens and used activites to launch them)
 * learning to use composable screen and navigating between
 * them:https://developer.android.com/codelabs/basic-android-
 * kotlin-compose-navigation#3
 */

/**
 * youtube video gave me the idea of creating composable
 * screens and navigating between them: https://www.youtube
 * .com/watch?v=4gUeyNkGE3g
 */

/**
 * learned how to create a splash screen :
 * https://www.geeksforgeeks.org/android/splash-screen-in-android/
 */

class MainActivity : ComponentActivity() {

    private var keepSplash = true

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {


        //Splash screen is installed at the start of the app and
        //is kept on the screen intil "keepSplash" is false
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplash }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            //the log in screen composable is the main activity
            LogInScreen(

                //the sign up  activity
                onSignUpClick = {
                    startActivity(Intent(this, SignUpActivity::class.java))
                },

                //the log in activity
                onLogInClick = {
                    // This is the screen where the user enters email/password
                    startActivity(Intent(this, LogInActivity::class.java))
                }
            )
        }


        /**
         * control spashscreen lifecycle by keeping the spashscreen
         * up for 3 seconds and then allow it to be dismissed by
         * turning "keepSplash" to false
         */
        lifecycleScope.launch {
            delay(3000) // Wait 3 seconds
            keepSplash = false // Allow splash screen to be dismissed
        }
    }
}

