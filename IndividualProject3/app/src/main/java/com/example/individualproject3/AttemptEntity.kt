package com.example.individualproject3.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempts")
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String,
    val childName: String,
    val levelId: String,
    val gameId: String,
    val resultCode: String,
    val commandsCount: Int
)
