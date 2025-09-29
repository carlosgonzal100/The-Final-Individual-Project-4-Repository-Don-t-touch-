package com.example.individualproject1

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LogInScreen() {

    //local context for the toast
    val context = LocalContext.current

    Scaffold(

        //displays top appbar with the app name
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Carlos Bank")
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,

        horizontalAlignment = Alignment.CenterHorizontally
        ) {

            //image at the top of the screen
            Image(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(10.dp)),

                painter = painterResource(id = R.drawable.bank),
                contentDescription = null,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                //text under the image
                Text(
                    text = "Welcome to Carlos Bank",
                    fontSize = 30.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),

                //spaces both buttons and centers horizontally
                horizontalArrangement = Arrangement.spacedBy(45.dp, Alignment.CenterHorizontally),

            ) {

                // the log in button
                Button(
                    /**when clicked, the onLogInClick lambda is called
                    * and navigates to the activity where you enter in your email
                    * and password
                    */
                    onClick = { context.startActivity(Intent(context, LogInActivity::class.java))
                    },
                    enabled = true,
                ) {
                    Text(text = "Log In", fontSize = 30.sp)
                }

                //the sign up button
                Button(
                    //works the same as the log in button using the lambda
                    onClick = { context.startActivity(Intent(context, SignUpActivity::class.java))
                    }
                ) {
                    Text(text = "Sign up", fontSize = 30.sp)
                }
            }
        }
    }
}
