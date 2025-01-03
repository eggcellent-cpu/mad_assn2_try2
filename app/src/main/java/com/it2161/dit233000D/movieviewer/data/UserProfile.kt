package com.it2161.dit233000D.movieviewer.data

import com.it2161.dit233000D.movieviewer.R

data class UserProfile(
    val userName: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val updates: Boolean = false,
    val avatar: Int = R.drawable.avatar_1,
    val yob: String = "",

    val preferredName: String = ""
)