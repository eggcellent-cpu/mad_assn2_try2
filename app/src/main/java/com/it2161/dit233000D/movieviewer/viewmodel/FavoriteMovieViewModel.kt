package com.it2161.dit233000D.movieviewer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it2161.dit233000D.movieviewer.data.movie.FavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteMovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _favoriteMovies = MutableStateFlow<List<FavoriteMovieItem>>(emptyList())
    val favoriteMovies: StateFlow<List<FavoriteMovieItem>> get() = _favoriteMovies

    // Load favorite movies for a specific user
    fun loadFavoriteMovies(userName: String) {
        viewModelScope.launch {
            repository.getFavoriteMovies(userName).collect { movies ->
                _favoriteMovies.value = movies
            }
        }
    }

    // Add a movie to favorites
    fun addFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.addFavoriteMovie(movie)
        }
    }

    // Remove a movie from favorites
    fun removeFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.removeFavoriteMovie(movie)
        }
    }

    fun toggleFavorite(movie: FavoriteMovieItem, is_favorite: Boolean) {
        viewModelScope.launch {
            if (is_favorite) {
                addFavoriteMovie(movie)
            } else {
                removeFavoriteMovie(movie)
            }
        }
    }

    fun loadMoviesWithFavorites(userName: String, allMovies: List<FavoriteMovieItem>) {
        viewModelScope.launch {
            repository.getFavoriteMovies(userName).collect { favorites ->
                val updatedMovies = allMovies.map { movie ->
                    movie.copy(is_favorite = favorites.any { it.id == movie.id })
                }
                _favoriteMovies.value = updatedMovies
            }
        }
    }

}
