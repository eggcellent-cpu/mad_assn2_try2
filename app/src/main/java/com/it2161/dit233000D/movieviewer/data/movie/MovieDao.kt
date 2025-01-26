package com.it2161.dit233000D.movieviewer.data.movie

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    // Insert a list of movies into the database (replace if conflict occurs)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieItem>)

    // Insert a single movie into the database (replace if conflict occurs)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieItem)

    // Get a movie by its ID (string)
    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Long): MovieItem?

    // Get all movies from the database
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<MovieItem>

    // Insert a favorite movie (assuming FavoriteMovieItem is a distinct entity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMovie(movie: FavoriteMovieItem)

    // Delete a favorite movie (assuming FavoriteMovieItem is a distinct entity)
    @Delete
    suspend fun deleteFavoriteMovie(movie: FavoriteMovieItem)

    // Get all favorite movies as a Flow
    @Query("SELECT * FROM favorite_movies") // Ensure this table exists or modify to match your schema
    fun getAllFavoriteMovies(): Flow<List<MovieItem>>

    // Get all favorite movies for a specific user
    @Query("SELECT * FROM favorite_movies WHERE userName = :userName")
    fun getFavoriteMovies(userName: String): Flow<List<FavoriteMovieItem>>
}

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    // Get reviews for a movie by movieId as String
    @Query("SELECT * FROM reviews WHERE movie_id = :movieId")
    suspend fun getReviewsForMovie(movieId: Long): List<Review>
}
