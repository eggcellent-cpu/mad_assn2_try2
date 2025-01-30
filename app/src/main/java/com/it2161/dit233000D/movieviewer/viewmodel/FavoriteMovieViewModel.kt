package com.it2161.dit233000D.movieviewer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit233000D.movieviewer.data.movie.FavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.movie.toFavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FavoriteMovieViewModel(private val repository: MovieRepository, userProfile: UserProfile?) : ViewModel() {

    private val _favoriteMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    val favoriteMovies: StateFlow<List<MovieItem>> get() = _favoriteMovies
    val currentUser = userProfile ?: UserProfile(0, "")

    // Load favorite movies for a specific user
    fun loadFavoriteMovies(userId: Int) {
        viewModelScope.launch {
            repository.getFavoriteMoviesWithDetails(userId).collect { movies ->
                _favoriteMovies.value = movies
            }
        }
    }


    // Add a movie to favorites
    fun addFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.addFavoriteMovie(movie)
            loadFavoriteMovies(movie.userId) // Reload favorites after adding
        }
    }

    // Remove a movie from favorites
    fun removeFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.removeFavoriteMovie(movie)
            loadFavoriteMovies(movie.userId) // Reload after removal
        }
    }

    // Toggle favorite status for a movie
    fun toggleFavorite(favoriteMovie: FavoriteMovieItem, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                repository.addFavoriteMovie(favoriteMovie)
            } else {
                repository.removeFavoriteMovie(favoriteMovie)
            }
            loadFavoriteMovies(favoriteMovie.userId) // Reload favorites after toggling
        }
    }

    suspend fun isMovieFavorited(movieId: Long, userId: Int): Boolean {
        val favoriteMovies = repository.getFavoriteMovies(userId).firstOrNull() ?: emptyList()
        Log.d("MovieViewerApp", "Checking if movie $movieId is favorited by user $userId")
        return favoriteMovies.any { it.favoriteId == movieId }
    }
}
