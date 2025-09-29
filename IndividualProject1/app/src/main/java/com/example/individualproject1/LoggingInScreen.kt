package com.example.individualproject1


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingInScreen(


    /** these are call back methods provided by
     * logInActivity.
     */
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {

    /**
     * rememberSaveable is used to save the entered email and password
     * and is used to restore the entered values when the screen is rotated.
     * both variables start as empty strings and are updated when the user
     */
    var enteredEmail by rememberSaveable { mutableStateOf("") }
    var enteredPassword by rememberSaveable { mutableStateOf("") }
    var loginError by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {

            //used to create the appbar on the top part of the app
            TopAppBar(

                //controls color of the appbar and the text in the appbar
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(" Please Log In") },
                navigationIcon = {

                    /**
                     * used to create the back button in the appbar and
                     *  used to navigate back to the previous screen
                     */
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Enter your email and password to log in:",
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
                )

            /**
             * used to create a outlined textfield for the user to enter
             * their email. same is for the password textfield. whatever is
             * entered in value, is shown on the textfield. when a user types
             * into the textfield, logInError is set to false to indicate that
             * there is currently no error until the info in the textfield is
             * checked
             */
            OutlinedTextField(
                value = enteredEmail,
                onValueChange = {
                    enteredEmail = it
                    loginError = false
                },

                //text shown in the textfield and goes away when clicked
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            //explanation of what the textfield is used for above
            OutlinedTextField(
                value = enteredPassword,

                onValueChange = {
                    enteredPassword = it
                    loginError = false
                },

                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            /**
             * if log in error is true, then this text is shown to the user
             * to show that there is an error by showing a text below the textfields
             * and by turning the color of the text to red.
             */
            if (loginError) {
                Text(
                    text = "User doesn't exist or credentials are incorrect.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {

                    /** used to validate the users entered email and password.
                     * compares the users string against the User email and password
                     * held in the user object in the signUpScreen class. if a valid login
                     * happens, the user is automatically sent to the main screen after a toast is shown
                     * else, the loginError variable is set to true to show an error to the user.
                     */
                    if (enteredEmail == User.email && enteredPassword == User.password) {

                        //finishes the activity and sends a toasts
                        onSuccess()

                    }
                    else {

                        loginError = true

                    }
                },

                modifier = Modifier.padding(top = 24.dp)

            ) {

                //Text on the button
                Text("Log In")

            }
        }
    }
}

/** used to preview the screen in the emulator
@Preview(showBackground = true)
@Composable
fun LoggingInScreenPreview() {
    LoggingInScreen(onBackClick = {})
}
 */
