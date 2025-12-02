package com.example.individualproject3

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border

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
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.graphicsLayer


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
                    onBack = { onModeChange(InventoryMode.WHEEL) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            InventoryMode.FUNCTIONS -> {
                FunctionsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            InventoryMode.SPECIALS -> {
                SpecialsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) },
                    modifier = Modifier.fillMaxSize()
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
fun CommandsSubMenu(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val holderPainter = painterResource(R.drawable.command_arrow_holder)
    val arrowPainter  = painterResource(R.drawable.command_arrow)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // title at top
        SubMenuTitle(text = "Movement Commands")

        // center area with the arrow grid – nudged a bit upward
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                //this part moves the grid of arrows up and down
                modifier = Modifier.padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // UP (base arrow points LEFT, see rotations in section 3)
                    ArrowCommandSlot(
                        cmd = Command.MOVE_UP,
                        rotation = 90f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter
                    )
                    // RIGHT
                    ArrowCommandSlot(
                        cmd = Command.MOVE_RIGHT,
                        rotation = 180f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT
                    ArrowCommandSlot(
                        cmd = Command.MOVE_LEFT,
                        rotation = 0f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter
                    )
                    // DOWN
                    ArrowCommandSlot(
                        cmd = Command.MOVE_DOWN,
                        rotation = -90f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter
                    )
                }
            }
        }

        // back button anchored to bottom-left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            SubMenuBackButton(onClick = onBack)
        }
    }
}


@Composable
fun FunctionsSubMenu(onBack: () -> Unit,
                     modifier: Modifier = Modifier
    ) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubMenuTitle(text = "Function Maker")

        // TODO: place your function maker UI here

        Spacer(modifier = Modifier.weight(1f))   // pushes back button down

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            SubMenuBackButton(onClick = onBack)
        }
    }
}

@Composable
fun SpecialsSubMenu(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val holderPainter = painterResource(R.drawable.special_actions_holder)
    val attackIcon = painterResource(R.drawable.sword_icon)   // or whatever icon you like
    val ifIcon = painterResource(R.drawable.if_tile)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),      // so we can push the back button to the bottom
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // title bar at the top
        SubMenuTitle(text = "Special Commands")

        // big holder bar with flames + the draggable specials
        // big holder bar with flames + the draggable specials
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 45.dp)
                .height(80.dp),          // height of the specials bar
            contentAlignment = Alignment.Center
        ) {
            val barWidth = maxWidth         // full width of the flame holder
            val barHeight = maxHeight
            val iconSize = 48.dp            // your icon size

            // Background bar
            Image(
                painter = holderPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .fillMaxHeight(),
                contentScale = ContentScale.FillBounds
            )

            // Place icons relative to the bar size (SAFE, dynamic)
            Box(modifier = Modifier.fillMaxSize()) {

                // LEFT ICON
                SpecialActionSlot(
                    payload = "ATTACK",
                    iconPainter = attackIcon,
                    modifier = Modifier
                        .size(iconSize)
                        .absoluteOffset(
                            x = barWidth * 0.26f - iconSize / 2,
                            y = barHeight / 2 - iconSize / 2   // vertically centered
                        )
                )

                // RIGHT ICON
                SpecialActionSlot(
                    payload = "IF_TILE",
                    iconPainter = ifIcon,
                    modifier = Modifier
                        .size(iconSize)
                        .absoluteOffset(
                            x = barWidth * 0.75f - iconSize / 2,
                            y = barHeight / 2 - iconSize / 2
                        )
                )
            }
        }


        // eat the remaining vertical space so the back button hugs the bottom
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            SubMenuBackButton(onClick = onBack)
        }
    }
}


@Composable
fun SubMenuTitle(text: String) {
    val titlePainter = painterResource(R.drawable.sub_menu_title)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),   // adjust to your PNG height
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = titlePainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            text = text,
            color = Color.Black,
            fontSize = 18.sp
        )
    }
}

@Composable
fun SubMenuBackButton(onClick: () -> Unit) {
    Image(
        painter = painterResource(R.drawable.back_button),
        contentDescription = "Back",
        modifier = Modifier
            .size(40.dp)                 // adjust if needed
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun ArrowCommandSlot(
    cmd: Command,
    rotation: Float,
    holderPainter: Painter,
    arrowPainter: Painter
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .dragAndDropSource(
                transferData = {
                    DragAndDropTransferData(
                        ClipData.newPlainText(
                            "command",
                            when (cmd) {
                                Command.MOVE_UP -> "UP"
                                Command.MOVE_DOWN -> "DOWN"
                                Command.MOVE_LEFT -> "LEFT"
                                Command.MOVE_RIGHT -> "RIGHT"
                                else -> ""
                            }
                        )
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // square holder background
        Image(
            painter = holderPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // rotated arrow icon
        Image(
            painter = arrowPainter,
            contentDescription = cmd.name,
            modifier = Modifier
                .fillMaxSize(0.7f)
                .graphicsLayer { rotationZ = rotation },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun SpecialActionSlot(
    payload: String,
    iconPainter: Painter,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .dragAndDropSource(
                transferData = { _ ->
                    DragAndDropTransferData(
                        ClipData.newPlainText("special", payload)
                    )
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = iconPainter,
            contentDescription = payload,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
