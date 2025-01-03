package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.MovieItem
import com.it2161.dit233000D.movieviewer.data.Review
import kotlinx.coroutines.launch

@Composable
fun MovieDetailScreen(movieId: Int, navController: NavController) {
    val movieDetail = remember { mutableStateOf<MovieItem?>(null) }
    val reviews = remember { mutableStateListOf<Review>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAllReviews by remember { mutableStateOf(false) }

    LaunchedEffect(movieId) {
        scope.launch {
            val apiKey = try {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                ).metaData.getString("com.movieviewer.API_KEY.233000D")
            } catch (e: Exception) {
                errorMessage = "Failed to retrieve API key: ${e.message}"
                return@launch
            }

            try {
                // Fetch movie details
                val movieDetailResponse = RetrofitInstance.getApiService().getMovieDetails(movieId, apiKey!!)
                movieDetail.value = movieDetailResponse

                // Fetch reviews for the movie
                val reviewsResponse = RetrofitInstance.getApiService().getMovieReviews(movieId, apiKey!!)
                reviews.addAll(reviewsResponse.results)
            } catch (e: Exception) {
                errorMessage = "Failed to fetch movie details: ${e.message}"
            }
        }
    }

    if (errorMessage != null) {
        Text(
            text = errorMessage!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    } else {
        movieDetail.value?.let { movie ->
            Column(modifier = Modifier.padding(16.dp)) {
                // Movie details
                Text(text = movie.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Release Date: ${movie.release_date}")
                Text(text = "Runtime: ${movie.runtime} min")
                Text(text = "Vote Average: ${movie.vote_average}")
                Text(text = "Vote Count: ${movie.vote_count}")
                Text(text = "Revenue: $${movie.revenue}")
                Text(text = "Original Language: ${movie.original_language}")
                Text(text = "Genres: ${movie.genres.joinToString(", ") { it.name }}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Overview: ${movie.overview}")

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews section
                Text(text = "Reviews:", style = MaterialTheme.typography.bodyMedium)
                if (reviews.isNotEmpty()) {
                    val firstReview = reviews.first()
                    Text(text = "Author: ${firstReview.author}")
                    Text(text = firstReview.content, style = MaterialTheme.typography.bodySmall, maxLines = 3)

                    // Show/Hide All Reviews
                    TextButton(onClick = { showAllReviews = !showAllReviews }) {
                        Text(text = if (showAllReviews) "Hide All Reviews" else "Read All Reviews")
                    }

                    if (showAllReviews) {
                        LazyColumn {
                            items(reviews) { review ->
                                Card(modifier = Modifier.padding(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = "Author: ${review.author}", style = MaterialTheme.typography.labelMedium)
                                        Text(text = review.content, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("No reviews available.")
                }
            }
        } ?: run {
            Text("Loading movie details...")
        }
    }
}
