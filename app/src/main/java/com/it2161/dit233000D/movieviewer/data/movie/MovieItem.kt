package com.it2161.dit233000D.movieviewer.data.movie

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "favorite_movies")
data class FavoriteMovieItem(
    @PrimaryKey(autoGenerate = true) val favoriteId: Long = 0,
    val movieId: Long,

    // specific user
    val userName: String,
    val userId: Int
)

@Entity(tableName = "movies")
data class MovieItem(
    @PrimaryKey val id: Long,
    val adult: Boolean = false,
    val backdrop_path: String? = null,
    val budget: Int? = 0,
    @TypeConverters(Converters::class)
    val genres: List<Genre>? = emptyList(),
    val homepage: String? = "Unavailable",
    val original_language: String,
    val original_title: String,
    val overview: String? = "Unavailable",
    val popularity: Double = 0.0,
    val poster_path: String? = null,
    val release_date: String = "Unavailable",
    val revenue: Int = 0,
    val runtime: Int = 0,
    val title: String,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0,
    val is_favorite: Boolean = false
)


data class Genre(
    val id: Int,
    val name: String
)

data class MovieResponse(
    val results: List<MovieItem>
)

data class ReviewResponse(
    val results: List<Review>
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "movie_id") val movieId: Long,
    val author: String,
    val content: String
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromGenreList(value: List<Genre>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toGenreList(value: String?): List<Genre>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Genre>>() {}.type
        return gson.fromJson(value, listType)
    }
}

// convert MovieItem to FavoriteMovieItem
fun MovieItem.toFavoriteMovieItem(userName: String, userId: Int): FavoriteMovieItem {
    return FavoriteMovieItem(
        favoriteId = this.id,
        movieId = this.id,
        userId = userId,
        userName = userName,
    )
}
