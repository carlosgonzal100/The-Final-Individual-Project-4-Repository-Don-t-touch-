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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent



/**
 * Author: Carlos Gonzalez with the assistance of AI(Chat Gpt)
 * Ram Num: R02190266
 * description: includes the placment of tiles when making the grid
 * in the actual levels and includes the the animations for bumping
 * into a wall and sinking in to water.
 */

//----the dungeon grid ----------//
//helps with positioning and visuals for the grid
@Composable
fun DungeonGrid(
    gameMap: GameMap,
    heroPos: Pair<Int, Int>,
    heroFacing: HeroFacing,
    heroShake: Pair<Int, Int> = 0 to 0,
    heroSinkProgress: Float = 0f,

    // NEW: IF tiles drawn on top of floor
    ifTiles: Set<Pair<Int, Int>> = emptySet(),
    // NEW: callback when an IF block is dropped on a tile
    onDropIfTile: ((Int, Int) -> Unit)? = null,
    buttonPressed: Boolean = false,
    monsterTiles: Set<Pair<Int, Int>> = emptySet(),
    heroAttackProgress: Float,
    monsterPoofPos: Pair<Int, Int>?,
    monsterPoofProgress: Float,
    onTapIfTile: ((Int, Int) -> Unit)? = null,

    ) {

    val mapBgPainter = painterResource(R.drawable.map_background)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Make the grid use only ~90% of the width so the parchment can show around it
        val gridWidth: Dp = maxWidth * 0.88f
        val tileSize: Dp = gridWidth / gameMap.width

        val monsterPainter = painterResource(R.drawable.monster_left)

        val monsterPoofStage1 = painterResource(R.drawable.monster_death_stage_1)
        val monsterPoofStage2 = painterResource(R.drawable.monster_death_stage_2)
        val monsterPoofStage3 = painterResource(R.drawable.monster_death_stage_3)
        val monsterPoofStage4 = painterResource(R.drawable.monster_death_stage_4)

        // --- Hero sprites --- //
        val heroPainter = when {

            // SINKING ANIMATION — DO NOT CHANGE THIS
            heroSinkProgress > 0f -> {
                // use the normal facing sprite while we animate the sinking
                when (heroFacing) {
                    HeroFacing.UP -> painterResource(R.drawable.up_sprite)
                    HeroFacing.DOWN -> painterResource(R.drawable.down_sprite)
                    HeroFacing.LEFT -> painterResource(R.drawable.left_sprite)
                    HeroFacing.RIGHT -> painterResource(R.drawable.right_sprite)
                }
            }

            heroAttackProgress > 0f && heroFacing == HeroFacing.RIGHT ->
                painterResource(R.drawable.hero_attack_right)

            heroAttackProgress > 0f && heroFacing == HeroFacing.LEFT ->
                painterResource(R.drawable.hero_attack_left)

            heroAttackProgress > 0f && heroFacing == HeroFacing.UP ->
                painterResource(R.drawable.hero_attack_up)

            heroAttackProgress > 0f && heroFacing == HeroFacing.DOWN ->
                painterResource(R.drawable.hero_attack_down)

            heroFacing == HeroFacing.UP ->
                painterResource(R.drawable.up_sprite)

            heroFacing == HeroFacing.DOWN ->
                painterResource(R.drawable.down_sprite)

            heroFacing == HeroFacing.LEFT ->
                painterResource(R.drawable.left_sprite)

            heroFacing == HeroFacing.RIGHT ->
                painterResource(R.drawable.right_sprite)

            else ->
                painterResource(R.drawable.down_sprite)
        }


        val maxX = gameMap.width - 1
        val maxY = gameMap.height - 1

        // If we have a full tile grid from the editor, use it for 1:1 visuals.
        val tiles = gameMap.tileIds
        if (tiles != null) {
            // Match editor palette IDs -> painters
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

                // Monster uses floor as its base; the sprite is drawn as an overlay
                "monster" to painterResource(R.drawable.floor_tile),

                // 🔹 NEW: pits and button
                "pit_top" to painterResource(R.drawable.pit_top),
                "pit_bottom" to painterResource(R.drawable.pit_bottom),
                "button_unpressed" to painterResource(R.drawable.button_unpressed),
                "button_pressed" to painterResource(R.drawable.button_pressed),
                "button" to painterResource(R.drawable.button_unpressed),
            )

            // Center the grid on top of the parchment map
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val framePaddingY = 24.dp   // how much parchment you want above/below the grid

                Image(
                    painter = mapBgPainter,
                    contentDescription = "Map Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tileSize * gameMap.height + framePaddingY * 2),
                    contentScale = ContentScale.FillBounds
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(gridWidth)) {
                    for (y in 0 until gameMap.height) {
                        Row {
                            for (x in 0 until gameMap.width) {
                                val isHero = (heroPos.first == x && heroPos.second == y)
                                val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                                val rawId = tiles[y][x]

                                // Decide what to actually draw based on button state
                                val effectiveId = when (rawId) {
                                    // Pits: look like pits before button, look like floor after
                                    "pit_top", "pit_bottom" ->
                                        if (buttonPressed) "floor" else rawId

                                    // Button: use pressed / unpressed art
                                    "button_unpressed", "button_pressed", "button" ->
                                        if (buttonPressed) "button_pressed" else "button_unpressed"

                                    else -> rawId
                                }

                                val basePainter: Painter? = painterById[effectiveId]


                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .background(Color.Transparent)
                                        // NEW: allow drops if a callback was provided
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
                                                        override fun onDrop(event: androidx.compose.ui.draganddrop.DragAndDropEvent): Boolean {
                                                            val clipData = event.toAndroidDragEvent().clipData ?: return false
                                                            if (clipData.itemCount < 1) return false
                                                            val text = clipData.getItemAt(0).text?.toString() ?: return false

                                                            // Only react to IF_TILE payloads, and only on valid floor tiles
                                                            if (text == "IF_TILE") {
                                                                val isHeroHere = (heroPos.first == x && heroPos.second == y)
                                                                val isGoalHere = (gameMap.goalX == x && gameMap.goalY == y)
                                                                val isFloorHere = (effectiveId == "floor")  // only floor tiles

                                                                // ❌ Block placing IF on non-floor, the goal, or the hero
                                                                if (!isFloorHere || isGoalHere || isHeroHere) {
                                                                    return false
                                                                }

                                                                // ✅ Valid placement
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
                                    if (basePainter != null) {
                                        Image(
                                            painter = basePainter,
                                            contentDescription = rawId,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.FillBounds
                                        )
                                    }

                                    // 🔹 IF tile overlay
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

                                    // 🔹 MONSTER overlay
                                    if (monsterTiles.contains(x to y)) {
                                        Image(
                                            painter = monsterPainter,
                                            contentDescription = "Monster",
                                            modifier = Modifier.matchParentSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // 🔹 MONSTER POOF overlay (uses 4 frames)
                                    if (monsterPoofPos != null &&
                                        monsterPoofPos == (x to y) &&
                                        monsterPoofProgress > 0f
                                    ) {
                                        // Pick which poof frame based on progress 0f..1f
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

                                    // Goal overlay
                                    if (isGoal) {
                                        Image(
                                            painter = painterResource(R.drawable.goal),
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
                                                .graphicsLayer {
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
            val bottomRightLowerCorner = painterResource(R.drawable.bottom_right_side_lower_wall)

            val doorGoal = painterResource(R.drawable.goal)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val framePaddingY = 16.dp   // how much parchment you want above/below the grid

                Image(
                    painter = mapBgPainter,
                    contentDescription = "Map Background",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tileSize * gameMap.height + framePaddingY * 2),
                    contentScale = ContentScale.FillBounds
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(gridWidth)) {
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

