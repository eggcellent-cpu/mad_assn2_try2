package com.it2161.dit233000D.movieviewer.data.user

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class],
    version = 2,
    exportSchema = false
) // Increment version number

abstract class UserDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            Log.d("MovieViewerApp", "Initializing User Database...")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                )
                    .build()
                INSTANCE = instance
                Log.d("MovieViewerApp", "User Database initialized successfully!")
                instance
            }
        }
    }
}
