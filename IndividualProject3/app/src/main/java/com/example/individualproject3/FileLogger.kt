package com.example.individualproject3


import android.content.Context

class FileLogger(private val context: Context) {

    private val logFileName = "progress_log.csv"

    fun logAttempt(attempt: AttemptLog) {
        val line = buildString {
            append(attempt.childId).append(',')
            append(attempt.levelId).append(',')
            append(attempt.gameId).append(',')
            append(attempt.success).append(',')
            append(attempt.movesUsed).append(',')
            append(attempt.timestampMillis).append('\n')
        }

        context.openFileOutput(logFileName, Context.MODE_APPEND).use { fos ->
            fos.write(line.toByteArray())
        }
    }
}