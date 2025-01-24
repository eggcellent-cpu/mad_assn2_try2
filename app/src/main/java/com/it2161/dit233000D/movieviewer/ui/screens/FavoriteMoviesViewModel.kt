import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.dit233000D.movieviewer.data.movie.FavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteMoviesViewModel(private val repository: MovieRepository) : ViewModel() {

    // MutableStateFlow for managing favorite movies
    private val _favoriteMovies = MutableStateFlow<List<MovieItem>>(emptyList())
    companion object {
        fun provideFactory(
            repository: MovieRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoriteMoviesViewModel(repository) as T
            }
        }
    }
    // Exposed StateFlow for observing favorite movies
    val favoriteMovies: StateFlow<List<MovieItem>> = _favoriteMovies

    suspend fun getMovieById(movieId: Long): MovieItem? {
        return repository.getMovieById(movieId)  // Assuming you have a repository method for this
    }

    init {
        // Initialize the favorite movies from the repository
        loadFavoriteMovies()
    }

    // Load favorite movies from repository
    private fun loadFavoriteMovies() {
        viewModelScope.launch {
            repository.getFavoriteMovies().collect { movies ->
                _favoriteMovies.value = movies
            }
        }
    }

    // Add a movie to favorites
    fun addFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.addFavoriteMovie(movie)
            loadFavoriteMovies()  // Refresh the list after adding
        }
    }

    // Remove a movie from favorites
    fun removeFavoriteMovie(movie: FavoriteMovieItem) {
        viewModelScope.launch {
            repository.removeFavoriteMovie(movie)
            loadFavoriteMovies()  // Refresh the list after removing
        }
    }

    fun isMovieFavorited(movieId: Long): Boolean {
        return favoriteMovies.value.any { it.id == movieId }
    }
}
