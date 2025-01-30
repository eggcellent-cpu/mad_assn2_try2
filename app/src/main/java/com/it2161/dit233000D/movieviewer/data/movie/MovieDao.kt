package com.it2161.dit233000D.movieviewer.data.movie

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieItem>)

    // insert a single movie into the database (replace if conflict occurs)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieItem)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Long): MovieItem?

    // get all movies from the database
    @Query("SELECT * FROM movies")
    suspend fun getAllMovies(): List<MovieItem>

    // Insert a favorite movie (assuming FavoriteMovieItem is a distinct entity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteMovie(movie: FavoriteMovieItem)

    // delete favorite movie (specific user)
    @Query("DELETE FROM favorite_movies WHERE favoriteId = :movieId AND userId = :userId")
    suspend fun deleteFavoriteMovie(movieId: Long, userId: Int)

    // Get all favorite movies as a Flow
    @Query("SELECT * FROM favorite_movies") // Ensure this table exists or modify to match your schema
    fun getAllFavoriteMovies(): Flow<List<FavoriteMovieItem>>

    // Get all favorite movies for a specific user
    @Query("SELECT * FROM favorite_movies WHERE userId = :userId")
    fun getFavoriteMovies(userId: Int): Flow<List<FavoriteMovieItem>>

    @Query("SELECT * FROM favorite_movies WHERE favoriteId = :movieId AND userId = :userId")
    suspend fun getFavoriteMovieById(movieId: Long, userId: Int): FavoriteMovieItem?

    @Query("""
        SELECT movies.* FROM movies
        INNER JOIN favorite_movies ON movies.id = favorite_movies.movieId
        WHERE favorite_movies.userId = :userId
    """)
    fun getFavoriteMoviesWithDetails(userId: Int): Flow<List<MovieItem>>
}

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<Review>)

    // Get reviews for a movie by movieId as String
    @Query("SELECT * FROM reviews WHERE movie_id = :movieId")
    suspend fun getReviewsForMovie(movieId: Long): List<Review>
}
