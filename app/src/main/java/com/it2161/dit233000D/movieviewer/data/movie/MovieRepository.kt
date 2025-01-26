package com.it2161.dit233000D.movieviewer.data.movie

import android.content.Context
import android.util.Log
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieRepository private constructor(context: Context) {

    // Initialize the database and DAO
    private val database: MovieDatabase = MovieDatabase.getDatabase(context)
    private val movieDao: MovieDao = database.movieDao()

    // Fetch all movies from API or database (fallback on failure)
    suspend fun getAllMovies(context: Context, apiKey: String, value: String): List<MovieItem> {
        return withContext(Dispatchers.IO) {
            val allMovies = mutableListOf<MovieItem>() // To store movies from all categories
            if (isNetworkAvailable(context)) {
                try {
                    // Fetch movies from all categories
                    val categories = listOf("popular", "toprated", "nowplaying", "upcoming")
                    for (category in categories) {
                        val movieResponse = when (category) {
                            "popular" -> RetrofitInstance.getApiService().getPopularMovies(apiKey)
                            "toprated" -> RetrofitInstance.getApiService().getTopRatedMovies(apiKey)
                            "nowplaying" -> RetrofitInstance.getApiService().getNowPlayingMovies(apiKey)
                            "upcoming" -> RetrofitInstance.getApiService().getUpcomingMovies(apiKey)
                            else -> null
                        }
                        movieResponse?.results?.let { allMovies.addAll(it) }
                    }
                    movieDao.insertMovies(allMovies) // Save all fetched movies to the database
                } catch (e: IOException) {
                    Log.e("MovieRepository", "Network error: ${e.message}")
                } catch (e: Exception) {
                    Log.e("MovieRepository", "Error fetching movies: ${e.message}")
                }
            }

            // Fallback to database if offline or API fetch fails
            if (allMovies.isEmpty()) {
                return@withContext movieDao.getAllMovies()
            } else {
                return@withContext allMovies
            }
        }
    }


    // Get the list of favorite movies from local storage as a Flow
    fun getFavoriteMovies(userName: String): Flow<List<FavoriteMovieItem>> {
        return movieDao.getFavoriteMovies(userName)
    }

    // Add a movie to favorites
    suspend fun addFavoriteMovie(movie: FavoriteMovieItem) {
        movieDao.insertFavoriteMovie(movie) // Insert into the general movie table
    }

    // Remove a movie from favorites
    suspend fun removeFavoriteMovie(movie: FavoriteMovieItem) {
        movieDao.deleteFavoriteMovie(movie) // Ensure this method is correct and corresponds to your table
    }

    // Get a movie by its ID
    suspend fun getMovieById(movieId: Long): MovieItem? {
        Log.d("MovieRepository", "Fetching movie with id: $movieId")
        return movieDao.getMovieById(movieId)
    }

    companion object {
        @Volatile
        private var INSTANCE: MovieRepository? = null

        fun getInstance(context: Context): MovieRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = MovieRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}