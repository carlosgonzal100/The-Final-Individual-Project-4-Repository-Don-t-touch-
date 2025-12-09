package com.example.individualproject3

/**
 * Author: Carlos Gonzalez with the assistance of AI(Chat Gpt)
 * Ram Num: R02190266
 * description: contains all data for the levels including the default
 * data that i made in my level editor that have been hard coded
 * into the app.
 */

fun createAllLevels(): List<Level> {

    // -------- EASY Levels (handmade easy levels) --------
    val easy1Tiles = listOf(
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "tl_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty", "empty")
    )

    val easyGame1 = gameMapFromTileIds(
        id = "Simple Movement",
        startX = 1,
        startY = 7,
        goalX = 7,
        goalY = 1,
        tileIds = easy1Tiles
    )

    val easy2Tiles = listOf(
        listOf("empty", "empty", "empty", "tl_lower", "top_lower", "tr_lower", "tl_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "left_lower", "button_unpressed", "right_lower", "left_lower", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "left_lower", "floor", "right_lower", "left_lower", "floor", "right_lower"),
        listOf("tl_lower", "top_lower", "top_lower", "inner_br", "floor", "inner_bl", "inner_br", "pit_top", "right_lower"),
        listOf("left_lower", "floor", "floor", "monster", "floor", "pit_bottom", "pit_bottom", "pit_bottom", "right_lower"),
        listOf("left_lower", "floor", "water", "water", "water", "water", "water", "water", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty", "empty")
    )

    val easyGame2 = gameMapFromTileIds(
        id = "If Blocks and Attacks",
        startX = 1,
        startY = 7,
        goalX = 7,
        goalY = 1,
        tileIds = easy2Tiles
    )

    val easy3Tiles = listOf(
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "tl_lower", "top_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "tl_lower", "inner_br", "floor", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "tl_lower", "inner_br", "floor", "floor", "inner_tl", "br_lower"),
        listOf("empty", "empty", "empty", "tl_lower", "inner_br", "floor", "floor", "inner_tl", "br_lower", "empty"),
        listOf("empty", "empty", "tl_lower", "inner_br", "floor", "floor", "inner_tl", "br_lower", "empty", "empty"),
        listOf("empty", "tl_lower", "inner_br", "floor", "floor", "inner_tl", "br_lower", "empty", "empty", "empty"),
        listOf("tl_lower", "inner_br", "floor", "floor", "inner_tl", "br_lower", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "floor", "inner_tl", "br_lower", "empty", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "inner_tl", "br_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty", "empty", "empty")
    )

    val easyGame3 = gameMapFromTileIds(
        id = "Functions and Loops",
        startX = 1,
        startY = 8,
        goalX = 8,
        goalY = 1,
        tileIds = easy3Tiles
    )

    // -------- HARD Levels (handmade hard levels) --------
    val hard1Tiles = listOf(
        listOf("water", "water", "water", "water", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("bl_lower", "bottom_lower", "left_lower", "floor", "floor", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("water", "water", "left_lower", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("water", "tl_lower", "inner_br", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("tl_lower", "inner_br", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water")
    )

    val hardGame1 = gameMapFromTileIds(
        id = "A good start",
        startX = 2,
        startY = 10,
        goalX = 10,
        goalY = 4,
        tileIds = hard1Tiles
    )

    val hard2Tiles = listOf(
        listOf("tl_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "tr_upper"),
        listOf("left_upper", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_upper"),
        listOf("left_upper", "pit_top", "pit_top", "pit_top", "pit_top", "pit_top", "inner_wall", "inner_wall", "inner_wall", "floor", "right_upper"),
        listOf("left_upper", "pit_bottom", "pit_bottom", "pit_bottom", "pit_bottom", "pit_bottom", "inner_wall", "floor", "inner_wall", "floor", "right_upper"),
        listOf("left_upper", "floor", "floor", "button_unpressed", "floor", "floor", "inner_wall", "floor", "inner_wall", "floor", "right_upper"),
        listOf("left_upper", "floor", "floor", "floor", "floor", "floor", "inner_wall", "floor", "inner_wall", "floor", "right_upper"),
        listOf("left_upper", "inner_wall", "inner_wall", "inner_wall", "inner_wall", "monster", "inner_wall", "floor", "inner_wall", "monster", "right_upper"),
        listOf("left_upper", "floor", "floor", "monster", "floor", "floor", "water", "floor", "inner_wall", "monster", "right_upper"),
        listOf("left_upper", "floor", "floor", "monster", "floor", "floor", "water", "floor", "inner_wall", "monster", "right_upper"),
        listOf("left_upper", "floor", "floor", "monster", "floor", "floor", "water", "floor", "floor", "floor", "right_upper"),
        listOf("bl_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "br_upper")
    )

    val hardGame2 = gameMapFromTileIds(
        id = "if here, if there, if everywhere",
        startX = 1,
        startY = 8,
        goalX = 7,
        goalY = 3,
        tileIds = hard2Tiles
    )

    val hard3Tiles = listOf(
        listOf("tl_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "tr_upper"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "right_upper"),
        listOf("water", "floor", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "floor", "floor", "inner_wall", "monster", "monster", "floor", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "inner_wall", "floor", "monster", "monster", "monster", "floor", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "floor", "inner_wall", "monster", "monster", "monster", "floor", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "floor", "floor", "inner_wall", "inner_wall", "inner_wall", "floor", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "pit_top", "pit_top", "pit_top", "inner_wall", "floor", "floor", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "inner_wall", "pit_bottom", "pit_bottom", "pit_bottom", "floor", "floor", "inner_wall", "water", "right_upper"),
        listOf("water", "monster", "monster", "monster", "water", "floor", "floor", "floor", "floor", "inner_wall", "inner_wall", "water", "right_upper"),
        listOf("water", "floor", "floor", "floor", "water", "floor", "floor", "inner_wall", "inner_wall", "inner_wall", "inner_wall", "water", "right_upper"),
        listOf("water", "floor", "floor", "floor", "water", "floor", "floor", "floor", "floor", "button_unpressed", "inner_wall", "water", "right_upper"),
        listOf("bl_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "br_upper")
    )

    val hardGame3 = gameMapFromTileIds(
        id = "One man Army",
        startX = 2,
        startY = 10,
        goalX = 5,
        goalY = 5,
        tileIds = hard3Tiles
    )


    // -------- EASY LEVEL COLLECTION --------
    val easyLevel = Level(
        id = "easy_level",
        name = "Easy Dungeons",
        difficulty = Difficulty.EASY,
        games = listOf(easyGame1,easyGame2,easyGame3)
    )

    // -------- HARD LEVEL (placeholder for now) --------
    val hardLevel = Level(
        id = "hard_level",
        name = "Hard Dungeons",
        difficulty = Difficulty.HARD,
        games = listOf(hardGame1, hardGame2,hardGame3)   // later you can add baked hardGame1, hardGame2, etc.
    )

    return listOf(easyLevel, hardLevel)
}