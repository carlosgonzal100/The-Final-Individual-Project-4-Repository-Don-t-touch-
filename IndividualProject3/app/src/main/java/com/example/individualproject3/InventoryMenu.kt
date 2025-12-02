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
    val wheelPainter = painterResource(R.drawable.selection_wheel) // your cleaned HD wheel

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.65f)   // wheel is 65% of inventory width
            .aspectRatio(1f),     // keep it a circle
        contentAlignment = Alignment.Center
    ) {
        val wheelSize = maxWidth
        val buttonSize = wheelSize * 0.22f   // slightly smaller buttons

        Image(
            painter = wheelPainter,
            contentDescription = "Selection wheel",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        fun Modifier.atCircle(fx: Float, fy: Float): Modifier =
            this
                .offset(
                    x = wheelSize * fx - buttonSize / 2f,
                    y = wheelSize * fy - buttonSize / 2f
                )
                .size(buttonSize)

        // TOP CIRCLE – movement / commands
        CircleButton(
            onClick = onCommandsClicked,
            iconRes = R.drawable.movement_icon,
            modifier = Modifier.atCircle(
                fx = 0.13f,
                fy = -0.07f
            )
        )

        // BOTTOM LEFT – functions
        CircleButton(
            onClick = onFunctionsClicked,
            iconRes = R.drawable.function_maker_icon,
            modifier = Modifier.atCircle(
                fx = -0.02f,
                fy = 0.20f
            )
        )

// BOTTOM RIGHT – specials
        CircleButton(
            onClick = onSpecialsClicked,
            iconRes = R.drawable.special_actions_icon,
            modifier = Modifier.atCircle(
                fx = 0.29f,
                fy = 0.20f
            )
        )
    }
}



@Composable
private fun CircleButton(
    onClick: () -> Unit,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,    // size is set from outside (atCircle)
        shape = CircleShape,
        color = Color.Transparent,
        onClick = onClick,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f), // icon fits inside circle
                contentScale = ContentScale.Fit
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

