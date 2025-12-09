package com.example.individualproject3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for the app.
 *
 * This database stores **AttemptEntity** objects, which represent:
 *  - Child name
 *  - Level played
 *  - Outcome (success / failure type)
 *  - Timestamp
 *
 * This data is used by the Parent Stats screen to show:
 *  - Total attempts
 *  - Success/failure counts
 *  - Filtered attempts per child
 *  - Attempt history
 *
 * Only one DAO exists so far: AttemptDao.
 */
@Database(
    entities = [AttemptEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {

    /**
     * DAO giving access to insert, query, and delete attempt records.
     */
    abstract fun attemptDao(): AttemptDao

    companion object {
        /**
         * Singleton instance of the database.
         *
         * @Volatile ensures that reads/writes to INSTANCE are always consistent
         * across multiple threads. This is important because RoomDB creation
         * must never be duplicated — multiple databases would cause crashes.
         */
        @Volatile
        private var INSTANCE: GameDatabase? = null

        /**
         * Returns the single shared GameDatabase instance.
         *
         * Uses double-checked locking:
         *  1. Check if INSTANCE is already created.
         *  2. If not, synchronize to ensure only one thread creates it.
         *
         * The database lives for the entire app lifespan and should never
         * be manually destroyed or recreated outside of migrations.
         */
        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                // If still null inside synchronized, build the DB.
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database" // filename stored on the device
                ).build().also { INSTANCE = it }
            }
        }
    }
}
