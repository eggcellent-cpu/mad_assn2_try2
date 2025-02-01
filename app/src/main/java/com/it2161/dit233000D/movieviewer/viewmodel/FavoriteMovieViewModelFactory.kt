package com.it2161.dit233000D.movieviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserProfile

class FavoriteMovieViewModelFactory(
    private val repository: MovieRepository,
    private val userProfile: UserProfile?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteMovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteMovieViewModel(repository, userProfile) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

