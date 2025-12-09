package com.example.individualproject3

import android.content.ClipDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Author: Carlos Gonzalez with the assistance of AI (ChatGPT)
 * Ram Num: R02190266
 *
 * This file draws the dungeon grid in the actual play screen.
 *
 * Responsibilities:
 * - Render the map background (parchment/map image).
 * - Render tiles either from:
 *   - A full editor-created tile grid (gameMap.tileIds != null), OR
 *   - A fallback ring-based layout using helper functions (for older built-in maps).
 * - Draw hero, goal, monsters, pits, buttons, water, IF tiles, and monster “poof” animation.
 * - Handle drag/drop for IF tiles and tapping IF tiles to remove them.
 */

// ---- Dungeon grid composable ---- //
@Composable
fun DungeonGrid(
    gameMap: GameMap,

    // Current hero position in grid coordinates (x, y)
    heroPos: Pair<Int, Int>,

    // Current hero facing direction (used to select the correct sprite)
    heroFacing: HeroFacing,

    // Small shake offset used when bumping into walls (in dp)
    heroShake: Pair<Int, Int> = 0 to 0,

    // Sinking animation progress (0f = normal, 1f = fully sunk)
    heroSinkProgress: Float = 0f,

    // IF tiles currently placed by the player (grid coordinates)
    ifTiles: Set<Pair<Int, Int>> = emptySet(),

    // Called when an IF block is dropped on a tile (from the specials inventory)
    onDropIfTile: ((Int, Int) -> Unit)? = null,

    // True if the puzzle button has been pressed, which closes pits / changes visuals
    buttonPressed: Boolean = false,

    // Live monster tiles on the grid (grid coordinates)
    monsterTiles: Set<Pair<Int, Int>> = emptySet(),

    // Attack animation progress (0f..1f)
    heroAttackProgress: Float,

    // If not null, the tile where a monster “poof” animation should be drawn
    monsterPoofPos: Pair<Int, Int>?,

    // Monster poof animation progress (0f..1f)
    monsterPoofProgress: Float,

    // Optional tap callback to remove an IF tile when the player taps it
    onTapIfTile: ((Int, Int) -> Unit)? = null,
) {
    // Parchment/map background behind the grid
    val mapBgPainter = painterResource(R.drawable.map_background)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // The grid is drawn at ~88% of the available width so the parchment can be visible around it.
        val gridWidth: Dp = maxWidth * 0.88f
        val tileSize: Dp = gridWidth / gameMap.width

        // Monster base sprite (overlays on top of floor)
        val monsterPainter = painterResource(R.drawable.monster_left)

        // 4-frame monster death / poof animation
        val monsterPoofStage1 = painterResource(R.drawable.monster_death_stage_1)
        val monsterPoofStage2 = painterResource(R.drawable.monster_death_stage_2)
        val monsterPoofStage3 = painterResource(R.drawable.monster_death_stage_3)
        val monsterPoofStage4 = painterResource(R.drawable.monster_death_stage_4)

        // --- Hero sprites (movement + attack + sinking) --- //
        val heroPainter = when {

            // SINKING ANIMATION — DO NOT CHANGE THIS
            // We keep using the "normal" facing sprite, and only move it down with heroSinkProgress.
            heroSinkProgress > 0f -> {
                when (heroFacing) {
                    HeroFacing.UP -> painterResource(R.drawable.up_sprite)
                    HeroFacing.DOWN -> painterResource(R.drawable.down_sprite)
                    HeroFacing.LEFT -> painterResource(R.drawable.left_sprite)
                    HeroFacing.RIGHT -> painterResource(R.drawable.right_sprite)
                }
            }

            // Attack sprites (direction-based)
            heroAttackProgress > 0f && heroFacing == HeroFacing.RIGHT ->
                painterResource(R.drawable.hero_attack_right)

            heroAttackProgress > 0f && heroFacing == HeroFacing.LEFT ->
                painterResource(R.drawable.hero_attack_left)

            heroAttackProgress > 0f && heroFacing == HeroFacing.UP ->
                painterResource(R.drawable.hero_attack_up)

            heroAttackProgress > 0f && heroFacing == HeroFacing.DOWN ->
                painterResource(R.drawable.hero_attack_down)

            // Normal non-attacking movement sprites
            heroFacing == HeroFacing.UP ->
                painterResource(R.drawable.up_sprite)

            heroFacing == HeroFacing.DOWN ->
                painterResource(R.drawable.down_sprite)

            heroFacing == HeroFacing.LEFT ->
                painterResource(R.drawable.left_sprite)

            heroFacing == HeroFacing.RIGHT ->
                painterResource(R.drawable.right_sprite)

            // Defensive fallback (should basically never be hit)
            else ->
                painterResource(R.drawable.down_sprite)
        }

        val maxX = gameMap.width - 1
        val maxY = gameMap.height - 1

        // If this map has a full tile grid from the level editor, use that.
        // Otherwise fall back to the old “auto walls” layout.
        val tiles = gameMap.tileIds
        if (tiles != null) {
            // --- EDITOR-BASED GRID RENDERING --- //
            // Map the editor tile IDs to the correct painter resources.
            val painterById: Map<String, Painter> = mapOf(
                "floor" to painterResource(R.drawable.floor_tile),
                "inner_wall" to painterResource(R.drawable.inner_wall),
                "water" to painterResource(R.drawable.water_tile),

                "left_upper" to painterResource(R.drawable.left_side_upper_wall),
                "left_lower" to painterResource(R.drawable.left_side_lower_wall),
                "right_upper" to painterResource(R.drawable.right_side_upper_wall),
                "right_lower" to painterResource(R.drawable.right_side_lower_wall),

                "top_upper" to painterResource(R.drawable.top_side_upper_wall),
                "top_lower" to painterResource(R.drawable.top_side_lower_wall),
                "bottom_upper" to painterResource(R.drawable.bottom_side_upper_wall),
                "bottom_lower" to painterResource(R.drawable.bottom_side_lower_wall),

                "tl_lower" to painterResource(R.drawable.top_left_corner_lower_wall),
                "tr_lower" to painterResource(R.drawable.top_right_side_lower_wall),
                "bl_lower" to painterResource(R.drawable.bottom_left_side_lower_wall),
                "br_lower" to painterResource(R.drawable.bottom_right_side_lower_wall),

                "tl_upper" to painterResource(R.drawable.top_left_corner_upper_wall),
                "tr_upper" to painterResource(R.drawable.top_right_side_upper_wall),
                "bl_upper" to painterResource(R.drawable.bottom_left_side_upper_wall),
                "br_upper" to painterResource(R.drawable.bottom_right_side_upper_wall),

                "outer_tl" to painterResource(R.drawable.outer_top_left_corner),
                "outer_tr" to painterResource(R.drawable.outer_top_right_corner),
                "outer_bl" to painterResource(R.drawable.outer_bottom_left_corner),
                "outer_br" to painterResource(R.drawable.outer_bottom_right_corner),

                "inner_tl" to painterResource(R.drawable.inner_top_left_corner),
                "inner_tr" to painterResource(R.drawable.inner_top_right_corner),
                "inner_bl" to painterResource(R.drawable.inner_bottom_left_corner),
                "inner_br" to painterResource(R.drawable.inner_bottom_right_corner),

                // Monster is rendered as a floor tile with a separate sprite overlayed
                "monster" to painterResource(R.drawable.floor_tile),

                // Pits and button tiles
                "pit_top" to painterResource(R.drawable.pit_top),
                "pit_bottom" to painterResource(R.drawable.pit_bottom),
                "button_unpressed" to painterResource(R.drawable.button_unpressed),
                "button_pressed" to painterResource(R.drawable.button_pressed),
                "button" to painterResource(R.drawable.button_unpressed),
            )

            // Center the grid over the parchment/map background
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Vertical padding between the grid and the edges of the parchment
                val framePaddingY = 24.dp

                // Background parchment
                Image(
                    painter = mapBgPainter,
                    contentDescription = "Map Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tileSize * gameMap.height + framePaddingY * 2),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(gridWidth)
                ) {
                    for (y in 0 until gameMap.height) {
                        Row {
                            for (x in 0 until gameMap.width) {
                                val isHero = (heroPos.first == x && heroPos.second == y)
                                val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                                val rawId = tiles[y][x]

                                // effectiveId applies dynamic rules:
                                // - Pits become floor when the button is pressed.
                                // - Button art switches between pressed / unpressed.
                                val effectiveId = when (rawId) {
                                    // Pits: look like pits before the button, become safe floor after
                                    "pit_top", "pit_bottom" ->
                                        if (buttonPressed) "floor" else rawId

                                    // Button states
                                    "button_unpressed", "button_pressed", "button" ->
                                        if (buttonPressed) "button_pressed" else "button_unpressed"

                                    else -> rawId
                                }

                                val basePainter: Painter? = painterById[effectiveId]

                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .background(Color.Transparent)
                                        // Enable drag-and-drop target behavior only if a callback exists.
                                        .let { base ->
                                            if (onDropIfTile == null) {
                                                base
                                            } else {
                                                base.dragAndDropTarget(
                                                    shouldStartDragAndDrop = { event ->
                                                        event.mimeTypes()
                                                            .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                                    },
                                                    target = object :
                                                        androidx.compose.ui.draganddrop.DragAndDropTarget {

                                                        /**
                                                         * Handle IF tile drop events on a specific tile.
                                                         * We only react to "IF_TILE" payloads and only if:
                                                         * - The tile is a floor tile.
                                                         * - The tile is NOT the goal.
                                                         * - The tile is NOT where the hero currently stands.
                                                         */
                                                        override fun onDrop(
                                                            event: androidx.compose.ui.draganddrop.DragAndDropEvent
                                                        ): Boolean {
                                                            val clipData =
                                                                event.toAndroidDragEvent().clipData
                                                                    ?: return false
                                                            if (clipData.itemCount < 1) return false
                                                            val text = clipData.getItemAt(0)
                                                                .text?.toString() ?: return false

                                                            // Only accept IF tile drags
                                                            if (text == "IF_TILE") {
                                                                val isHeroHere =
                                                                    (heroPos.first == x && heroPos.second == y)
                                                                val isGoalHere =
                                                                    (gameMap.goalX == x && gameMap.goalY == y)
                                                                val isFloorHere =
                                                                    (effectiveId == "floor")

                                                                // Block placing IF on:
                                                                // - non-floor tiles
                                                                // - the goal
                                                                // - the hero position
                                                                if (!isFloorHere || isGoalHere || isHeroHere) {
                                                                    return false
                                                                }

                                                                // Valid drop position -> notify caller
                                                                onDropIfTile.invoke(x, y)
                                                                return true
                                                            }
                                                            return false
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Base tile (floor, wall, water, pit, etc.)
                                    if (basePainter != null) {
                                        Image(
                                            painter = basePainter,
                                            contentDescription = rawId,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.FillBounds
                                        )
                                    }

                                    // IF tile overlay (can be tapped to remove, if callback provided)
                                    if (ifTiles.contains(x to y)) {
                                        Image(
                                            painter = painterResource(R.drawable.if_tile),
                                            contentDescription = "IF tile",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable(enabled = onTapIfTile != null) {
                                                    onTapIfTile?.invoke(x, y)
                                                },
                                            contentScale = ContentScale.FillBounds
                                        )
                                    }

                                    // Monster overlay sprite
                                    if (monsterTiles.contains(x to y)) {
                                        Image(
                                            painter = monsterPainter,
                                            contentDescription = "Monster",
                                            modifier = Modifier.matchParentSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Monster poof animation overlay (4-frame sequence)
                                    if (monsterPoofPos != null &&
                                        monsterPoofPos == (x to y) &&
                                        monsterPoofProgress > 0f
                                    ) {
                                        // Choose which poof frame to show based on progress.
                                        val framePainter = when {
                                            monsterPoofProgress < 0.25f -> monsterPoofStage1
                                            monsterPoofProgress < 0.50f -> monsterPoofStage2
                                            monsterPoofProgress < 0.75f -> monsterPoofStage3
                                            else -> monsterPoofStage4
                                        }

                                        Image(
                                            painter = framePainter,
                                            contentDescription = "Monster Poof",
                                            modifier = Modifier
                                                .matchParentSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Goal overlay (door / portal)
                                    if (isGoal) {
                                        Image(
                                            painter = painterResource(R.drawable.goal),
                                            contentDescription = "Goal",
                                            modifier = Modifier.fillMaxSize(0.85f),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Hero overlay (with shake + vertical sinking)
                                    if (isHero) {
                                        Image(
                                            painter = heroPainter,
                                            contentDescription = "Hero",
                                            modifier = Modifier
                                                .offset(
                                                    x = heroShake.first.dp,
                                                    y = heroShake.second.dp
                                                )
                                                .graphicsLayer {
                                                    // Move hero sprite downward based on sink progress.
                                                    translationY += heroSinkProgress * tileSize.toPx()
                                                }
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
        } else {
            // ---------- FALLBACK: old arena/room visuals for built-in levels ----------
            // This path is used when there is no editor-provided tile grid.
            // It builds a two-ring outer wall area, inner walls, water, goal, etc.

            val floorTile = painterResource(R.drawable.floor_tile)
            val waterTile = painterResource(R.drawable.water_tile)
            val innerWall = painterResource(R.drawable.inner_wall)

            // Outer wall tiles (upper ring)
            val topSideUpper = painterResource(R.drawable.top_side_upper_wall)
            val bottomSideUpper = painterResource(R.drawable.bottom_side_upper_wall)
            val leftSideUpper = painterResource(R.drawable.left_side_upper_wall)
            val rightSideUpper = painterResource(R.drawable.right_side_upper_wall)
            val topLeftUpperCorner = painterResource(R.drawable.top_left_corner_upper_wall)
            val topRightUpperCorner = painterResource(R.drawable.top_right_side_upper_wall)
            val bottomLeftUpperCorner = painterResource(R.drawable.bottom_left_side_upper_wall)
            val bottomRightUpperCorner = painterResource(R.drawable.bottom_right_side_upper_wall)

            // Outer wall tiles (lower ring)
            val topSideLower = painterResource(R.drawable.top_side_lower_wall)
            val bottomSideLower = painterResource(R.drawable.bottom_side_lower_wall)
            val leftSideLower = painterResource(R.drawable.left_side_lower_wall)
            val rightSideLower = painterResource(R.drawable.right_side_lower_wall)
            val topLeftLowerCorner = painterResource(R.drawable.top_left_corner_lower_wall)
            val topRightLowerCorner = painterResource(R.drawable.top_right_side_lower_wall)
            val bottomLeftLowerCorner = painterResource(R.drawable.bottom_left_side_lower_wall)
            val bottomRightLowerCorner =
                painterResource(R.drawable.bottom_right_side_lower_wall)

            val doorGoal = painterResource(R.drawable.goal)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val framePaddingY = 16.dp

                // Parchment background for the fallback layout
                Image(
                    painter = mapBgPainter,
                    contentDescription = "Map Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tileSize * gameMap.height + framePaddingY * 2),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(gridWidth)
                ) {
                    for (y in 0 until gameMap.height) {
                        Row {
                            for (x in 0 until gameMap.width) {
                                val isHero = (heroPos.first == x && heroPos.second == y)
                                val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                                // Base painter chosen by outer/inner ring and tile type.
                                val basePainter: Painter = when {
                                    // Outer wall: upper ring (edges of the map)
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

                                    // Outer wall: lower ring (one tile inside the outer ring)
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

                                    // Goal vs normal floor
                                    isGoal -> doorGoal
                                    else -> floorTile
                                }

                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .background(Color.Black),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Draw the base floor/wall/water/goal tile
                                    Image(
                                        painter = basePainter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds
                                    )

                                    // Hero in the fallback layout also supports shake + sink.
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
                                                    // In this fallback path, we scale + fade out the hero as they sink.
                                                    scaleX = 1f - heroSinkProgress * 0.6f,
                                                    scaleY = 1f - heroSinkProgress * 0.6f,
                                                    alpha = 1f - heroSinkProgress
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
}

/**
 * True if (x, y) is on the outer ring of the map (the very edge).
 * Used for drawing the outermost border walls in the fallback renderer.
 */
fun isUpperWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 0 || x == maxX || y == 0 || y == maxY)
}

/**
 * True if (x, y) is on the second ring inside the map boundary.
 * Used for drawing the inner ring of the outer wall in the fallback renderer.
 */
fun isLowerWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 1 || x == maxX - 1 || y == 1 || y == maxY - 1)
}

/**
 * Helper for movement logic in GameScreen:
 * A tile is considered an "outer wall" if it belongs to either wall ring.
 */
fun isOuterWall(x: Int, y: Int, map: GameMap): Boolean =
    isUpperWallRing(x, y, map) || isLowerWallRing(x, y, map)
