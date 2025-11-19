@file:OptIn(ExperimentalFoundationApi::class)
package com.example.individualproject3

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter

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
 */
// TODO: Added small change so Git can detect modifications


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KodableApp()
                }
            }
        }
    }
}

// All the screens in your app
enum class Screen {
    MAIN_MENU,
    LEVEL_SELECT,
    GAME,
    PARENT_DASHBOARD,
    LOGIN_KID,
    LOGIN_PARENT,

    LEVEL_EDITOR   ,
    EDITOR_TEST              // 👈 NEW
// NEW

}

data class Account(
    val username: String,
    val password: String,
    val isParent: Boolean,
    val parentUsername: String? = null   // only used for kid accounts
)

// ---------- MAIN APP ROOT ----------

@Composable
fun KodableApp() {
    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }

    // Normal game selection
    var selectedLevel by remember { mutableStateOf<Level?>(null) }
    var selectedGame by remember { mutableStateOf<GameMap?>(null) }

    // Runtime test level from the editor
    var editorTestLevel by remember { mutableStateOf<EditorTestLevel?>(null) }

    // Accounts + who is logged in
    val accounts = remember { mutableStateListOf<Account>() }
    var currentKidName by remember { mutableStateOf<String?>(null) }
    var currentParentName by remember { mutableStateOf<String?>(null) }

    // All built-in levels
    val allLevels = remember { createAllLevels() }

    when (currentScreen) {

        Screen.MAIN_MENU -> MainMenuScreen(
            onPlayAsKid = {
                // If kid not logged in yet, go to kid login first
                if (currentKidName == null) {
                    currentScreen = Screen.LOGIN_KID
                } else {
                    currentScreen = Screen.LEVEL_SELECT
                }
            },
            onParentDashboard = {
                // If parent not logged in yet, go to parent login first
                if (currentParentName == null) {
                    currentScreen = Screen.LOGIN_PARENT
                } else {
                    currentScreen = Screen.PARENT_DASHBOARD
                }
            },
            onOpenLevelEditor = {
                currentScreen = Screen.LEVEL_EDITOR
            }
        )

        Screen.LOGIN_KID -> LoginScreen(
            isParent = false,
            accounts = accounts,
            onBack = { currentScreen = Screen.MAIN_MENU },
            onLoggedIn = { username ->
                currentKidName = username
                currentScreen = Screen.LEVEL_SELECT
            }
        )

        Screen.LOGIN_PARENT -> LoginScreen(
            isParent = true,
            accounts = accounts,
            onBack = { currentScreen = Screen.MAIN_MENU },
            onLoggedIn = { username ->
                currentParentName = username
                currentScreen = Screen.PARENT_DASHBOARD
            }
        )

        Screen.LEVEL_SELECT -> LevelSelectScreen(
            levels = allLevels,
            onBack = { currentScreen = Screen.MAIN_MENU },
            onSelectGame = { level, game ->
                selectedLevel = level
                selectedGame = game
                currentScreen = Screen.GAME
            }
        )

        Screen.GAME -> {
            val level = selectedLevel
            val game = selectedGame
            if (level != null && game != null) {
                GameScreen(
                    level = level,
                    gameMap = game,
                    onBack = { currentScreen = Screen.LEVEL_SELECT }
                )
            } else {
                // If something went wrong, send them back to level select
                currentScreen = Screen.LEVEL_SELECT
            }
        }

        Screen.PARENT_DASHBOARD -> ParentDashboardScreen(
            onBack = { currentScreen = Screen.MAIN_MENU }
        )

        Screen.LEVEL_EDITOR -> LevelEditorScreen(
            onBack = { currentScreen = Screen.MAIN_MENU }
        )


        Screen.EDITOR_TEST -> {
            val runtime = editorTestLevel
            if (runtime != null) {
                EditorTestScreen(
                    testLevel = runtime,
                    onBack = { currentScreen = Screen.LEVEL_EDITOR }
                )
            } else {
                currentScreen = Screen.LEVEL_EDITOR
            }
        }
    }
}



// ---------- MAIN MENU SCREEN ----------

