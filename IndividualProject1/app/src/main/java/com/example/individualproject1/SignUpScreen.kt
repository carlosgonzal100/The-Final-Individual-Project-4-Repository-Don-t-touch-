import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(

    /**these are callback methods provided by signUpActivity.
     */
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {

    /** remember saveable is used to remember the entered values
     * and restore them when the screen is rotated. starts with a
     * string of empty values and is updated when the user enters
     * a value in the textfield.
     */
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    /** the same is used to keep track of if there is an error
     * in a textfield. these are used so when the screen is rotated
     * the errors are remembered and not reset. global error is used
     * to keep track if there is any errors in any text field. the user
     * cannot register if global error is true. all errors start false
     */
    var firstNameError by rememberSaveable { mutableStateOf(false) }
    var lastNameError by rememberSaveable { mutableStateOf(false) }
    var dobError by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf(false) }
    var globalError by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        /** the appbar is created here with the title "Register" and
         * a back button is put into the appbar to provide a way to
         * navigate to the previous screen at any time in the app
         */
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Register") },
                navigationIcon = {
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
            /**
             * the ime padding method is very important.
             * if i type into a textfield, when i click it the keyboard
             * goes over the texfield and i cant see it. the ime padding
             * shifts the UI up to the keyboard is right below the textfield
             */
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .padding(16.dp)
                //allows the user to scroll vertically when some items are offscreen
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /** the firstName text field. outlined and has a label of "First Name".
             * goes away when the userr clicks the textfield. when the textfield
             * is filled firstNameError is set to false until it is validated at the end.
             */
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = false
                },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            /**
             * if first name has an error, this error text is shown below the first name textfield
             * and has red colored text to show an error
             */
            if (firstNameError)
                Text("First name must be 3–30 characters.", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = false
                },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
            )

            /**
             * if last name has an error, this error text is shown below the last name textfield
             * and has red colored text to show an error
             */
            if (lastNameError)
                Text("Last name must be 3–30 characters.", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = dob,
                onValueChange = {
                    dob = it
                    dobError = false
                },
                label = { Text("Date of Birth") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
            )

            /**
             * if dob has an error, this error text is shown below the dob textfield
             * and has red colored text to show an error
             */
            if (dobError)
                Text("Date must be in format YYYY-MM-DD.", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = false
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
            )

            /**
             * if email has an error, this error text is shown below the email textfield
             * and has red colored text to show an error
             */
            if (emailError)
                Text("Please enter a valid email.", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            /**
             * if password has an error, this error text is shown below the password textfield
             * and has red colored text to show an error
             */
            if (passwordError)
                Text("Password must be at least 6 characters.", color = MaterialTheme.colorScheme.error)

            /**
             * if there is any errors in the textfields, this text is shown below the textfields
             * and has red colored text to show that there is an error and to fix them all
             */
            if (globalError) {
                Text(
                    "Please fix the above errors.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            /**
             * this button used to check and validate all the textfields.
             */
            Button(
                onClick = {
                    // Reset errors
                    globalError = false


                    /**
                     * first name, last name and passwords are simply checked by
                     * checking if they are long enough. for date of birth, the
                     * length has to be 10, the 4th and 7th index must be a dash.
                     * the email is checked by using a pattern matcher to check
                     */
                    firstNameError = firstName.length !in 3..30
                    lastNameError = lastName.length !in 3..30
                    dobError = dob.length != 10 || dob[4] != '-' || dob[7] != '-'
                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    passwordError = password.length < 6

                    /** if any text field has an error, global errors is set to true. the user
                     * is not sent to the main screen and the error texts are shown beneath the
                     * textfields
                     */
                    if (firstNameError || lastNameError || dobError || emailError || passwordError) {
                        globalError = true
                    } else {

                        /** if all text fields are valid, the user is sent to the main screen
                         * and the entered values are saved in the user object.
                         */
                        User.firstName = firstName
                        User.lastName  = lastName
                        User.dob       = dob
                        User.email     = email
                        User.password  = password

                        //finishes the activity and sends a toast
                        onSuccess()

                    }
                },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),

                //shapes button into a rounded corner shape
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Up")
            }

        }
    }
}

/**
 * the user object is used to save the entered values in the
 * textfields and restore them when the screen is rotated.
 */
object User {
    var firstName: String = ""
    var lastName: String = ""
    var dob: String = ""
    var email: String = ""
    var password: String = ""

}

/**
@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(onBackClick = {})
}
        */