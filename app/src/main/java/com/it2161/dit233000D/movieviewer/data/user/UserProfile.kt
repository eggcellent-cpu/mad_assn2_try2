package com.it2161.dit233000D.movieviewer.data.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.it2161.dit233000D.movieviewer.R

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Add a primary key for Room
    val userName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val updates: Boolean = false,
    val avatar: Int = R.drawable.avatar_1,
    val yob: String = "",
    val preferredName: String = ""
)