@Composable
fun MainMenuScreen(
    onPlayAsKid: () -> Unit,
    onParentDashboard: () -> Unit,
    onOpenLevelEditor: () -> Unit
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
            Text("Dungeon Coding Game", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            Button(onClick = onPlayAsKid) {
                Text("Play as Kid")
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onParentDashboard) {
                Text("Parent Dashboard")
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = onOpenLevelEditor) {
                Text("Level Editor")
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                // Optional: exit app here if you want
                // (context as? Activity)?.finish()
            }) {
                Text("Exit")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    levels: List<Level>,
    onBack: () -> Unit,
    onSelectGame: (Level, GameMap) -> Unit
) {
    val context = LocalContext.current

    // Track which game we're picking a custom for, if any
    var customPickerGameId by remember { mutableStateOf<String?>(null) }

    // Re-read applied mappings whenever we come back or change them
    var appliedMappings by remember { mutableStateOf(loadAppliedMappings(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Game") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            levels.forEach { level ->
                Text(
                    "${level.name} (${level.difficulty})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                level.games.forEach { game ->
                    val isCustomApplied = appliedMappings.containsKey(game.id)

                    // Play button: uses custom GameMap if one is applied
                    Button(
                        onClick = {
                            val mapping = loadAppliedMappings(context)
                            val customId = mapping[game.id]
                            if (customId != null) {
                                val allCustom = loadCustomLevels(context)
                                val custom = allCustom.find { it.id == customId }
                                if (custom != null) {
                                    val customMap = custom.toGameMap(idOverride = game.id)
                                    onSelectGame(level, customMap)
                                    return@Button
                                }
                            }
                            // Fallback: default map
                            onSelectGame(level, game)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCustomApplied) {
                            Text("Play game: ${game.id} (custom)")
                        } else {
                            Text("Play game: ${game.id}")
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Custom button: open picker dialog
                    OutlinedButton(
                        onClick = { customPickerGameId = game.id },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Custom")
                    }

                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // Custom picker dialog
        val targetGameId = customPickerGameId
        if (targetGameId != null) {
            CustomLevelPickerDialog(
                gameId = targetGameId,
                onDismiss = { customPickerGameId = null },
                onApplied = {
                    // Refresh mapping so UI updates (shows "(custom)")
                    appliedMappings = loadAppliedMappings(context)
                }
            )
        }
    }
}

@Composable
fun CustomLevelPickerDialog(
    gameId: String,
    onDismiss: () -> Unit,
    onApplied: () -> Unit
) {
    val context = LocalContext.current
    val customLevels = remember { loadCustomLevels(context) }

    var selected by remember { mutableStateOf<SavedCustomLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose custom level for $gameId") },
        text = {
            Column {
                if (customLevels.isEmpty()) {
                    Text("No custom levels saved yet.\nUse the Level Editor to create some.")
                } else {
                    Text("Tap a custom level to select it:", style = MaterialTheme.typography.bodyMedium)
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
                        text = "The last applied custom will stay on this slot\nuntil you change it or clear it.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Row {
                // Button to clear custom and go back to default
                TextButton(
                    onClick = {
                        clearCustomFromGame(context, gameId)
                        onApplied()
                        onDismiss()
                    }
                ) {
                    Text("Use Default")
                }

                Spacer(Modifier.width(8.dp))

                TextButton(
                    enabled = (selected != null),
                    onClick = {
                        val chosen = selected ?: return@TextButton
                        applyCustomToGame(context, gameId, chosen.id)
                        onApplied()
                        onDismiss()
                    }
                ) {
                    Text("Apply")
                }
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
fun LoginScreen(
    isParent: Boolean,
    accounts: MutableList<Account>,
    onBack: () -> Unit,
    onLoggedIn: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    // For kid registration: which parent owns this kid?
    var selectedParent by remember { mutableStateOf<String?>(null) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }


    val title = if (isParent) "Parent Login" else "Kid Login"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegisterMode) "Register New ${if (isParent) "Parent" else "Kid"}" else "Log In",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = ""
                },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // If we're registering a KID, show parent selection
            if (!isParent && isRegisterMode) {
                Text("Select Parent Account:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))

                Box {
                    Button(onClick = { parentDropdownExpanded = true }) {
                        Text(selectedParent ?: "Choose Parent")
                    }

                    DropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        val parentAccounts = accounts.filter { it.isParent }
                        if (parentAccounts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No parent accounts yet") },
                                onClick = { parentDropdownExpanded = false }
                            )
                        } else {
                            parentAccounts.forEach { parentAcc ->
                                DropdownMenuItem(
                                    text = { Text(parentAcc.username) },
                                    onClick = {
                                        selectedParent = parentAcc.username
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both username and password."
                        return@Button
                    }

                    if (isRegisterMode) {
                        if (isParent) {
                            // PARENT REGISTRATION
                            accounts.add(
                                Account(
                                    username = username,
                                    password = password,
                                    isParent = true
                                )
                            )
                            onLoggedIn(username)
                        } else {
                            // KID REGISTRATION → MUST CHOOSE A PARENT
                            if (selectedParent == null) {
                                errorMessage = "Please choose a parent account."
                                return@Button
                            }

                            accounts.add(
                                Account(
                                    username = username,
                                    password = password,
                                    isParent = false,
                                    parentUsername = selectedParent!!
                                )
                            )
                            onLoggedIn(username)
                        }
                    } else {
                        // LOGIN
                        val account = accounts.find {
                            it.username == username && it.isParent == isParent
                        }
                        if (account == null) {
                            errorMessage = "Account not found. Try registering."
                        } else if (account.password != password) {
                            errorMessage = "Incorrect password."
                        } else {
                            onLoggedIn(username)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Register" else "Log In")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = ""
                }
            ) {
                Text(
                    if (isRegisterMode)
                        "Already have an account? Log in"
                    else
                        "New here? Register"
                )
            }
        }
    }
}

// Two-tile-thick outer wall around the map
fun isUpperWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 0 || x == maxX || y == 0 || y == maxY)
}

fun isLowerWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 1 || x == maxX - 1 || y == 1 || y == maxY - 1)
}

// For movement checks later
fun isOuterWall(x: Int, y: Int, map: GameMap): Boolean =
    isUpperWallRing(x, y, map) || isLowerWallRing(x, y, map)

@Composable
fun DungeonGrid(
    gameMap: GameMap,
    heroPos: Pair<Int, Int>,
    heroFacing: HeroFacing,
    heroShake: Pair<Int, Int> = 0 to 0,
    heroSinkProgress: Float = 0f
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val tileSize: Dp = maxWidth / gameMap.width

        // --- Hero sprites --- //
        val heroPainter: Painter = when (heroFacing) {
            HeroFacing.UP    -> painterResource(R.drawable.up_sprite)
            HeroFacing.DOWN  -> painterResource(R.drawable.down_sprite)
            HeroFacing.LEFT  -> painterResource(R.drawable.left_sprite)
            HeroFacing.RIGHT -> painterResource(R.drawable.right_sprite)
        }

        val maxX = gameMap.width - 1
        val maxY = gameMap.height - 1

        // If we have a full tile grid from the editor, use it for 1:1 visuals.
        val tiles = gameMap.tileIds
        if (tiles != null) {
            // Match editor palette IDs -> painters
            val painterById: Map<String, Painter> = mapOf(
                "floor"      to painterResource(R.drawable.floor_tile),
                "inner_wall" to painterResource(R.drawable.inner_wall),
                "water"      to painterResource(R.drawable.water_tile),

                "left_upper"   to painterResource(R.drawable.left_side_upper_wall),
                "left_lower"   to painterResource(R.drawable.left_side_lower_wall),
                "right_upper"  to painterResource(R.drawable.right_side_upper_wall),
                "right_lower"  to painterResource(R.drawable.right_side_lower_wall),

                "top_upper"    to painterResource(R.drawable.top_side_upper_wall),
                "top_lower"    to painterResource(R.drawable.top_side_lower_wall),
                "bottom_upper" to painterResource(R.drawable.bottom_side_upper_wall),
                "bottom_lower" to painterResource(R.drawable.bottom_side_lower_wall),

                "tl_lower"     to painterResource(R.drawable.top_left_corner_lower_wall),
                "tr_lower"     to painterResource(R.drawable.top_right_side_lower_wall),
                "bl_lower"     to painterResource(R.drawable.bottom_left_side_lower_wall),
                "br_lower"     to painterResource(R.drawable.bottom_right_side_lower_wall),

                "tl_upper"     to painterResource(R.drawable.top_left_corner_upper_wall),
                "tr_upper"     to painterResource(R.drawable.top_right_side_upper_wall),
                "bl_upper"     to painterResource(R.drawable.bottom_left_side_upper_wall),
                "br_upper"     to painterResource(R.drawable.bottom_right_side_upper_wall),

                "outer_tl"     to painterResource(R.drawable.outer_top_left_corner),
                "outer_tr"     to painterResource(R.drawable.outer_top_right_corner),
                "outer_bl"     to painterResource(R.drawable.outer_bottom_left_corner),
                "outer_br"     to painterResource(R.drawable.outer_bottom_right_corner),

                "inner_tl"     to painterResource(R.drawable.inner_top_left_corner),
                "inner_tr"     to painterResource(R.drawable.inner_top_right_corner),
                "inner_bl"     to painterResource(R.drawable.inner_bottom_left_corner),
                "inner_br"     to painterResource(R.drawable.inner_bottom_right_corner)
            )

            val doorGoal = painterResource(R.drawable.goal)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (y in 0 until gameMap.height) {
                    Row {
                        for (x in 0 until gameMap.width) {
                            val isHero = (heroPos.first == x && heroPos.second == y)
                            val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                            val id = tiles[y][x]
                            val basePainter: Painter? = painterById[id]

                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (basePainter != null) {
                                    Image(
                                        painter = basePainter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds
                                    )
                                } else {
                                    // Unknown / "empty" -> pure black
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                    )
                                }

                                // Goal overlay
                                if (isGoal) {
                                    Image(
                                        painter = doorGoal,
                                        contentDescription = "Goal",
                                        modifier = Modifier.fillMaxSize(0.85f),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Hero overlay (with shake + sink)
                                if (isHero) {
                                    Image(
                                        painter = heroPainter,
                                        contentDescription = "Hero",
                                        modifier = Modifier
                                            .offset(
                                                x = heroShake.first.dp,
                                                y = heroShake.second.dp
                                            )
                                            .graphicsLayer(
                                                scaleX = 1f - heroSinkProgress * 0.6f,
                                                scaleY = 1f - heroSinkProgress * 0.6f,
                                                alpha  = 1f - heroSinkProgress
                                            )
                                            .fillMaxSize(0.8f),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ---------- FALLBACK: old arena/room visuals for built-in levels ----------
            val floorTile   = painterResource(R.drawable.floor_tile)
            val waterTile   = painterResource(R.drawable.water_tile)
            val innerWall   = painterResource(R.drawable.inner_wall)

            // Outer wall tiles (upper ring)
            val topSideUpper            = painterResource(R.drawable.top_side_upper_wall)
            val bottomSideUpper         = painterResource(R.drawable.bottom_side_upper_wall)
            val leftSideUpper           = painterResource(R.drawable.left_side_upper_wall)
            val rightSideUpper          = painterResource(R.drawable.right_side_upper_wall)
            val topLeftUpperCorner      = painterResource(R.drawable.top_left_corner_upper_wall)
            val topRightUpperCorner     = painterResource(R.drawable.top_right_side_upper_wall)
            val bottomLeftUpperCorner   = painterResource(R.drawable.bottom_left_side_upper_wall)
            val bottomRightUpperCorner  = painterResource(R.drawable.bottom_right_side_upper_wall)

            // Outer wall tiles (lower ring)
            val topSideLower            = painterResource(R.drawable.top_side_lower_wall)
            val bottomSideLower         = painterResource(R.drawable.bottom_side_lower_wall)
            val leftSideLower           = painterResource(R.drawable.left_side_lower_wall)
            val rightSideLower          = painterResource(R.drawable.right_side_lower_wall)
            val topLeftLowerCorner      = painterResource(R.drawable.top_left_corner_lower_wall)
            val topRightLowerCorner     = painterResource(R.drawable.top_right_side_lower_wall)
            val bottomLeftLowerCorner   = painterResource(R.drawable.bottom_left_side_lower_wall)
            val bottomRightLowerCorner  = painterResource(R.drawable.bottom_right_side_lower_wall)

            val doorGoal = painterResource(R.drawable.goal)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (y in 0 until gameMap.height) {
                    Row {
                        for (x in 0 until gameMap.width) {
                            val isHero = (heroPos.first == x && heroPos.second == y)
                            val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                            val basePainter: Painter = when {
                                // Outer wall: upper ring
                                isUpperWallRing(x, y, gameMap) -> {
                                    when {
                                        x == 0 && y == 0 -> topLeftUpperCorner
                                        x == maxX && y == 0 -> topRightUpperCorner
                                        x == 0 && y == maxY -> bottomLeftUpperCorner
                                        x == maxX && y == maxY -> bottomRightUpperCorner
                                        y == 0 -> topSideUpper
                                        y == maxY -> bottomSideUpper
                                        x == 0 -> leftSideUpper
                                        x == maxX -> rightSideUpper
                                        else -> topSideUpper
                                    }
                                }

                                // Outer wall: lower ring
                                isLowerWallRing(x, y, gameMap) -> {
                                    when {
                                        x == 1 && y == 1 -> topLeftLowerCorner
                                        x == maxX - 1 && y == 1 -> topRightLowerCorner
                                        x == 1 && y == maxY - 1 -> bottomLeftLowerCorner
                                        x == maxX - 1 && y == maxY - 1 -> bottomRightLowerCorner
                                        y == 1 -> topSideLower
                                        y == maxY - 1 -> bottomSideLower
                                        x == 1 -> leftSideLower
                                        x == maxX - 1 -> rightSideLower
                                        else -> topSideLower
                                    }
                                }

                                // Water & inner walls
                                gameMap.waterTiles.contains(x to y) -> waterTile
                                gameMap.walls.contains(x to y) -> innerWall

                                // Goal and floor
                                isGoal -> doorGoal
                                else -> floorTile
                            }

                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = basePainter,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )

                                if (isHero) {
                                    Image(
                                        painter = heroPainter,
                                        contentDescription = "Hero",
                                        modifier = Modifier
                                            .offset(
                                                x = heroShake.first.dp,
                                                y = heroShake.second.dp
                                            )
                                            .graphicsLayer(
                                                scaleX = 1f - heroSinkProgress * 0.6f,
                                                scaleY = 1f - heroSinkProgress * 0.6f,
                                                alpha  = 1f - heroSinkProgress
                                            )
                                            .fillMaxSize(0.8f),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun commandLabel(cmd: Command): String =
    when (cmd) {
        Command.MOVE_UP -> "↑"
        Command.MOVE_DOWN -> "↓"
        Command.MOVE_LEFT -> "←"
        Command.MOVE_RIGHT -> "→"
    }

fun stepPosition(
    currentPos: Pair<Int, Int>,
    cmd: Command
): Pair<Int, Int> {
    val (x, y) = currentPos
    return when (cmd) {
        Command.MOVE_UP -> x to (y - 1)
        Command.MOVE_DOWN -> x to (y + 1)
        Command.MOVE_LEFT -> (x - 1) to y
        Command.MOVE_RIGHT -> (x + 1) to y
    }
}

@Composable
fun chooseWallSprite(
    x: Int,
    y: Int,
    map: GameMap,
    horizontal: Painter,
    vertical: Painter,
    corner: Painter
): Painter {

    val maxX = map.width - 1
    val maxY = map.height - 1

    return when {
        // Corners
        x == 0 && y == 0 -> corner           // top-left
        x == maxX && y == 0 -> corner        // top-right
        x == 0 && y == maxY -> corner        // bottom-left
        x == maxX && y == maxY -> corner     // bottom-right

        // Top or bottom row → horizontal wall
        y == 0 || y == maxY -> horizontal

        // Left or right column → vertical wall
        x == 0 || x == maxX -> vertical

        // Otherwise fall back to your map-defined interior walls
        else -> horizontal
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var totalAttempts by remember { mutableStateOf(0) }
    var resultStats by remember { mutableStateOf<List<ResultStat>>(emptyList()) }

    val scrollState = rememberScrollState()

    // Load stats once when screen opens
    LaunchedEffect(Unit) {
        val entries = readProgressEntries(context)
        totalAttempts = entries.size

        val counts = entries.groupingBy { it.resultCode }.eachCount()

        // We keep a consistent order + labels
        val codesInOrder = listOf("SUCCESS", "HIT_WALL", "OUT_OF_BOUNDS", "NO_GOAL", "UNKNOWN")

        resultStats = codesInOrder.map { code ->
            val count = counts[code] ?: 0
            ResultStat(
                code = code,
                label = when (code) {
                    "SUCCESS" -> "Success"
                    "HIT_WALL" -> "Hit Wall"
                    "OUT_OF_BOUNDS" -> "Out of Bounds"
                    "NO_GOAL" -> "Finished w/o Goal"
                    else -> "Other / Unknown"
                },
                count = count
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Child Progress Summary",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            if (totalAttempts == 0) {
                Text("No attempts logged yet. Have your child play a level first.")
            } else {
                Text("Total Attempts: $totalAttempts")
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Attempts by Outcome:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                val maxCount = resultStats.maxOfOrNull { it.count } ?: 0

                resultStats.forEach { stat ->
                    if (stat.count > 0) {
                        ResultBarRow(
                            label = stat.label,
                            count = stat.count,
                            maxCount = maxCount
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Note: Data is based on runs of the dungeon game.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ResultBarRow(
    label: String,
    count: Int,
    maxCount: Int
) {
    // Guard: avoid division by zero
    val fraction = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f

    Column {
        Text("$label: $count")
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

data class ProgressEntry(
    val levelId: String,
    val gameId: String,
    val resultCode: String,
    val commandsCount: Int
)

data class ResultStat(
    val code: String,
    val label: String,
    val count: Int
)

fun readProgressEntries(context: Context): List<ProgressEntry> {
    val fileName = "progress_log.csv"

    return try {
        val text = context.openFileInput(fileName).bufferedReader().use { it.readText() }

        text.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(',')
                // Expected: timestamp, levelId, gameId, resultCode, commandsCount
                if (parts.size >= 5) {
                    val levelId = parts[1]
                    val gameId = parts[2]
                    val resultCode = parts[3]
                    val commandsCount = parts[4].toIntOrNull() ?: 0
                    ProgressEntry(levelId, gameId, resultCode, commandsCount)
                } else {
                    null
                }
            }
            .toList()
    } catch (e: Exception) {
        // If file doesn't exist yet or read fails, just return empty list
        emptyList()
    }
}

// ---------- Demo data so the app actually runs ----------

fun createAllLevels(): List<Level> {

    // -------- EASY 1 (your baked custom level) --------
    val easy1Tiles = listOf(
        listOf("empty", "empty", "empty", "empty", "empty", "tl_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty")
    )

    // added change here
    val easyGame1 = gameMapFromTileIds(
        id = "easy1_default",
        startX = 1,
        startY = 6,
        goalX = 6,
        goalY = 1,
        tileIds = easy1Tiles
    )

    // -------- EASY LEVEL COLLECTION --------
    val easyLevel = Level(
        id = "easy_level",
        name = "Easy Dungeons",
        difficulty = Difficulty.EASY,
        games = listOf(easyGame1)
    )

    // -------- HARD LEVEL (placeholder for now) --------
    val hardLevel = Level(
        id = "hard_level",
        name = "Hard Dungeons",
        difficulty = Difficulty.HARD,
        games = emptyList()   // later you can add baked hardGame1, hardGame2, etc.
    )

    return listOf(easyLevel, hardLevel)
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Level,
    gameMap: GameMap,
    onBack: () -> Unit
) {
    // Hero starts at the map start location
    var heroPos by remember {
        mutableStateOf(gameMap.startX to gameMap.startY)
    }

    // NEW: track facing direction for sprite orientation
    var heroFacing by remember {
        mutableStateOf(HeroFacing.DOWN)
    }

    // Small offset for bonk shake (x,y in dp)
    var heroShake by remember {
        mutableStateOf(0 to 0)
    }

    // Sinking animation amount (0f to 1f)
    var heroSinkProgress by remember { mutableStateOf(0f) }

    // Commands the child drags into the program
    val program = remember { mutableStateListOf<Command>() }

    // Disable run button while program executes
    var isRunning by remember { mutableStateOf(false) }

    // Message shown under the grid (hit wall, success, etc.)
    var statusMessage by remember { mutableStateOf("") }

    // Result dialog state
    var showResultDialog by remember { mutableStateOf(false) }
    var resultTitle by remember { mutableStateOf("") }
    var resultBody by remember { mutableStateOf("") }
    var isSuccessResult by remember { mutableStateOf(false) }
    var runResultCode by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Logging + sound
    val context = LocalContext.current
    val logger = remember { ProgressLogger(context) }
    val soundManager = remember { SoundManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game: ${gameMap.id}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF071017)) // dungeon dark background
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Level: ${level.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            DungeonGrid(
                gameMap = gameMap,
                heroPos = heroPos,
                heroFacing = heroFacing,
                heroShake = heroShake,
                heroSinkProgress = heroSinkProgress
            )

            Spacer(Modifier.height(24.dp))

            // -------------------------------
            // COMMAND PALETTE
            // -------------------------------
            Text("Commands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Drag arrows below into the drop area.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Command.values().forEach { cmd ->
                    val label = commandLabel(cmd)

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, Color.Black)
                            .background(Color.White)
                            .dragAndDropSource(
                                transferData = {
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            "command",
                                            when (cmd) {
                                                Command.MOVE_UP    -> "UP"
                                                Command.MOVE_DOWN  -> "DOWN"
                                                Command.MOVE_LEFT  -> "LEFT"
                                                Command.MOVE_RIGHT -> "RIGHT"
                                            }
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Program:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Drag-and-drop TARGET area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(2.dp, Color.DarkGray)
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            event.mimeTypes()
                                .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        },
                        target = remember {
                            object : DragAndDropTarget {
                                override fun onDrop(event: androidx.compose.ui.draganddrop.DragAndDropEvent): Boolean {
                                    val clipData = event.toAndroidDragEvent().clipData ?: return false
                                    if (clipData.itemCount < 1) return false
                                    val text = clipData.getItemAt(0).text?.toString() ?: return false

                                    val cmd = when (text) {
                                        "UP" -> Command.MOVE_UP
                                        "DOWN" -> Command.MOVE_DOWN
                                        "LEFT" -> Command.MOVE_LEFT
                                        "RIGHT" -> Command.MOVE_RIGHT
                                        else -> null
                                    }

                                    if (cmd != null && !isRunning) {
                                        program.add(cmd)
                                        return true
                                    }
                                    return false
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (program.isEmpty())
                        "Drag commands here"
                    else
                        "Drop more commands to extend program",
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            if (program.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    program.forEachIndexed { index, cmd ->
                        Surface {
                            Text(
                                text = "${index + 1}: ${commandLabel(cmd)}",
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage)
                Spacer(Modifier.height(8.dp))
            }

            // -----------------------
            // RUN / CLEAR / RESET
            //-----------------------
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // RUN BUTTON
                Button(
                    enabled = !isRunning && program.isNotEmpty(),
                    onClick = {
                        statusMessage = ""
                        showResultDialog = false
                        isSuccessResult = false
                        runResultCode = ""


                        scope.launch {
                            isRunning = true
                            heroPos = gameMap.startX to gameMap.startY
                            heroFacing = HeroFacing.DOWN
                            heroSinkProgress = 0f

                            var touchedWater = false
                            var reachedGoal = false

                            for (cmd in program) {

                                // 1) Update facing based on command
                                heroFacing = when (cmd) {
                                    Command.MOVE_UP    -> HeroFacing.UP
                                    Command.MOVE_DOWN  -> HeroFacing.DOWN
                                    Command.MOVE_LEFT  -> HeroFacing.LEFT
                                    Command.MOVE_RIGHT -> HeroFacing.RIGHT
                                }

                                // Direction vector for this command
                                val (dx, dy) = when (cmd) {
                                    Command.MOVE_UP    -> 0 to -1
                                    Command.MOVE_DOWN  -> 0 to 1
                                    Command.MOVE_LEFT  -> -1 to 0
                                    Command.MOVE_RIGHT -> 1 to 0
                                }

                                var movedThisCommand = false

                                // Slide in this direction until blocked
                                slideLoop@ while (true) {
                                    val nextX = heroPos.first + dx
                                    val nextY = heroPos.second + dy

                                    // --- OUT OF BOUNDS CHECK ---
                                    if (nextX !in 0 until gameMap.width || nextY !in 0 until gameMap.height) {
                                        if (!movedThisCommand) {
                                            // Immediately went off map -> fail
                                            statusMessage = "Went out of bounds!"
                                            resultTitle = "Out of Bounds"
                                            resultBody = "Your program moved Link off the map."
                                            isSuccessResult = false
                                            runResultCode = "OUT_OF_BOUNDS"
                                            soundManager.playFailure()
                                            showResultDialog = true
                                            isRunning = false
                                            break
                                        } else {
                                            // We were sliding and hit edge; just stop on last valid tile
                                            break@slideLoop
                                        }
                                    }

                                    val isOuter = isOuterWall(nextX, nextY, gameMap)
                                    val isInner = gameMap.walls.contains(nextX to nextY)
                                    val isWater = gameMap.waterTiles.contains(nextX to nextY)

                                    // --- WATER: fall in & lose ---
                                    if (isWater) {
                                        heroPos = nextX to nextY
                                        soundManager.playSplash()

                                        // sink animation
                                        for (i in 0..10) {
                                            heroSinkProgress = i / 10f
                                            delay(50L)
                                        }

                                        statusMessage = "Link fell into the water!"
                                        resultTitle = "Splash!"
                                        resultBody = "Link fell into the water. Try a different program."
                                        isSuccessResult = false
                                        runResultCode = "WATER"
                                        showResultDialog = true
                                        isRunning = false
                                        break
                                    }

                                    // --- WALL: stop sliding (and possibly fail) ---
                                    if (isOuter || isInner) {
                                        if (!movedThisCommand) {
                                            // Already against wall when this command started → bonk AND fail
                                            soundManager.playBonk()
                                            heroShake = when (cmd) {
                                                Command.MOVE_UP    -> 0 to -4
                                                Command.MOVE_DOWN  -> 0 to 4
                                                Command.MOVE_LEFT  -> -4 to 0
                                                Command.MOVE_RIGHT -> 4 to 0
                                            }
                                            delay(80L)
                                            heroShake = 0 to 0

                                            statusMessage = "Link bumped into a wall!"
                                            resultTitle = "Bumped the Wall"
                                            resultBody = "Your program tried to move Link into a wall. Try a different set of commands."
                                            isSuccessResult = false
                                            runResultCode = "HIT_WALL"
                                            showResultDialog = true
                                            isRunning = false

                                            // stop this slide loop; the outer loop will see showResultDialog and break too
                                            break
                                        } else {
                                            // We slid at least one tile, then hit a wall → just stop sliding
                                            break
                                        }
                                    }


                                    // --- SAFE TILE: move one step and keep sliding ---
                                    heroPos = nextX to nextY
                                    movedThisCommand = true

                                    // Check goal after each step of the slide
                                    if (heroPos.first == gameMap.goalX && heroPos.second == gameMap.goalY) {
                                        statusMessage = "Reached the goal!"
                                        resultTitle = "Great Job!"
                                        resultBody = "You guided Link successfully."
                                        isSuccessResult = true
                                        runResultCode = "SUCCESS"
                                        soundManager.playSuccess()
                                        showResultDialog = true
                                        isRunning = false
                                        break
                                    }

                                    // Show intermediate motion
                                    delay(200L)
                                }

                                // After finishing this command’s slide, if a dialog is showing we already ended
                                if (showResultDialog) break
                            }

// After the for-loop over commands:
                            if (!showResultDialog) {
                                // All commands ran, no water, no out-of-bounds, no goal
                                statusMessage = "Program finished, but Link did not reach the goal."
                                resultTitle = "Try Again"
                                resultBody = "All commands ran, but Link never reached the goal."
                                isSuccessResult = false
                                runResultCode = "NO_GOAL"
                                soundManager.playFailure()
                                showResultDialog = true
                            }

                            // 8) If we didn’t already stop because of goal / water / out-of-bounds:
                            if (!showResultDialog) {
                                when {
                                    reachedGoal -> {
                                        // (Safety case; we already handle goal above)
                                        statusMessage = "Reached the goal!"
                                        resultTitle = "Great Job!"
                                        resultBody = "You guided Link successfully."
                                        isSuccessResult = true
                                        runResultCode = "SUCCESS"
                                        soundManager.playSuccess()
                                    }
                                    !touchedWater -> {
                                        // All commands finished, no water, no goal -> end-of-run failure
                                        statusMessage = "Program finished, but Link did not reach the goal."
                                        resultTitle = "Try Again"
                                        resultBody = "All commands ran, but Link never reached the goal."
                                        isSuccessResult = false
                                        runResultCode = "NO_GOAL"
                                        soundManager.playFailure()
                                    }
                                }
                                showResultDialog = true
                            }

                            // 9) Log this attempt
                            logger.logAttempt(
                                levelId = level.id,
                                gameId = gameMap.id,
                                resultCode = if (runResultCode.isNotEmpty()) runResultCode else "UNKNOWN",
                                commandsCount = program.size
                            )

                            isRunning = false
                        }
                    }
                ) {
                    Text("Run")
                }

                // CLEAR PROGRAM
                Button(
                    enabled = !isRunning,
                    onClick = {
                        program.clear()
                        statusMessage = ""
                        showResultDialog = false
                    }
                ) {
                    Text("Clear")
                }

                // RESET HERO
                Button(
                    enabled = !isRunning,
                    onClick = {
                        heroPos = gameMap.startX to gameMap.startY
                        heroFacing = HeroFacing.DOWN
                        heroShake = 0 to 0              // NEW
                        statusMessage = ""
                        heroSinkProgress = 0f
                        showResultDialog = false
                        isSuccessResult = false
                        runResultCode = ""
                    }
                ) {
                    Text("Reset")
                }
            }

            Spacer(Modifier.height(16.dp))

            // RESULT DIALOG (success or fail)
            if (showResultDialog) {
                AlertDialog(
                    onDismissRequest = { /* forced */ },
                    title = { Text(resultTitle) },
                    text = { Text(resultBody) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                heroPos = gameMap.startX to gameMap.startY
                                heroFacing = HeroFacing.DOWN
                                heroSinkProgress = 0f
                                statusMessage = ""
                                showResultDialog = false
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                )
            }
        }
    }
}
