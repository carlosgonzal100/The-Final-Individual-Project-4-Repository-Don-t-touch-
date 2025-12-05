package com.example.individualproject3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Author: Carlos Gonzalez with the assistance of AI(Chat Gpt)
 * Ram Num: R02190266
 * description: holds the composable for the first screen the
 * parent sees, the parent login screen and the composable
 * for the parent home screen, which is where the main UI is.
 * also it holds and facilitates dialog when the parent adds a child
 */

//-----------The parent log in screen------------//
//they first see this composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentLoginScreen(
    existingParent: ParentAccount?,
    onParentCreatedOrLoggedIn: (ParentAccount) -> Unit
) {
    val context = LocalContext.current

    var registrationMode by remember { mutableStateOf(existingParent == null) }
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    val hasExistingParent = existingParent != null
    val bottomBg = painterResource(R.drawable.bottom_half_level_background)



    Scaffold{ padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 🔹 BACKGROUND: two halves using bottom_half_level_background
            Column(modifier = Modifier.fillMaxSize()) {
                // Top half – rotated 180 degrees
                Image(
                    painter = bottomBg,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer { rotationZ = 180f },
                    contentScale = ContentScale.FillBounds
                )

                // Bottom half – normal
                Image(
                    painter = bottomBg,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.FillBounds
                )
            }

            // 🔹 FOREGROUND: your original Column, just without the .background(DungeonBg)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 👉 MOVE your Box-with-arrow-holder here, unchanged

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.log_in_screen_box),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SubMenuTitle(
                            text = if (registrationMode) "Create Parent Account" else "Enter Dungeon PIN"
                        )

                        Spacer(Modifier.height(16.dp))

                        if (registrationMode) {
                            //Put the same background picture for the name
                            //if the parent wants to register as well

                            //put a nice background image behind the enter pin textfield
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // background PNG
                                Image(
                                    painter = painterResource(R.drawable.text_field_container),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )

                                TextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    placeholder = {
                                        Text(
                                            "Your Name",
                                            color = Color.Black.copy(alpha = 0.5f)
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        errorContainerColor = Color.Transparent,

                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        errorIndicatorColor = Color.Transparent,

                                        cursorColor = Color.Black,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        //put a nice background image behind the enter pin textfield
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // background PNG
                            Image(
                                painter = painterResource(R.drawable.text_field_container),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            TextField(
                                value = pin,
                                onValueChange = { value ->
                                    pin = value.filter { it.isDigit() }.take(6)
                                    status = ""
                                },
                                placeholder = {
                                    Text(
                                        "PIN (numbers)",
                                        color = Color.Black.copy(alpha = 0.4f)
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent),

                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,

                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    errorIndicatorColor = Color.Transparent,

                                    cursorColor = Color.Black,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )

                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (registrationMode) {
                                    if (name.isBlank() || pin.length < 3) {
                                        status = "Enter a name and a PIN (at least 3 digits)."
                                    } else {
                                        val parent = ParentAccount(
                                            id = "parent_1",
                                            name = name.trim(),
                                            pin = pin
                                        )
                                        saveParentAccount(context, parent)
                                        status = ""
                                        onParentCreatedOrLoggedIn(parent)
                                    }
                                } else {
                                    val p = existingParent
                                    if (p != null && pin == p.pin) {
                                        status = ""
                                        onParentCreatedOrLoggedIn(p)
                                    } else {
                                        status = "Incorrect PIN."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DungeonAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(if (registrationMode) "Create Account" else "Enter Dungeon")
                        }

                        if (!registrationMode && hasExistingParent) {
                            Spacer(Modifier.height(8.dp))
                            TextButton(
                                onClick = {
                                    context.deleteFile("parent_account.json")
                                    context.deleteFile("children.json")
                                    context.deleteFile("progress_log.csv")
                                    registrationMode = true
                                    status = "Old parent removed. Create a new parent account."
                                    name = ""
                                    pin = ""
                                }
                            ) {
                                Text(
                                    "Register New Parent (Reset)",
                                    color = Color(0xFFFF8080)
                                )
                            }
                        }

                        if (status.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(text = status, color = Color(0xFFFF8080))
                        }
                    }
                }
            }
        }
    }
}

//--------The parent home screen-------------//
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    parent: ParentAccount,
    children: List<ChildAccount>,
    currentChild: ChildAccount?,
    onChildrenChanged: (List<ChildAccount>) -> Unit,
    onSelectChild: (ChildAccount) -> Unit,
    onPlayAsChild: () -> Unit,
    onOpenEditor: () -> Unit,
    onViewStats: () -> Unit,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DungeonBg,
        topBar = {
            TopAppBar(
                title = { Text("Dungeon Control", color = DungeonTextMain) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DungeonPanel,
                    titleContentColor = DungeonTextMain
                ),
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = DungeonAccent)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DungeonBg)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Warden: ${parent.name}",
                style = MaterialTheme.typography.titleMedium,
                color = DungeonTextMain
            )
            Spacer(Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DungeonPanel,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Adventurers:", color = DungeonTextSub)
                    Spacer(Modifier.height(4.dp))

                    if (children.isEmpty()) {
                        Text("No children registered yet.", color = DungeonTextSub)
                    } else {
                        children.forEach { child ->
                            val isActive = currentChild?.id == child.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSelectChild(child) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = child.name + if (isActive) " (Active)" else "",
                                    modifier = Modifier.weight(1f),
                                    color = DungeonTextMain
                                )
                                TextButton(
                                    onClick = {
                                        val updated = children.filter { it.id != child.id }
                                        onChildrenChanged(updated)
                                    }
                                ) {
                                    Text("Remove", color = Color(0xFFFF8080))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DungeonAccent,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Register New Child")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Active adventurer: ${currentChild?.name ?: "None"}",
                color = DungeonTextMain
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (currentChild == null) {
                        status = "Select a child first."
                    } else {
                        status = ""
                        onPlayAsChild()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DungeonAccent,
                    contentColor = Color.Black
                )
            ) {
                Text("Play Game")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onOpenEditor,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DungeonPanel,
                    contentColor = DungeonTextMain
                )
            ) {
                Text("Open Level Editor")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onViewStats,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DungeonPanel,
                    contentColor = DungeonTextMain
                )
            ) {
                Text("View Stats")
            }

            if (status.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(status, color = Color(0xFFFF8080))
            }
        }

        if (showAddDialog) {
            AddChildDialog(
                onDismiss = { showAddDialog = false },
                onChildCreated = { name, age, notes ->
                    val newChild = ChildAccount(
                        id = "child_${System.currentTimeMillis()}",
                        name = name,
                        age = age,
                        notes = notes,
                        parentId = parent.id
                    )
                    onChildrenChanged(children + newChild)
                    showAddDialog = false
                }
            )
        }
    }
}

// ---------- DIALOG TO ADD CHILD ----------
@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onChildCreated: (String, Int?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Child") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Child Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Age (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        error = "Name is required."
                    } else {
                        val age = ageStr.takeIf { it.isNotBlank() }?.toIntOrNull()
                        onChildCreated(name.trim(), age, notes.takeIf { it.isNotBlank() })
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}