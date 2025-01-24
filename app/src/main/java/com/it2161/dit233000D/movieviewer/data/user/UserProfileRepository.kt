package com.it2161.dit233000D.movieviewer.data.user

class UserProfileRepository(private val userDao: UserProfileDao) {
    suspend fun insertUser(user: UserProfile) = userDao.insertUser(user)
    suspend fun getUserByUserName(userName: String) = userDao.getUserByUserName(userName)
    suspend fun deleteUser(user: UserProfile) = userDao.deleteUser(user)
    suspend fun clearAllUsers() = userDao.clearAllUsers()
}
