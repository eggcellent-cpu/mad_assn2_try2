package com.it2161.dit233000D.movieviewer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit233000D.movieviewer.data.movie.FavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FavoriteMovieViewModel(private val repository: MovieRepository, userProfile: UserProfile?) : ViewModel() {

    private val _favoriteMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val favoriteMovies: StateFlow<List<MovieItem>> get() = _favoriteMovies
    val currentUser = userProfile ?: UserProfile(0, "")

    // Load favorite movies for the current user
    fun loadFavoriteMovies() {
        viewModelScope.launch {
            repository.getFavoriteMoviesWithDetails(currentUser.id).collect { movies ->
                _favoriteMovies.value = movies
            }
        }
    }

    // Add a movie to favorites for the current user
    fun addFavoriteMovie(movie: MovieItem) {
        viewModelScope.launch {
            val favoriteMovie = FavoriteMovieItem(
                userId = currentUser.id,
                userName = currentUser.userName,
                movieId = movie.id
            )
            repository.addFavoriteMovie(favoriteMovie)
            loadFavoriteMovies() // reload favorites after adding
        }
    }

    // Remove a movie from favorites for the current user
    fun removeFavoriteMovie(movieId: Long) {
        viewModelScope.launch {
            repository.removeFavoriteMovie(currentUser.id, movieId)
            loadFavoriteMovies() // reload after removal
        }
    }

    // Check if a movie is favorited by the current user
    suspend fun isMovieFavorited(movieId: Long): Boolean {
        val favoriteMovies = repository.getFavoriteMovies(currentUser.id).firstOrNull() ?: emptyList()
        Log.d("MovieViewerApp", "Checking if movie $movieId is favorited by user ${currentUser.id}")
        return favoriteMovies.any { it.movieId == movieId }
    }
}
