package com.example.individualproject3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Author: Carlos Gonzalez with the assistance of AI (Chat Gpt)
 * Ram Num: R02190266
 *
 * This screen is a **read-only visual preview** of a level created in the
 * custom level editor.
 *
 * It shows:
 *  - The exact tiles placed in the editor (walls, floors, corners, etc.)
 *  - The chosen start position (hero) and goal position (door icon)
 *
 * There is **no gameplay or movement logic** here. This is strictly a
 * visual check so the designer can verify that:
 *  - All corners and walls line up
 *  - Start / goal are where they expect them to be
 *  - The tile IDs match the palette mapping used in the editor/game
 *
 * This is intended for developer/parent testing and can be extended later
 * if you want to "test-run" the level directly from the editor.
 */

/**
 * Simple data structure passed in from the editor code containing the
 * grid dimensions, tile IDs, and start/goal coordinates.
 *
 * @param testLevel  The level to show (built from the editor grid)
 * @param onBack     Called when the user taps the back button in the top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTestScreen(
    testLevel: EditorTestLevel,
    onBack: () -> Unit
) {
    // Local palette used only for visual preview (same tile IDs as the editor).
    // It maps a string ID -> drawable resource + human-readable name + logical type.
    val palette: List<PaletteTile> = listOf(
        PaletteTile("floor", "Floor", R.drawable.floor_tile, LogicalTileType.FLOOR),
        PaletteTile("inner_wall", "Inner Wall", R.drawable.inner_wall, LogicalTileType.WALL),
        PaletteTile("water", "Water", R.drawable.water_tile, LogicalTileType.WATER),

        PaletteTile("left_upper", "Left U", R.drawable.left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("left_lower", "Left L", R.drawable.left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("right_upper", "Right U", R.drawable.right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("right_lower", "Right L", R.drawable.right_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("top_upper", "Top U", R.drawable.top_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("top_lower", "Top L", R.drawable.top_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bottom_upper", "Bottom U", R.drawable.bottom_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bottom_lower", "Bottom L", R.drawable.bottom_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("tl_lower", "TL L", R.drawable.top_left_corner_lower_wall, LogicalTileType.WALL),
        PaletteTile("tr_lower", "TR L", R.drawable.top_right_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bl_lower", "BL L", R.drawable.bottom_left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("br_lower", "BR L", R.drawable.bottom_right_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("tl_upper", "TL U", R.drawable.top_left_corner_upper_wall, LogicalTileType.WALL),
        PaletteTile("tr_upper", "TR U", R.drawable.top_right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bl_upper", "BL U", R.drawable.bottom_left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("br_upper", "BR U", R.drawable.bottom_right_side_upper_wall, LogicalTileType.WALL),

        PaletteTile("outer_tl", "Outer TL", R.drawable.outer_top_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_tr", "Outer TR", R.drawable.outer_top_right_corner, LogicalTileType.WALL),
        PaletteTile("outer_bl", "Outer BL", R.drawable.outer_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_br", "Outer BR", R.drawable.outer_bottom_right_corner, LogicalTileType.WALL),

        PaletteTile("inner_tl", "Inner TL", R.drawable.inner_top_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_tr", "Inner TR", R.drawable.inner_top_right_corner, LogicalTileType.WALL),
        PaletteTile("inner_bl", "Inner BL", R.drawable.inner_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_br", "Inner BR", R.drawable.inner_bottom_right_corner, LogicalTileType.WALL)
    )

    // Fast lookup from tile ID -> palette entry, avoids scanning the list in every cell.
    val paletteById = remember { palette.associateBy { it.id } }

    // For preview purposes, hero is always shown using the "facing down" sprite.
    val heroResId = R.drawable.down_sprite
    val goalResId = R.drawable.goal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Level View") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Brief description for whoever is using this screen
            Text(
                "Preview of your level exactly as you drew it.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            // Simple box that contains a compact grid of tiles.
            Box(
                modifier = Modifier
                    .background(Color(0xFF111111))
                    .padding(4.dp)
            ) {
                Column {
                    // Draw the grid row by row
                    for (y in 0 until testLevel.height) {
                        Row {
                            for (x in 0 until testLevel.width) {
                                val id = testLevel.tileIds[y][x]
                                val baseTile = paletteById[id]

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(1.dp, Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Base tile image, or a black box if the tile ID is unknown/empty.
                                    if (baseTile != null) {
                                        Image(
                                            painter = painterResource(id = baseTile.resId),
                                            contentDescription = baseTile.displayName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.FillBounds
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black)
                                        )
                                    }

                                    // Start position overlay: small hero sprite on top of the tile.
                                    if (testLevel.startPos.first == x && testLevel.startPos.second == y) {
                                        Image(
                                            painter = painterResource(id = heroResId),
                                            contentDescription = "Start",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // Goal position overlay: door/goal icon.
                                    if (testLevel.goalPos.first == x && testLevel.goalPos.second == y) {
                                        Image(
                                            painter = painterResource(id = goalResId),
                                            contentDescription = "Goal",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Helpful hint explaining that this is not the "play" screen.
            Text(
                "Use this to verify your tile layout & corners.\n" +
                        "Gameplay (commands / sliding) is still in GameScreen.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
