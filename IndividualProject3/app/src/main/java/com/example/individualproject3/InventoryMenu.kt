package com.example.individualproject3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Arrangement


@Composable
fun InventoryMenu(
    mode: InventoryMode,
    onModeChange: (InventoryMode) -> Unit
) {
    val bgPainter = painterResource(R.drawable.inventory_background)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),   // tweak height as needed
        contentAlignment = Alignment.Center
    ) {
        // Background frame (the tall parchment)
        Image(
            painter = bgPainter,
            contentDescription = "Inventory background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // What appears *inside* that box depends on the mode
        when (mode) {
            InventoryMode.WHEEL -> {
                SelectionWheel(
                    onCommandsClicked = { onModeChange(InventoryMode.COMMANDS) },
                    onFunctionsClicked = { onModeChange(InventoryMode.FUNCTIONS) },
                    onSpecialsClicked = { onModeChange(InventoryMode.SPECIALS) }
                )
            }

            InventoryMode.COMMANDS -> {
                CommandsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) }
                )
            }

            InventoryMode.FUNCTIONS -> {
                FunctionsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) }
                )
            }

            InventoryMode.SPECIALS -> {
                SpecialsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) }
                )
            }
        }
    }
}

@Composable
fun SelectionWheel(
    onCommandsClicked: () -> Unit,
    onFunctionsClicked: () -> Unit,
    onSpecialsClicked: () -> Unit
) {
    val wheelPainter = painterResource(R.drawable.selection_wheel)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)   // 60% of the screen width
            .aspectRatio(1f),     // keeps it perfectly circular
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = wheelPainter,
            contentDescription = "Selection wheel",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Overlay row of 3 circular click targets roughly aligned with the dark circles
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleButton(onClick = onCommandsClicked, label = "Cmd")
            CircleButton(onClick = onFunctionsClicked, label = "Fn")
            CircleButton(onClick = onSpecialsClicked, label = "Spc")
        }
    }
}

@Composable
private fun CircleButton(
    onClick: () -> Unit,
    label: String
) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = Color.Transparent,
        onClick = onClick,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.Black,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun CommandsSubMenu(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Commands", color = Color.Black)
        // TODO: place your arrow command palette here

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun FunctionsSubMenu(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Functions", color = Color.Black)
        // TODO: place your function maker UI here

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
fun SpecialsSubMenu(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Special Actions", color = Color.Black)
        // TODO: place your IF / ATTACK palette here

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}

