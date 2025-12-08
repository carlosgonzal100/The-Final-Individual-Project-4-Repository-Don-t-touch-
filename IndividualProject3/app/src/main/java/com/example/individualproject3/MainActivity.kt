@file:OptIn(ExperimentalFoundationApi::class)
package com.example.individualproject3

import IndividualProject3Theme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Author: Carlos Gonzalez with the assistance of AI(Chat Gpt)
 * Ram Num: R02190266
 * description: contains the enum for all screens, applies the dungeon theme
 * to the app, helps show the stat bar in the stats screen using colors and
 * helps facilitate navigation in the app by defining screens and moving to them
 */

/**notes:
 * got the fail sound filr here:
 * https://www.myinstants.com/en/instant/botw-game-over-25928/
 *
 * got the victory sound file here:
 * https://www.myinstants.com/en/instant/vlp-zelda-victory-94573/
 *
 * got floor tiles and wall tiles here:
 * https://www.spriters-resource.com/gamecube/legendofzeldafourswordsadventures/asset/93739/
 *
 * got falling in water soundeffect here:
 * https://pixabay.com/sound-effects/falling-game-character-352287/
 *
 * got the bump into wall sound effect here:
 * https://pixabay.com/sound-effects/thump-105302/
 *
 * took bits and pieces for the map design from this website:
 * https://www.spriters-resource.com/game_boy_advance/thelegendofzeldaalinktothepast/asset/20701/
 *
 * took the bits and pieces from this image for the selection wheel using art from
 * these 2 sourcess:
 * https://www.spriters-resource.com/ds_dsi/thelegendofzeldaphantomhourglass/asset/22843/
 * https://www.spriters-resource.com/ds_dsi/thelegendofzeldaphantomhourglass/asset/8598/
 *
 * got some icons for the sub menu buttons here:
 * https://www.spriters-resource.com/ds_dsi/thelegendofzeldaphantomhourglass/asset/8599/
 *
 * got the button generate photo here:
 * https://www.shutterstock.com/search/pixel-art-button?dd_referrer=https%3A%2F%2Fwww.google.com%2F
 *
 * got the d-pad in the function maker here:
 * https://forums.rpgmakerweb.com/index.php?threads/d-pad-action-button-plugin-request.48213/
 *
 * top left exit button in the game screen was taken from here:
 * https://www.shutterstock.com/image-vector/pixel-exit-button-vector-illustration-2206589721
 *
 * recieved the pixel font theme fron this website:
 * https://fonts.google.com/specimen/Tiny5?query=pixel
 *
 * got my app icon from here:
 * https://www.flaticon.com/search?word=video%20game
 *
 * got the background for my app icon here:
 * https://stock.adobe.com/search?k=dungeon+wall&asset_id=962355507
 * TODO: Added small change so Git can detect modifications*/


// Simple dungeon color palette and defines
//the colors so i can use them
val DungeonBg       = Color(0xFF050B10)
val DungeonPanel    = Color(0xFF111820)
val DungeonBorder   = Color(0xFF243447)
val DungeonAccent   = Color(0xFF4CAF50)
val DungeonTextMain = Color(0xFFE0E6ED)
val DungeonTextSub  = Color(0xFF9BA8B5)

// All the screens in my app
enum class Screen {
    PARENT_LOGIN,
    PARENT_HOME,
    MAIN_MENU,
    LEVEL_SELECT,
    GAME,
    LEVEL_EDITOR,
    PARENT_STATS
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            //sets the custom made "dungeon theme"
            IndividualProject3Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background   // use dark theme BG
                ) {
                    KodableApp()
                }
            }
        }
    }
}
// ---------- MAIN APP ROOT ----------

