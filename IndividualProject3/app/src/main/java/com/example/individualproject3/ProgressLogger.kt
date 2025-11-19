package com.example.individualproject3

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Simple logger that appends one line per game run to a CSV file.
 * Format: timestamp,levelId,gameId,resultCode,commandsCount
 */
class ProgressLogger(private val context: Context) {

    private val fileName = "progress_log.csv"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun logAttempt(
        levelId: String,
        gameId: String,
        resultCode: String,
        commandsCount: Int
    ) {
        val timestamp = dateFormat.format(Date())
        val line = "$timestamp,$levelId,$gameId,$resultCode,$commandsCount\n"

        context.openFileOutput(fileName, Context.MODE_APPEND).use { fos ->
            fos.write(line.toByteArray())
        }
    }
}