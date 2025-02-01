package com.it2161.dit233000D.movieviewer.data.movie

import android.content.Context
import android.util.Log
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieRepository private constructor(context: Context) {
    private val database: MovieDatabase = MovieDatabase.getDatabase(context)
    private val movieDao: MovieDao = database.movieDao()

    // Fetch all movies from API or database (fallback on failure)
    suspend fun getAllMovies(context: Context, apiKey: String): List<MovieItem> {
        return withContext(Dispatchers.IO) {
            val allMovies = mutableListOf<MovieItem>()
            if (isNetworkAvailable(context)) {
                try {
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
                    movieDao.insertMovies(allMovies) // save all fetched movies to the database
                } catch (e: IOException) {
                    Log.e("MovieRepository", "Network error: ${e.message}")
                } catch (e: Exception) {
                    Log.e("MovieRepository", "Error fetching movies: ${e.message}")
                }
            }

            // Fallback to database if offline or API fetch fails
            if (allMovies.isEmpty()) {
                return@withContext movieDao.getAllDbMovies()
            } else {
                return@withContext allMovies
            }
        }
    }

    fun getFavoriteMovies(userId: Int): Flow<List<FavoriteMovieItem>> {
        return movieDao.getFavoriteMovies(userId).map { favorites ->
            Log.d("MovieViewerApp", "Fetched favorites for user $userId: ${favorites.size}")
            favorites
        }
    }

    // Add a movie to favorites
    suspend fun addFavoriteMovie(movie: FavoriteMovieItem) {
        movieDao.insertFavoriteMovie(movie) // database insert
    }

    // Remove a movie from favorites
    suspend fun removeFavoriteMovie(userId: Int, movieId: Long) {
        movieDao.deleteFavoriteMovie(userId, movieId)
    }

    fun getFavoriteMoviesWithDetails(userId: Int): Flow<List<MovieItem>> {
        return movieDao.getFavoriteMoviesWithDetails(userId)
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