package com.example.individualproject3

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Author: Carlos Gonzalez with the assistance of AI(Chat Gpt)
 * Ram Num: R02190266
 * description: this class is what forms the actual UI for the
 * game screen. not including the grid. this class includes the buttons
 * run, clear and reset. it also includes the logic and visuals for all
 * the command arrows and how they will move the character in the level
 * (straght glide until water or a wall is hit). This class also most
 * importantly includes the game logic, such as falling into water when
 * touched or bumping into a wall if the user after stopping, chooses to go
 * into the direction of the wall. it also keeps track of the way the
 * hero is facing when moving, making movement more visually pleasing.
 */

// ---- FUNCTION / GEM SUPPORT TYPES ----

// The 4 gem colors, in the order they will cycle
enum class GemColor {
    RED, BLUE, GREEN, PURPLE
}

// A saved user function: which gem color, its commands, and loop count
data class UserFunction(
    val id: Int,                 // unique id per function (0,1,2,3,...)
    val color: GemColor,
    val commands: List<Command>, // only MOVE_* commands
    val repeatCount: Int
)


//--------The game screen-------------//
//This is where the game screen is formed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Level,
    gameMap: GameMap,
    currentKidName: String?,   // 🔹 must be here
    onBack: () -> Unit
) {
    // Hero starts at the map start location
    var heroPos by remember {
        mutableStateOf(gameMap.startX to gameMap.startY)
    }

    // If this map came from the editor, it has a tile layout.
    // In that case we should NOT use the auto outer-wall rings.
    val hasTileLayout = gameMap.tileIds != null

    // IF tiles placed by the child during play (x,y coordinates)
    val ifTiles = remember { mutableStateListOf<Pair<Int, Int>>() }

    // Max number of IF blocks allowed on this map.
    // TODO: customize per level using gameMap.id if you want.
    // Max number of IF blocks allowed on this map.
    // Customize per level using gameMap.id
    val maxIfBlocks = remember(gameMap.id) {
        when (gameMap.id) {
            // 🔽 EXAMPLES — replace these IDs with your real map IDs

            // No IF blocks allowed on this map → IF UI will be hidden
            "easy level 1" -> 0

            // Allow exactly 1 IF block on this map
            "easy level 2" -> 1

            // Allow 2 IF blocks on this map
            "Level3" -> 2

            // Default for any other map (if not matched above)
            else -> 3
        }
    }

    // How many IF blocks are still available to place
    var remainingIfBlocks by remember(gameMap.id) { mutableStateOf(maxIfBlocks) }

    // -----------------------------
    // MONSTERS (from tileIds)
    // -----------------------------

    // Initial monster positions read from tileIds ("monster")
    val initialMonsterTiles = remember(gameMap.id) {
        val mons = mutableSetOf<Pair<Int, Int>>()
        val tiles = gameMap.tileIds
        if (tiles != null) {
            for (y in tiles.indices) {
                for (x in tiles[y].indices) {
                    if (tiles[y][x] == "monster") {   // 🔹 tile id used by the editor
                        mons.add(x to y)
                    }
                }
            }
        }
        mons
    }

    // Live monsters for the current run (can shrink when you kill them)
    val monsterTiles = remember(gameMap.id) {
        mutableStateListOf<Pair<Int, Int>>().apply {
            addAll(initialMonsterTiles)
        }
    }

    // Are we currently standing on a monster tile?
    var standingOnMonster by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }


    // -----------------------------
    // PITS + BUTTONS (from tileIds)
    // -----------------------------

    // All pit tiles for this map (both top+bottom) discovered from tileIds
    val pitTiles = remember(gameMap.id) {
        val pits = mutableSetOf<Pair<Int, Int>>()
        val tiles = gameMap.tileIds
        if (tiles != null) {
            for (y in tiles.indices) {
                for (x in tiles[y].indices) {
                    when (tiles[y][x]) {
                        "pit_top", "pit_bottom" -> pits.add(x to y)
                    }
                }
            }
        }
        pits
    }

    // All button tiles (unpressed/pressed) discovered from tileIds
    val buttonTiles = remember(gameMap.id) {
        val btns = mutableSetOf<Pair<Int, Int>>()
        val tiles = gameMap.tileIds
        if (tiles != null) {
            for (y in tiles.indices) {
                for (x in tiles[y].indices) {
                    when (tiles[y][x]) {
                        "button_unpressed", "button_pressed" -> btns.add(x to y)
                    }
                }
            }
        }
        btns
    }

    // Whether the button has been pressed in this run
    //var buttonPressed by remember(gameMap.id) { mutableStateOf(false) }

    // Has the puzzle button been pressed in this run?
    var buttonPressed by remember { mutableStateOf(false) }


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

    // NEW: sword-swing animation (0f to 1f)
    var heroAttackProgress by remember { mutableStateOf(0f) }

    // Remember the last movement direction (dx, dy) so we can continue sliding after attacks
    var lastMoveDirection by remember { mutableStateOf<Pair<Int, Int>?>(null) }

