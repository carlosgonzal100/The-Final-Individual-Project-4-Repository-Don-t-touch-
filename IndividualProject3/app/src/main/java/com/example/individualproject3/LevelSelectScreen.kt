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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * description: inclides the screen for picking levels easy 1-3 and
 * hard levels 1-3. it also has dialog for custom levels, but for now it is
 * unused
 * */

@Composable
fun PlayCustomLevelDialog(
    customLevels: List<SavedCustomLevel>,
    onDismiss: () -> Unit,
    onPlay: (SavedCustomLevel) -> Unit
) {
    var selected by remember { mutableStateOf<SavedCustomLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a custom level") },
        text = {
            Column {
                Text(
                    "Tap a custom level to select it:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    customLevels.forEach { lvl ->
                        val isSelected = selected?.id == lvl.id
                        OutlinedButton(
                            onClick = { selected = lvl },
                            modifier = Modifier.fillMaxWidth(),
                            border = if (isSelected)
                                ButtonDefaults.outlinedButtonBorder.copy(width = 3.dp)
                            else
                                ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("${lvl.id} (${lvl.difficulty}, ${lvl.width}x${lvl.height})")
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "The custom level you pick will be played right away.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = (selected != null),
                onClick = {
                    val chosen = selected ?: return@TextButton
                    onPlay(chosen)
                }
            ) {
                Text("Play")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    levels: List<Level>,
    onBack: () -> Unit,
    onSelectGame: (Level, GameMap) -> Unit
) {
    val context = LocalContext.current

    var customLevels by remember { mutableStateOf<List<SavedCustomLevel>>(emptyList()) }
    var showCustomDialog by remember { mutableStateOf(false) }
    val bottomBg = painterResource(R.drawable.bottom_half_level_background)

    LaunchedEffect(Unit) {
        customLevels = loadCustomLevels(context)
    }

    val scrollState = rememberScrollState()

    Scaffold{ padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {
                // top half behind the grid (rotated 180°)
                Image(
                    painter = bottomBg,
                    contentDescription = "Top level background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer(rotationZ = 180f),
                    contentScale = ContentScale.FillBounds
                )

                // bottom half behind the command / function UI
                Image(
                    painter = bottomBg,
                    contentDescription = "Bottom level background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.FillBounds
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {

                // BACK BUTTON ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clickable { onBack() }
                    ) {
                        BrownGenericTitleBar(text = "Back")
                    }
                }

                // Big title at the very top below the exit button
                MainScreenTitle(text = "Level Select")

                levels.forEach { level ->
                    BluePixelTitleBar("${level.name} (${level.difficulty})")
                    Spacer(Modifier.height(8.dp))

                    level.games.forEach { game ->
                        PurplePixelButton(
                            text = "Play: ${game.id}",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelectGame(level, game) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(8.dp))
                Divider(color = DungeonBorder)
                Spacer(Modifier.height(8.dp))

                BluePixelTitleBar("Custom Levels")
                Spacer(Modifier.height(8.dp))

                if (customLevels.isEmpty()) {
                    Text(
                        "No custom levels saved yet.\nUse the Level Editor to forge your own dungeon.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DungeonTextSub
                    )
                } else {
                    PurplePixelButton(
                        text = "Play Custom Level",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showCustomDialog = true }
                    )
                }
            }
        }

        if (showCustomDialog) {
            PlayCustomLevelDialog(
                customLevels = customLevels,
                onDismiss = { showCustomDialog = false },
                onPlay = { chosen ->
                    val gameMap = chosen.toGameMap(idOverride = chosen.id)
                    val level = Level(
                        id = "custom_${chosen.id}",
                        name = "Custom: ${chosen.id}",
                        difficulty = chosen.difficulty,
                        games = listOf(gameMap)
                    )
                    onSelectGame(level, gameMap)
                    showCustomDialog = false
                }
            )
        }
    }
}