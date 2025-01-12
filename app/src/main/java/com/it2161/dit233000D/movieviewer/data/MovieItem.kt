package com.it2161.dit233000D.movieviewer.data

data class MovieItem(
    val adult: Boolean = true,
    val backdrop_path: String?,
    val belongs_to_collection: BelongsToCollection? = null, // Updated here
    val budget: Int = 0,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val imdb_id: String?,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val production_companies: List<ProductionCompany>,
    val production_countries: List<ProductionCountry>,
    val release_date: String,
    val revenue: Int = 0,
    val runtime: Int = 0,
    val spoken_languages: List<SpokenLanguage>,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val vote_average: Double = 0.0,
    val vote_count: Int = 0
)

data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int = 0,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)

data class ProductionCountry(
    val iso_3166_1: String,
    val name: String
)

data class SpokenLanguage(
    val english_name: String,
    val iso_639_1: String,
    val name: String
)

data class MovieResponse(
    val results: List<MovieItem>
)

data class BelongsToCollection(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?
)

// ReviewResponse and Review Data Classes
data class ReviewResponse(
    val results: List<Review>
)

data class Review(
    val author: String,
    val content: String
)
