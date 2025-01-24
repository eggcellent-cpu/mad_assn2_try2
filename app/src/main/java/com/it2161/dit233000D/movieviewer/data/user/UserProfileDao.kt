package com.it2161.dit233000D.movieviewer.data.user

import android.util.Log
import androidx.room.*

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE userName = :userName LIMIT 1")
    suspend fun getUserByUserName(userName: String): UserProfile?

    @Delete
    suspend fun deleteUser(user: UserProfile)

    @Query("DELETE FROM user_profiles")
    suspend fun clearAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfile(id: Int): UserProfile?

}
