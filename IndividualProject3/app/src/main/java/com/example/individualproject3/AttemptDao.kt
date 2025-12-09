package com.example.individualproject3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * DAO for reading and writing level attempt data in the Room database.
 *
 * This is used by:
 * - ProgressLogger: when a run finishes, it inserts a new AttemptEntity.
 * - Parent screens: to read all attempts, filter by child, and show stats.
 */
@Dao
interface AttemptDao {

    /**
     * Insert a single attempt record into the database.
     *
     * Called whenever a run is logged (success or failure).
     */
    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    /**
     * Get every attempt in the database, newest first.
     *
     * Used for building:
     * - Overall stats
     * - Per-child stats
     * - Attempt history lists in the parent screen.
     */
    @Query("SELECT * FROM attempts ORDER BY id DESC")
    suspend fun getAllAttempts(): List<AttemptEntity>

    /**
     * Delete all attempts for a single child.
     *
     * Used by the "Clear stats" action when a specific child is filtered.
     */
    @Query("DELETE FROM attempts WHERE childName = :childName")
    suspend fun deleteAttemptsForChild(childName: String)

    /**
     * Delete *all* attempts in the table.
     *
     * Currently optional: this is mainly useful for debugging or if you
     * later add a "Clear all stats" button in the parent controls.
     */
    @Query("DELETE FROM attempts")
    suspend fun deleteAllAttempts()
}
