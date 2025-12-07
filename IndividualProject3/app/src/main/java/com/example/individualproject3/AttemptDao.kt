package com.example.individualproject3.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttemptDao {

    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Query("SELECT * FROM attempts ORDER BY id DESC")
    suspend fun getAllAttempts(): List<AttemptEntity>

    // 🔹 Clear attempts for a single child
    @Query("DELETE FROM attempts WHERE childName = :childName")
    suspend fun deleteAttemptsForChild(childName: String)

    // (Optional) clear everything – handy for debugging if you ever want it
    @Query("DELETE FROM attempts")
    suspend fun deleteAllAttempts()
}