// NEW: monster poof animation (position + 0f..1f)
    var monsterPoofPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var monsterPoofProgress by remember { mutableStateOf(0f) }

    // Commands the child drags into the program
    val program = remember { mutableStateListOf<Command>() }

    // ---------------------------
    // PROGRAM SLOTS (Kodable-style)
    // ---------------------------

    // Max number of command slots allowed on this map
    // (Customize per level using gameMap.id, just like IF blocks / functions.)
    val maxCommandSlots = remember(gameMap.id) {
        when (gameMap.id) {
            "easy level 1" -> 3
            // EXAMPLES – change these IDs to your actual map IDs:
            // "easy level 1" -> 4   // only 4 moves allowed
            // "easy level 2" -> 6
            else -> 8               // default max: 8 slots
        }
    }

    // The fixed slots for commands. null = empty slot.
    val commandSlots = remember(gameMap.id) {
        mutableStateListOf<Command?>().apply {
            repeat(maxCommandSlots) { add(null) }
        }
    }

    // Parallel list storing which function each FUNCTION_1 slot refers to
    val commandSlotFunctionRefs = remember(gameMap.id) {
        mutableStateListOf<UserFunction?>().apply {
            repeat(maxCommandSlots) { add(null) }
        }
    }


    // ---------------------------
    // FUNCTION MAKER STATE
    // ---------------------------

    // Steps inside the user-defined function (up to 4 arrows)
    val functionCommands = remember { mutableStateListOf<Command>() }

    // Maximum number of command slots *inside one function*.
    // You can customize per map just like maxIfBlocks / maxFunctions.
    val maxFunctionSlots = remember(gameMap.id) {
        when (gameMap.id) {
            // Example: early levels with tiny functions:
            //"easy level 2" -> 2
            //"easy level 3" -> 3

            else -> 4  // default: 4 slots
        }
    }

    // Fixed slots for building the function (null = empty slot)
    val functionSlots = remember(gameMap.id) {
        mutableStateListOf<Command?>().apply {
            repeat(maxFunctionSlots) { add(null) }
        }
    }


    // How many times to loop the function when it is called
    var functionRepeatCount by remember { mutableStateOf(1) }

    // Whether the function has been "confirmed" and can be used
    var functionReady by remember { mutableStateOf(false) }

    // Whether the function maker mini panel is visible
    var showFunctionMaker by remember { mutableStateOf(false) }

    // All functions the kid has created (up to 4 total for now)
    val userFunctions = remember { mutableStateListOf<UserFunction>() }

    // Which gem color to use for the next new function (index into colorOrder)
    var nextGemColorIndex by remember { mutableStateOf(0) }

    // Program now needs to remember which function each FUNCTION_1 call refers to.
    // We'll keep a parallel list aligned with 'program':
    // - for normal arrow commands -> null
    // - for function gem calls    -> the UserFunction it uses
    val programFunctionRefs = remember { mutableStateListOf<UserFunction?>() }

    // Which functions still have an available gem to drag (single-use gems)
    val unusedFunctionIds = remember { mutableStateListOf<Int>() }

    // 🔹 Max number of functions allowed on this map (0 means no functions allowed)
    val maxFunctions = remember(gameMap.id) {
        when (gameMap.id) {
            // EXAMPLES – change these IDs to your actual level IDs

            // No functions on this map → Function Maker is hidden
            "easy level 1" -> 0

            // Allow exactly 1 function on this map
            "easy level 2" -> 1

            // Allow 2 functions on this map
            "easy level 3"       -> 0

            // Default for any other map
            else -> 4
        }
    }

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
                heroSinkProgress = heroSinkProgress,
                ifTiles = ifTiles.toSet(),
                        onDropIfTile = { x, y ->
                    if (!isRunning && maxIfBlocks > 0) {
                        val isWall = gameMap.walls.contains(x to y) ||
                                (!hasTileLayout && isOuterWall(x, y, gameMap))
                        val isWater = gameMap.waterTiles.contains(x to y)

                        if (!isWall && !isWater) {
                            val alreadyHasIf = ifTiles.any { it.first == x && it.second == y }

                            if (alreadyHasIf) {
                                ifTiles.removeAll { it.first == x && it.second == y }
                                remainingIfBlocks = (remainingIfBlocks + 1).coerceAtMost(maxIfBlocks)
                            } else {
                                if (remainingIfBlocks > 0) {
                                    ifTiles.add(x to y)
                                    remainingIfBlocks--
                                }
                            }
                        }
                    }
                },
                buttonPressed = buttonPressed,
                monsterTiles = monsterTiles.toSet(),
                monsterPoofPos = monsterPoofPos,              // 🔹 NEW
                monsterPoofProgress = monsterPoofProgress,
                heroAttackProgress = heroAttackProgress,
                )

            Spacer(Modifier.height(24.dp))

            // -------------------------------
            // IF BLOCK PALETTE
            // -------------------------------
            if (maxIfBlocks > 0) {
                Text("Special Blocks", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Drag an IF block onto any floor tile.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))

                // Show how many IF blocks remain
                Text(
                    text = "IF blocks left: $remainingIfBlocks / $maxIfBlocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1A242E))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .dragAndDropSource(
                                transferData = {
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            "tile",
                                            "IF_TILE"   // marker the grid looks for
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.if_tile),
                            contentDescription = "IF block",
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Optional: clear all IF tiles at once
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = ifTiles.isNotEmpty(),
                        onClick = {
                            ifTiles.clear()
                            remainingIfBlocks = maxIfBlocks
                        }
                    ) {
                        Text("Clear IF Blocks")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }


            // -------------------------------
            // COMMAND PALETTE
            // -------------------------------
            Text("Commands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Drag arrows below into the Slots.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

            // one base painter for all arrows
            val commandArrowPainter = painterResource(R.drawable.command_arrow)
            val attackPainter = painterResource(R.drawable.sword_icon)


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Only show the 4 movement commands, not FUNCTION_1
                val directionalCommands = listOf(
                    Command.MOVE_UP,
                    Command.MOVE_DOWN,
                    Command.MOVE_LEFT,
                    Command.MOVE_RIGHT
                )

                directionalCommands.forEach { cmd ->

                    val rotation = when (cmd) {
                        Command.MOVE_UP    ->  90f
                        Command.MOVE_DOWN  -> -90f
                        Command.MOVE_LEFT  ->   0f
                        Command.MOVE_RIGHT -> 180f
                        Command.FUNCTION_1 -> 0f
                        Command.ATTACK     -> 0f   // NEW (won’t rotate attack)
                        else               -> 0f
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1A242E)) // dark panel
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary, // your green accent
                                shape = RoundedCornerShape(10.dp)
                            )
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
                                                Command.FUNCTION_1 -> "FUNC1"
                                                Command.ATTACK     -> "ATTACK"    // NEW
                                                else               -> ""
                                            }
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = commandArrowPainter,
                            contentDescription = cmd.name,
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer(rotationZ = rotation),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 🔹 ATTACK command button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF1A242E))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .dragAndDropSource(
                        transferData = {
                            DragAndDropTransferData(
                                ClipData.newPlainText("command", "ATTACK")
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = attackPainter,
                    contentDescription = "Attack",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // -------------------------------
            // FUNCTION MAKER PANEL
            // -------------------------------
            if (maxFunctions > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Function Maker", style = MaterialTheme.typography.titleSmall)
                    Button(
                        onClick = { showFunctionMaker = !showFunctionMaker },
                        enabled = !isRunning
                    ) {
                        Text(if (showFunctionMaker) "Hide" else "Show")
                    }
                }

                if (showFunctionMaker) {
                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF101820),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Drag up to 4 arrow commands here to define your function.",
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Drop area for function steps
                            Text(
                                text = "Drag commands into the function slots (tap a slot to clear it).",
                                style = MaterialTheme.typography.bodySmall
                            )

                            // Helper: rebuild the underlying list from slots whenever they change
                            fun syncFunctionCommandsFromSlots() {
                                functionCommands.clear()
                                functionSlots.forEach { slotCmd ->
                                    if (slotCmd != null) {
                                        functionCommands.add(slotCmd)
                                    }
                                }
                                functionReady = false
                            }

                            // Row of fixed slots, Kodable-style
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                functionSlots.forEachIndexed { index, slotCmd ->
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .border(
                                                width = 2.dp,
                                                color = if (slotCmd == null) Color.DarkGray else Color(0xFF6ABE30),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(Color(0xFF1A242E), RoundedCornerShape(8.dp))
                                            // Each slot is its own drop target
                                            .dragAndDropTarget(
                                                shouldStartDragAndDrop = { event ->
                                                    event.mimeTypes()
                                                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                                },
                                                target = remember(index) {
                                                    object : DragAndDropTarget {
                                                        override fun onDrop(
                                                            event: androidx.compose.ui.draganddrop.DragAndDropEvent
                                                        ): Boolean {
                                                            val clipData =
                                                                event.toAndroidDragEvent().clipData ?: return false
                                                            if (clipData.itemCount < 1) return false
                                                            val text =
                                                                clipData.getItemAt(0).text?.toString() ?: return false

                                                            val cmd = when (text) {
                                                                "UP"     -> Command.MOVE_UP
                                                                "DOWN"   -> Command.MOVE_DOWN
                                                                "LEFT"   -> Command.MOVE_LEFT
                                                                "RIGHT"  -> Command.MOVE_RIGHT
                                                                "ATTACK" -> Command.ATTACK
                                                                else     -> null
                                                            }

                                                            if (cmd != null && !isRunning) {
                                                                // Drop replaces whatever was in this slot
                                                                functionSlots[index] = cmd
                                                                syncFunctionCommandsFromSlots()
                                                                return true
                                                            }
                                                            return false
                                                        }
                                                    }
                                                }
                                            )
                                            // Tap a filled slot to clear it
                                            .clickable(
                                                enabled = !isRunning && slotCmd != null
                                            ) {
                                                functionSlots[index] = null
                                                syncFunctionCommandsFromSlots()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (slotCmd == null) {
                                            // Empty slot: show its index
                                            Text(
                                                text = "${index + 1}",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        } else {
                                            // Filled slot: draw the arrow / attack icon + index
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                if (slotCmd == Command.ATTACK) {
                                                    Image(
                                                        painter = attackPainter,
                                                        contentDescription = "Attack",
                                                        modifier = Modifier.size(28.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else {
                                                    val rotation = when (slotCmd) {
                                                        Command.MOVE_UP    ->  90f
                                                        Command.MOVE_DOWN  -> -90f
                                                        Command.MOVE_LEFT  ->   0f
                                                        Command.MOVE_RIGHT -> 180f
                                                        else               ->   0f
                                                    }

                                                    Image(
                                                        painter = commandArrowPainter,
                                                        contentDescription = slotCmd.name,
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .graphicsLayer(rotationZ = rotation),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }

                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Little counter under the slots
                            Text(
                                text = "Function steps used: ${
                                    functionSlots.count { it != null }
                                } / $maxFunctionSlots",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            // Loop icon + count (uses your loop picture)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.loop),
                                        contentDescription = "Loop",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                    Text(
                                        text = functionRepeatCount.toString(),
                                        color = Color.Black,
                                        style = MaterialTheme.typography.titleMedium,  // bigger
                                        textAlign = TextAlign.Center
                                    )
                                }


                                Button(
                                    enabled = !isRunning && functionRepeatCount > 1,
                                    onClick = { functionRepeatCount-- }
                                ) {
                                    Text("-")
                                }

                                Button(
                                    enabled = !isRunning && functionRepeatCount < 9,
                                    onClick = { functionRepeatCount++ }
                                ) {
                                    Text("+")
                                }

                                Button(
                                    enabled = !isRunning,
                                    onClick = { functionRepeatCount = 1 }
                                ) {
                                    Text("Clear Loop")
                                }
                            }

                            // 🔹 NEW: Clear Function button
                            Button(
                                enabled = functionCommands.isNotEmpty() && !isRunning,
                                onClick = {
                                    // Clear all slots
                                    for (i in 0 until functionSlots.size) {
                                        functionSlots[i] = null
                                    }

                                    functionCommands.clear()
                                    functionRepeatCount = 1
                                    functionReady = false
                                    statusMessage =
                                        "Function cleared. Drag new arrows to define it again."
                                }
                            ) {
                                Text("Clear Function")
                            }

                            // Count how many functions are currently "active":
                            //   - either they still have an unused gem
                            //   - or they are referenced somewhere in the program
                            val activeFunctionCount = userFunctions.count { fn ->
                                unusedFunctionIds.contains(fn.id) ||
                                        programFunctionRefs.any { it?.id == fn.id }
                            }
                            val remainingFunctions =
                                (maxFunctions - activeFunctionCount).coerceAtLeast(0)

                            // Show how many functions the kid can still create
                            Text(
                                text = "Functions left: $remainingFunctions / $maxFunctions",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )


                            // Generate a new function + gem (up to 4 total)
                            Button(
                                enabled = functionCommands.isNotEmpty() && !isRunning && activeFunctionCount < maxFunctions,
                                onClick = {
                                    val colorOrder = listOf(
                                        GemColor.RED,
                                        GemColor.BLUE,
                                        GemColor.GREEN,
                                        GemColor.PURPLE
                                    )

                                    val color = colorOrder[nextGemColorIndex]
                                    val newId = (userFunctions.maxOfOrNull { it.id } ?: -1) + 1

                                    val fn = UserFunction(
                                        id = newId,
                                        color = color,
                                        commands = functionCommands.toList(),  // copy the 1–4 commands
                                        repeatCount = functionRepeatCount
                                    )

                                    // Add this function and mark its gem as "unused" (available to drag once)
                                    userFunctions.add(fn)
                                    unusedFunctionIds.add(fn.id)   // 🔹 NEW

                                    // Advance color index cyclically
                                    nextGemColorIndex = (nextGemColorIndex + 1) % colorOrder.size

                                    // Reset the function maker panel (builder only)
                                    functionCommands.clear()
                                    functionRepeatCount = 1
                                    functionReady = false

                                    for (i in 0 until functionSlots.size) {
                                        functionSlots[i] = null
                                    }

                                    statusMessage =
                                        "Function created! Drag the gem into your program."
                                }
                            ) {
                                Text("Generate Function")
                            }

                            // Existing functions as gem drag sources
                            if (userFunctions.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Your functions:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    userFunctions
                                        .filter { unusedFunctionIds.contains(it.id) }   // only unused gems
                                        .forEach { fn ->

                                            val gemPainter = when (fn.color) {
                                                GemColor.RED -> painterResource(R.drawable.red_gem)
                                                GemColor.BLUE -> painterResource(R.drawable.blue_gem)
                                                GemColor.GREEN -> painterResource(R.drawable.green_gem)
                                                GemColor.PURPLE -> painterResource(R.drawable.purple_gem)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(
                                                        Color(0xFF1A242E),
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .dragAndDropSource(
                                                        transferData = {
                                                            DragAndDropTransferData(
                                                                ClipData.newPlainText(
                                                                    "command",
                                                                    "FUNC_${fn.id}"
                                                                )
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // Gem image itself
                                                Image(
                                                    painter = gemPainter,
                                                    contentDescription = "Function gem",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )

                                                // Loop count overlay on bottom
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .background(
                                                            Color(0xAA000000),
                                                            RoundedCornerShape(
                                                                topStart = 6.dp,
                                                                topEnd = 6.dp
                                                            )
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "x${fn.repeatCount}",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }
                                        }

                                }
                            }


                            // Draggable function icon (we’ll swap this to gems later)
                            if (functionReady && functionCommands.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Drag your function into the program:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color(0xFF1A242E), RoundedCornerShape(10.dp))
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFF6ABE30), // green outline
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .dragAndDropSource(
                                            transferData = {
                                                DragAndDropTransferData(
                                                    ClipData.newPlainText(
                                                        "command",
                                                        "FUNC1"   // function call marker
                                                    )
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "F",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Program:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Fixed command slots (like Kodable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                commandSlots.forEachIndexed { index, slotCmd ->
                    val fn = commandSlotFunctionRefs[index]
                    val isFunctionCall = (slotCmd == Command.FUNCTION_1 && fn != null)

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1A242E), RoundedCornerShape(10.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            // Each slot is its own drop target
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    event.mimeTypes()
                                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                },
                                target = remember(index) {
                                    object : DragAndDropTarget {
                                        override fun onDrop(
                                            event: androidx.compose.ui.draganddrop.DragAndDropEvent
                                        ): Boolean {
                                            val clipData =
                                                event.toAndroidDragEvent().clipData ?: return false
                                            if (clipData.itemCount < 1) return false
                                            val text =
                                                clipData.getItemAt(0).text?.toString()
                                                    ?: return false

                                            // 1) Normal arrow / ATTACK commands
                                            if (text in listOf("UP","DOWN","LEFT","RIGHT","ATTACK")) {
                                                val cmd = when (text) {
                                                    "UP"      -> Command.MOVE_UP
                                                    "DOWN"    -> Command.MOVE_DOWN
                                                    "LEFT"    -> Command.MOVE_LEFT
                                                    "RIGHT"   -> Command.MOVE_RIGHT
                                                    "ATTACK"  -> Command.ATTACK
                                                    else      -> null
                                                }

                                                if (cmd != null && !isRunning) {
                                                    commandSlots[index] = cmd
                                                    commandSlotFunctionRefs[index] = null
                                                    return true
                                                }
                                                return false
                                            }

                                            // 2) Function gem drops: "FUNC_<id>"
                                            if (text.startsWith("FUNC_")) {
                                                val idPart = text.removePrefix("FUNC_")
                                                val fnId = idPart.toIntOrNull()

                                                if (fnId != null && !isRunning) {
                                                    val dropFn = userFunctions.find { it.id == fnId }
                                                    if (dropFn != null) {
                                                        commandSlots[index] = Command.FUNCTION_1
                                                        commandSlotFunctionRefs[index] = dropFn

                                                        // Single-use gem: mark as used
                                                        unusedFunctionIds.remove(dropFn.id)
                                                        return true
                                                    }
                                                }
                                                return false
                                            }

                                            return false
                                        }
                                    }
                                }
                            )
                            // Tap a slot to clear it
                            .clickable(
                                enabled = !isRunning && (slotCmd != null || fn != null)
                            ) {
                                commandSlots[index] = null
                                commandSlotFunctionRefs[index] = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isFunctionCall && fn != null -> {
                                // Show gem + step number
                                val gemPainter = when (fn.color) {
                                    GemColor.RED    -> painterResource(R.drawable.red_gem)
                                    GemColor.BLUE   -> painterResource(R.drawable.blue_gem)
                                    GemColor.GREEN  -> painterResource(R.drawable.green_gem)
                                    GemColor.PURPLE -> painterResource(R.drawable.purple_gem)
                                }

                                Box(
                                    modifier = Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = gemPainter,
                                        contentDescription = "Function gem",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                )
                            }

                            slotCmd == Command.ATTACK -> {
                                // Sword icon + index
                                Image(
                                    painter = attackPainter,
                                    contentDescription = "Attack",
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                )
                            }

                            slotCmd != null -> {
                                // Normal movement arrow + index
                                val rotation = when (slotCmd) {
                                    Command.MOVE_UP    ->  90f
                                    Command.MOVE_DOWN  -> -90f
                                    Command.MOVE_LEFT  ->   0f
                                    Command.MOVE_RIGHT -> 180f
                                    else               -> 0f
                                }
                                Image(
                                    painter = commandArrowPainter,
                                    contentDescription = slotCmd.name,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .graphicsLayer(rotationZ = rotation),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 2.dp)
                                )
                            }

                            else -> {
                                // Empty slot placeholder
                                Text(
                                    text = "${index + 1}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap a filled slot to clear it.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )



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
                    enabled = !isRunning && commandSlots.any { it != null },
                    onClick = {
                        statusMessage = ""
                        showResultDialog = false
                        isSuccessResult = false
                        runResultCode = ""

                        buttonPressed = false

                        // Build program from slots
                        program.clear()
                        programFunctionRefs.clear()
                        commandSlots.forEachIndexed { index, cmd ->
                            val fnRef = commandSlotFunctionRefs[index]
                            if (cmd != null) {
                                program.add(cmd)
                                programFunctionRefs.add(fnRef)
                            }
                        }

                        scope.launch {
                            isRunning = true
                            heroPos = gameMap.startX to gameMap.startY
                            heroFacing = HeroFacing.DOWN
                            heroSinkProgress = 0f
                            standingOnMonster = null
                            monsterTiles.clear()
                            monsterTiles.addAll(initialMonsterTiles)


                            var touchedWater = false
                            var reachedGoal = false

                            // 1) Build a flattened program that expands each FUNCTION_1
                            val expandedProgram = mutableListOf<Command>()

                            program.forEachIndexed { index, cmd ->
                                val fn = programFunctionRefs.getOrNull(index)

                                if (cmd == Command.FUNCTION_1 && fn != null) {
                                    // Use this gem's own commands + repeatCount
                                    repeat(fn.repeatCount) {
                                        fn.commands.forEach { inner ->
                                            if (inner != Command.FUNCTION_1) {
                                                expandedProgram.add(inner)
                                            }
                                        }
                                    }
                                } else if (cmd != Command.FUNCTION_1) {
                                    // Normal arrow command
                                    expandedProgram.add(cmd)
                                }
                            }

                            // 2) Run your existing slide + monster logic on expandedProgram
                            outer@ for ((expandedProgramIndex, cmd) in expandedProgram.withIndex()) {

                                // 0) If we are currently standing on a monster:
                                if (standingOnMonster != null) {
                                    val monsterPos = standingOnMonster!!

                                    if (cmd == Command.ATTACK) {
                                        // ✅ Kill the monster under Link
                                        monsterTiles.remove(monsterPos)
                                        standingOnMonster = null

                                        // 🔹 Trigger attack animation
                                        // (optional) soundManager.playAttack()
                                        for (i in 0..10) {
                                            heroAttackProgress = i / 10f
                                            monsterPoofPos = monsterPos
                                            monsterPoofProgress = i / 10f
                                            delay(40L)
                                        }
                                        // reset animation state
                                        heroAttackProgress = 0f
                                        monsterPoofProgress = 0f
                                        monsterPoofPos = null

                                        // 🔹 NEW: after killing the monster, keep sliding in the same direction as before
                                        val dir = lastMoveDirection
                                        if (dir != null) {
                                            val (dx2, dy2) = dir

                                            // We treat this as "already moved this command",
                                            // so walls / edges just stop movement (no "bumped into wall" error).
                                            continueSlide@ while (true) {
                                                val nextX = heroPos.first + dx2
                                                val nextY = heroPos.second + dy2

                                                // --- OUT OF BOUNDS: just stop sliding
                                                if (nextX !in 0 until gameMap.width || nextY !in 0 until gameMap.height) {
                                                    break@continueSlide
                                                }

                                                val isOuter = if (hasTileLayout) {
                                                    false
                                                } else {
                                                    isOuterWall(nextX, nextY, gameMap)
                                                }

                                                val isInner = gameMap.walls.contains(nextX to nextY)
                                                val isWater = gameMap.waterTiles.contains(nextX to nextY)

                                                val tileId = gameMap.tileIds
                                                    ?.getOrNull(nextY)
                                                    ?.getOrNull(nextX)

                                                val isPitTile = tileId == "pit_top" || tileId == "pit_bottom"
                                                val isButtonTile = tileId == "button_unpressed" ||
                                                        tileId == "button_pressed" ||
                                                        tileId == "button"

                                                val isPit = pitTiles.contains(nextX to nextY)
                                                val isButton = buttonTiles.contains(nextX to nextY)
                                                val isMonsterNext = monsterTiles.contains(nextX to nextY)
                                                val isIfTile = ifTiles.contains(nextX to nextY)

                                                // --- IF TILE: move onto it and stop, like normal
                                                if (isIfTile) {
                                                    heroPos = nextX to nextY
                                                    delay(150L)
                                                    break@continueSlide
                                                }

                                                // --- PIT (still deadly if button not pressed) ---
                                                if (isPitTile && !buttonPressed) {
                                                    heroPos = nextX to nextY
                                                    soundManager.playSplash()
                                                    for (i in 0..10) {
                                                        heroSinkProgress = i / 10f
                                                        delay(50L)
                                                    }
                                                    statusMessage = "Link fell into the pit!"
                                                    resultTitle = "Pitfall!"
                                                    resultBody = "The pit was still open. Try pressing the button first."
                                                    isSuccessResult = false
                                                    runResultCode = "PIT"
                                                    showResultDialog = true
                                                    isRunning = false
                                                    break@continueSlide
                                                }

                                                // --- BUTTON ---
                                                if (isButtonTile && !buttonPressed) {
                                                    buttonPressed = true
                                                }

                                                // --- WATER (still deadly) ---
                                                if (isWater) {
                                                    heroPos = nextX to nextY
                                                    soundManager.playSplash()
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
                                                    touchedWater = true      // ← match your main slide logic if you do this there
                                                    break@continueSlide
                                                }

                                                // --- WALL: just stop sliding, no error popup ---
                                                if (isOuter || isInner) {
                                                    break@continueSlide
                                                }

                                                // --- ANOTHER MONSTER AHEAD ---
                                                if (isMonsterNext) {
                                                    heroPos = nextX to nextY
                                                    standingOnMonster = nextX to nextY
                                                    // pause on the new monster; the *next* command must attack again
                                                    delay(150L)
                                                    break@continueSlide
                                                }

                                                // --- SAFE TILE ---
                                                heroPos = nextX to nextY

                                                if (heroPos.first == gameMap.goalX && heroPos.second == gameMap.goalY) {
                                                    statusMessage = "Reached the goal!"
                                                    resultTitle = "Great Job!"
                                                    resultBody = "You guided Link successfully."
                                                    isSuccessResult = true
                                                    runResultCode = "SUCCESS"
                                                    soundManager.playSuccess()
                                                    showResultDialog = true
                                                    isRunning = false
                                                    break@continueSlide
                                                }

                                                // keep sliding visually
                                                delay(200L)
                                            }
                                        }

                                        // ATTACK is finished; move on to the next command
                                        continue@outer
                                    }

                                }


                                if (cmd == Command.ATTACK) {
                                    // Swing sword even if there's no monster
                                    for (i in 0..10) {
                                        heroAttackProgress = i / 10f
                                        delay(40L)
                                    }
                                    heroAttackProgress = 0f
                                    continue@outer
                                }

                                // 2) Only MOVE_* commands should run sliding movement
                                if (cmd != Command.MOVE_UP &&
                                    cmd != Command.MOVE_DOWN &&
                                    cmd != Command.MOVE_LEFT &&
                                    cmd != Command.MOVE_RIGHT
                                ) {
                                    // Defensive: ignore anything unexpected
                                    continue@outer
                                }

                                // --- Normal movement logic from here down ---

                                // Update facing
                                heroFacing = when (cmd) {
                                    Command.MOVE_UP    -> HeroFacing.UP
                                    Command.MOVE_DOWN  -> HeroFacing.DOWN
                                    Command.MOVE_LEFT  -> HeroFacing.LEFT
                                    Command.MOVE_RIGHT -> HeroFacing.RIGHT
                                    else               -> heroFacing
                                }

                                // Direction vector
                                val (dx, dy) = when (cmd) {
                                    Command.MOVE_UP    -> 0 to -1
                                    Command.MOVE_DOWN  -> 0 to 1
                                    Command.MOVE_LEFT  -> -1 to 0
                                    Command.MOVE_RIGHT -> 1 to 0
                                    else               -> 0 to 0
                                }

                                // Remember this direction so we can resume sliding after a monster is killed
                                lastMoveDirection = dx to dy

                                var movedThisCommand = false

                                // Slide in this direction until blocked
                                slideLoop@ while (true) {
                                    val nextX = heroPos.first + dx
                                    val nextY = heroPos.second + dy

                                    // --- OUT OF BOUNDS CHECK ---
                                    if (nextX !in 0 until gameMap.width || nextY !in 0 until gameMap.height) {
                                        if (!movedThisCommand) {
                                            statusMessage = "Went out of bounds!"
                                            resultTitle = "Out of Bounds"
                                            resultBody = "Your program moved Link off the map."
                                            isSuccessResult = false
                                            runResultCode = "OUT_OF_BOUNDS"
                                            soundManager.playFailure()
                                            showResultDialog = true
                                            isRunning = false
                                            break@slideLoop
                                        } else {
                                            break@slideLoop
                                        }
                                    }

                                    val isOuter = if (hasTileLayout) {
                                        false
                                    } else {
                                        isOuterWall(nextX, nextY, gameMap)
                                    }

                                    val isInner = gameMap.walls.contains(nextX to nextY)
                                    val isWater = gameMap.waterTiles.contains(nextX to nextY)

                                    val tileId = gameMap.tileIds
                                        ?.getOrNull(nextY)
                                        ?.getOrNull(nextX)

                                    val isPitTile = tileId == "pit_top" || tileId == "pit_bottom"
                                    val isButtonTile = tileId == "button_unpressed" ||
                                            tileId == "button_pressed" ||
                                            tileId == "button"

                                    val isPit = pitTiles.contains(nextX to nextY)
                                    val isButton = buttonTiles.contains(nextX to nextY)
                                    val isMonster = monsterTiles.contains(nextX to nextY)
                                    val isIfTile = ifTiles.contains(nextX to nextY)

                                    // --- IF TILE ---
                                    if (isIfTile) {
                                        heroPos = nextX to nextY
                                        movedThisCommand = true
                                        delay(150L)
                                        break@slideLoop
                                    }

                                    // --- PIT (deadly if button not pressed) ---
                                    if (isPitTile && !buttonPressed) {
                                        heroPos = nextX to nextY
                                        soundManager.playSplash()
                                        for (i in 0..10) {
                                            heroSinkProgress = i / 10f
                                            delay(50L)
                                        }
                                        statusMessage = "Link fell into the pit!"
                                        resultTitle = "Pitfall!"
                                        resultBody = "The pit was still open. Try pressing the button first."
                                        isSuccessResult = false
                                        runResultCode = "PIT"
                                        showResultDialog = true
                                        isRunning = false
                                        touchedWater = true
                                        break@slideLoop
                                    }

                                    // --- BUTTON ---
                                    if (isButtonTile && !buttonPressed) {
                                        buttonPressed = true
                                    }

                                    // --- WATER ---
                                    if (isWater) {
                                        heroPos = nextX to nextY
                                        soundManager.playSplash()
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
                                        break@slideLoop
                                    }

                                    // --- WALL ---
                                    if (isOuter || isInner) {
                                        if (!movedThisCommand) {
                                            soundManager.playBonk()
                                            heroShake = when (cmd) {
                                                Command.MOVE_UP    -> 0 to -4
                                                Command.MOVE_DOWN  -> 0 to 4
                                                Command.MOVE_LEFT  -> -4 to 0
                                                Command.MOVE_RIGHT -> 4 to 0
                                                else               -> 0 to 0
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
                                            break@slideLoop
                                        } else {
                                            break@slideLoop
                                        }
                                    }

                                    // --- MONSTER ---
                                    if (isMonster) {
                                        heroPos = nextX to nextY
                                        movedThisCommand = true
                                        standingOnMonster = nextX to nextY
                                        // show Link on the monster tile
                                        delay(150L)
                                        break@slideLoop
                                    }

                                    // --- SAFE TILE ---
                                    heroPos = nextX to nextY
                                    movedThisCommand = true

                                    if (heroPos.first == gameMap.goalX && heroPos.second == gameMap.goalY) {
                                        statusMessage = "Reached the goal!"
                                        resultTitle = "Great Job!"
                                        resultBody = "You guided Link successfully."
                                        isSuccessResult = true
                                        runResultCode = "SUCCESS"
                                        soundManager.playSuccess()
                                        showResultDialog = true
                                        isRunning = false
                                        reachedGoal = true
                                        break@slideLoop
                                    }

                                    // show sliding
                                    delay(200L)
                                }

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

                            logger.logAttempt(
                                childName = currentKidName,
                                levelId = level.id,
                                gameId = gameMap.id,
                                resultCode = if (runResultCode.isNotEmpty()) runResultCode else "UNKNOWN",
                                commandsCount = program.size
                            )

                            // 🔹 If the run was a success, clear the program line
                            if (isSuccessResult) {
                                program.clear()
                                programFunctionRefs.clear()

                                commandSlots.indices.forEach { i ->
                                    commandSlots[i] = null
                                    commandSlotFunctionRefs[i] = null
                                }
                            }

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
                        programFunctionRefs.clear()
                        statusMessage = ""
                        showResultDialog = false
                        buttonPressed = false

                        // Clear all command slots too
                        commandSlots.indices.forEach { i ->
                            commandSlots[i] = null
                            commandSlotFunctionRefs[i] = null
                        }
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

                        // Reset button state (pits become dangerous again)
                        buttonPressed = false

                        // Reset IF tiles
                        ifTiles.clear()
                        remainingIfBlocks = maxIfBlocks

                        // Also clear program + its function refs if you want Reset to fully reset:
                        program.clear()
                        programFunctionRefs.clear()

                        //resets the monsters
                        monsterTiles.clear()
                        monsterTiles.addAll(initialMonsterTiles)
                        standingOnMonster = null

                        // Clear all command slots
                        commandSlots.indices.forEach { i ->
                            commandSlots[i] = null
                            commandSlotFunctionRefs[i] = null
                        }


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
                                buttonPressed = false

                                //resets the monsters
                                monsterTiles.clear()
                                monsterTiles.addAll(initialMonsterTiles)
                                standingOnMonster = null
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