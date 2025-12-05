package com.example.individualproject3

import android.content.ClipData
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

import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer


@Composable
fun InventoryMenu(
    mode: InventoryMode,
    onModeChange: (InventoryMode) -> Unit,
    functionSlots: List<Command?>,
    functionRepeatCount: Int,
    functionCommands: MutableList<Command>,
    userFunctions: List<UserFunction>,
    unusedFunctionIds: MutableList<Int>,
    nextGemColorIndex: Int,
    maxFunctions: Int,
    isRunning: Boolean,
    onUpdateSlots: (List<Command?>) -> Unit,
    onUpdateRepeat: (Int) -> Unit,
    onUpdateNextGemColor: (Int) -> Unit,
    onClearFunction: () -> Unit,
    onGenerateFunction: (List<Command>, Int) -> Unit,
    onStatusMessage: (String) -> Unit,
    latestFunctionId: Int?,
    functionResetCounter: Int       // 👈 NEW


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

                val latestFunctionWithGem =
                    userFunctions.lastOrNull { unusedFunctionIds.contains(it.id) }

                FunctionsSubMenu(
                    onBack = { onModeChange(InventoryMode.WHEEL) },
                    latestFunction = latestFunctionWithGem,          // 👈 pass the actual function
                    onGenerateFunction = onGenerateFunction,
                    onClearFunction = onClearFunction,
                    unusedFunctionIds = unusedFunctionIds,
                    functionResetCounter = functionResetCounter,
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
fun FunctionsSubMenu(
    onBack: () -> Unit,
    latestFunction: UserFunction?,                 // 👈 change type
    onGenerateFunction: (List<Command>, Int) -> Unit,
    onClearFunction: () -> Unit,
    unusedFunctionIds: List<Int>,
    functionResetCounter: Int,           // 👈 NEW
    modifier: Modifier = Modifier
) {
    val holderPainter = painterResource(R.drawable.command_arrow_holder)
    val arrowPainter = painterResource(R.drawable.command_arrow)
    val gemHolderPainter = painterResource(R.drawable.gem_holder)
    val attackPainter = painterResource(R.drawable.sword_icon)   // 👈 ADD THIS

    // loop state for this submenu – resets when functionResetCounter changes
    var loopCount by remember(functionResetCounter) { mutableStateOf(1) }

    // 4 commands that define the function – also reset by functionResetCounter
    var slots by remember(functionResetCounter) { mutableStateOf(List(4) { null as Command? }) }

    val loopPainter = painterResource(R.drawable.loop)


    // Gem painter based on the *actual* latest function’s color
    val gemPainter = when (latestFunction?.color) {
        GemColor.RED   -> painterResource(R.drawable.red_gem)
        GemColor.BLUE  -> painterResource(R.drawable.blue_gem)
        GemColor.GREEN -> painterResource(R.drawable.green_gem)
        GemColor.PURPLE-> painterResource(R.drawable.purple_gem)
        null           -> null
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // title bar
        SubMenuTitle(text = "Function Maker")

        // MAIN AREA (centered vertically)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // GEM HOLDER + GEM
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(width = 80.dp, height = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // hands background
                    Image(
                        painter = gemHolderPainter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    // ✅ Only show gem if this function still has an unused gem
                    val shouldShowGem =
                        latestFunction != null && unusedFunctionIds.contains(latestFunction.id)

                    if (shouldShowGem && gemPainter != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .dragAndDropSource(
                                    transferData = {
                                        DragAndDropTransferData(
                                            ClipData.newPlainText(
                                                "command",
                                                "FUNC_${latestFunction.id}"   // 👈 use the real id
                                            )
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = gemPainter,                       // 👈 now safe (not null here)
                                contentDescription = "Function gem",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                }

                // 4 function slots in a row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        FunctionSlot(
                            cmd = slots[index],
                            holderPainter = holderPainter,
                            arrowPainter = arrowPainter
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // tap-to-add arrow buttons under the slots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    fun addCommand(cmd: Command) {
                        val firstEmpty = slots.indexOfFirst { it == null }
                        if (firstEmpty != -1) {
                            val newList = slots.toMutableList()
                            newList[firstEmpty] = cmd
                            slots = newList
                        }
                    }

                    FunctionCommandButton(
                        cmd = Command.MOVE_UP,
                        rotation = 90f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter,
                        onClick = { addCommand(Command.MOVE_UP) }
                    )
                    FunctionCommandButton(
                        cmd = Command.MOVE_RIGHT,
                        rotation = 180f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter,
                        onClick = { addCommand(Command.MOVE_RIGHT) }
                    )
                    FunctionCommandButton(
                        cmd = Command.MOVE_DOWN,
                        rotation = -90f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter,
                        onClick = { addCommand(Command.MOVE_DOWN) }
                    )
                    FunctionCommandButton(
                        cmd = Command.MOVE_LEFT,
                        rotation = 0f,
                        holderPainter = holderPainter,
                        arrowPainter = arrowPainter,
                        onClick = { addCommand(Command.MOVE_LEFT) }
                    )

                    FunctionCommandButton(
                        cmd = Command.ATTACK,
                        rotation = 0f,
                        holderPainter = holderPainter,
                        arrowPainter = painterResource(R.drawable.sword_icon),
                        onClick = { addCommand(Command.ATTACK) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ===== BOTTOM ROW: Back | Clear | Generate | Loop | +/- =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BACK on the far left
                    SubMenuBackButton(onClick = onBack)

                    Spacer(modifier = Modifier.width(32.dp))

                    // CLEAR
                    Image(
                        painter = painterResource(R.drawable.clear_button),
                        contentDescription = "Clear function",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                // reset slots + loop + hide gem
                                slots = List(4) { null }
                                loopCount = 1

                                // tell GameScreen to clear its builder state
                                onClearFunction()
                            },
                        contentScale = ContentScale.Fit
                    )

                    // GENERATE
                    Image(
                        painter = painterResource(R.drawable.generate_button),
                        contentDescription = "Generate function",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                val commands = slots.filterNotNull()
                                if (commands.isNotEmpty()) {
                                    // Tell GameScreen to create the function/gem
                                    onGenerateFunction(commands, loopCount)

                                    // 🔹 NOW clear the local function builder in the submenu
                                    slots = List(4) { null }
                                    loopCount = 1
                                }
                            },
                                contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.size(25.dp))

                    // LOOP ICON + NUMBER
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = loopPainter,
                            contentDescription = "Loop count",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = loopCount.toString(),
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }

                    // + and - buttons
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // GREEN +
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                .clickable { loopCount++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 22.sp)
                        }

                        // RED -
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                                .clickable { if (loopCount > 1) loopCount-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontSize = 22.sp)
                        }
                    }
                }
            }
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

@Composable
private fun DPadButton(
    painter: Painter,
    rotation: Float,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = Color.Transparent,
        onClick = onClick,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .graphicsLayer { rotationZ = rotation },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun FunctionGem(
    gemPainter: Painter,
    enabled: Boolean
) {
    val baseModifier = Modifier
        .size(40.dp)
        .graphicsLayer(alpha = if (enabled) 1f else 0.4f)

    val dragModifier = if (enabled) {
        baseModifier.dragAndDropSource(
            transferData = {
                DragAndDropTransferData(
                    ClipData.newPlainText("FUNCTION", "FUNCTION")
                )
            }
        )
    } else {
        baseModifier
    }

    Box(
        modifier = dragModifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = gemPainter,
            contentDescription = "Function gem",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun FunctionSlot(
    cmd: Command?,                // 🔹 now takes the command in this slot
    holderPainter: Painter,
    arrowPainter: Painter
) {
    Box(
        modifier = Modifier
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = holderPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        if (cmd != null) {
            if (cmd == Command.ATTACK) {
                // Show sword icon for ATTACK instead of an arrow
                val attackPainter = painterResource(R.drawable.sword_icon)
                Image(
                    painter = attackPainter,
                    contentDescription = "Attack",
                    modifier = Modifier
                        .fillMaxSize(0.7f),
                    contentScale = ContentScale.Fit
                )
            } else {
                val rotation = when (cmd) {
                    Command.MOVE_UP    -> 90f
                    Command.MOVE_RIGHT -> 180f
                    Command.MOVE_DOWN  -> -90f
                    Command.MOVE_LEFT  -> 0f
                    else               -> 0f
                }
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

    }
}

@Composable
private fun FunctionCommandButton(
    cmd: Command,
    rotation: Float,
    holderPainter: Painter,
    arrowPainter: Painter,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = holderPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
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
fun MainScreenTitle(text: String) {
    val titlePainter = painterResource(R.drawable.main_title_holder)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),   // tweak if you want taller/shorter
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
            fontSize = 22.sp
        )
    }
}

/**
 * Generic pixel button using generic_button.png
 */
@Composable
fun BluePixelMenuButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.generic_blue_button),
            contentDescription = text,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 18.sp
        )
    }
}

@Composable
fun PurplePixelButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)                     // matches the PNG proportions
            .fillMaxWidth()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.purple_level_select_buttons), // your purple asset
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
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
fun BrownGenericTitleBar(text: String) {
    val bg = painterResource(R.drawable.brown_generic_title)

    // Measure text so the box can size to fit it
    val textMeasurer = rememberTextMeasurer()
    val measured = textMeasurer.measure(
        AnnotatedString(text),
        style = TextStyle(fontSize = 18.sp)
    )

    // Add padding around text so the graphic has breathing room
    val horizontalPadding = 24.dp
    val totalWidth = with(LocalDensity.current) {
        measured.size.width.toDp() + horizontalPadding * 2
    }

    Box(
        modifier = Modifier
            .width(totalWidth)
            .height(40.dp),    // Adjust if your PNG height differs
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = bg,
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
fun BluePixelTitleBar(text: String) {

    val bg = painterResource(R.drawable.generic_blue_button)

    // Measure text so the box can size to fit exactly
    val textMeasurer = rememberTextMeasurer()
    val measured = textMeasurer.measure(
        AnnotatedString(text),
        style = TextStyle(fontSize = 18.sp)
    )

    // Padding around text so the title bar looks good
    val horizontalPadding = 24.dp
    val totalWidth = with(LocalDensity.current) {
        measured.size.width.toDp() + horizontalPadding * 2
    }

    Box(
        modifier = Modifier
            .width(totalWidth)     // Auto-sized width
            .height(48.dp),        // Adjust if your blue PNG is taller/shorter
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = bg,
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