@Composable
fun KodableApp() {

    val context = LocalContext.current

    // -------------------------
    // STATE for accounts
    // -------------------------
    var currentScreen by remember { mutableStateOf(Screen.PARENT_LOGIN) }

    var parentAccount by remember {
        mutableStateOf(loadParentAccount(context))
    }

    var children by remember {
        mutableStateOf(loadChildren(context))
    }

    var currentChild by remember {
        mutableStateOf<ChildAccount?>(null)
    }

    var currentKidName by remember { mutableStateOf<String?>(null) }

    // -------------------------
    // GAME STATE
    // -------------------------

    var selectedLevel by remember { mutableStateOf<Level?>(null) }

    var selectedGameMap by remember { mutableStateOf<GameMap?>(null) }

    // All levels (easy + hard)
    val allLevels = remember { createAllLevels() }

    // -------------------------
    // SCREEN ROUTING
    // -------------------------
    when (currentScreen) {

        Screen.PARENT_LOGIN -> {
            ParentLoginScreen(
                existingParent = parentAccount,
                onParentCreatedOrLoggedIn = { parent ->
                    parentAccount = parent
                    // reload children after reset
                    children = loadChildren(context)
                    currentChild = null
                    currentKidName = null
                    currentScreen = Screen.PARENT_HOME
                }
            )
        }

        //the parent home screen, this is where the main UI is
        //goes to all other screens
        Screen.PARENT_HOME -> {
            ParentHomeScreen(
                parent = parentAccount!!,
                children = children,
                currentChild = currentChild,
                onChildrenChanged = { updated ->
                    children = updated
                    saveChildren(context, updated)
                },
                onSelectChild = { child ->
                    currentChild = child
                    currentKidName = child.name
                },
                onPlayAsChild = {
                    if (currentChild != null) {
                        currentScreen = Screen.LEVEL_SELECT
                    }
                },
                onOpenEditor = {
                    currentScreen = Screen.LEVEL_EDITOR
                },
                onViewStats = {
                    currentScreen = Screen.PARENT_STATS
                },
                onLogout = {
                    currentChild = null
                    currentKidName = null
                    currentScreen = Screen.PARENT_LOGIN
                }
            )
        }

        Screen.MAIN_MENU -> {
            MainMenuScreen(
                currentChild = currentChild!!,
                onBackToParent = { currentScreen = Screen.PARENT_HOME },
                onSelectDifficulty = { difficulty ->
                    currentScreen = Screen.LEVEL_SELECT
                }
            )
        }

        //Select levels
        Screen.LEVEL_SELECT -> {
            LevelSelectScreen(
                levels = allLevels,
                onBack = { currentScreen = Screen.PARENT_HOME },
                onSelectGame = { level, gameMap ->
                    selectedLevel = level
                    selectedGameMap = gameMap
                    currentScreen = Screen.GAME
                }
            )
        }


        // go to the game screen for the level
        Screen.GAME -> {
            val level = selectedLevel!!
            val gameMap = selectedGameMap!!

            // 🔹 Build an ordered list of (Level, GameMap) only for the SAME difficulty
            val sameDifficultyPairs: List<Pair<Level, GameMap>> =
                allLevels
                    .filter { it.difficulty == level.difficulty }
                    .flatMap { lvl -> lvl.games.map { gm -> lvl to gm } }

            // 🔹 Find where we are in that list
            val currentIndex = sameDifficultyPairs.indexOfFirst { (lvl, gm) ->
                lvl.id == level.id && gm.id == gameMap.id
            }

            val isLastInDifficulty = currentIndex == sameDifficultyPairs.lastIndex && currentIndex != -1

            // 🔹 If not last, prepare the NEXT level callback
            val onNextLevel: (() -> Unit)? =
                if (!isLastInDifficulty && currentIndex != -1) {
                    {
                        val (nextLevel, nextGameMap) = sameDifficultyPairs[currentIndex + 1]
                        selectedLevel = nextLevel
                        selectedGameMap = nextGameMap
                        currentScreen = Screen.GAME
                    }
                } else {
                    null
                }

            GameScreen(
                level = level,
                gameMap = gameMap,
                currentKidName = currentKidName,
                onBack = { currentScreen = Screen.LEVEL_SELECT },
                onNextLevel = onNextLevel,
                isLastLevelInDifficulty = isLastInDifficulty
            )
        }


        //takes you to the level editor screen
        Screen.LEVEL_EDITOR -> {
            LevelEditorScreen(
                onBack = { currentScreen = Screen.PARENT_HOME }
            )
        }

        //takes you to the stats screen to show childs progress
        Screen.PARENT_STATS -> {
            ParentStatsScreen(
                children = children,
                onBack = { currentScreen = Screen.PARENT_HOME }
            )
        }
    }
}

// ---------- MAIN MENU SCREEN ----------
@Composable
fun MainMenuScreen(
    currentChild: ChildAccount,
    onBackToParent: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome, ${currentChild.name}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(24.dp))

            Text("Choose difficulty:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onSelectDifficulty(Difficulty.EASY) }) {
                    Text("Easy")
                }
                Button(onClick = { onSelectDifficulty(Difficulty.HARD) }) {
                    Text("Hard")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onBackToParent) {
                Text("Back")
            }
        }
    }
}