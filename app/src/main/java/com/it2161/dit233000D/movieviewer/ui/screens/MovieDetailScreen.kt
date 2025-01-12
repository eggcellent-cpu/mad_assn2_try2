package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.MovieItem
import com.it2161.dit233000D.movieviewer.data.Review
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(movieId: Int, navController: NavController) {
    val movieDetail = remember { mutableStateOf<MovieItem?>(null) }
    val reviews = remember { mutableStateListOf<Review>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAllReviews by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    val reviewsPerPage = 5
    val totalPages = (reviews.size + reviewsPerPage - 1) / reviewsPerPage

    // Function to clean HTML tags from review content
    fun cleanHtmlContent(content: String): String {
        return Jsoup.parse(content).text() // Using Jsoup to strip HTML tags and keep only the text
    }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Movie Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { padding ->
            if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                movieDetail.value?.let { movie ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Movie details
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Release Date: ${movie.release_date}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Runtime: ${movie.runtime} min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Vote Average: ${movie.vote_average}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Vote Count: ${movie.vote_count}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Revenue: $${movie.revenue}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Original Language: ${movie.original_language}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Genres: ${movie.genres.joinToString(", ") { it.name }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Overview:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = movie.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Reviews section
                        Text(
                            text = "Reviews:",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (reviews.isNotEmpty()) {
                            // Display first review
                            val firstReview = reviews.first()
                            Text(text = "Author: ${firstReview.author}")
                            Text(
                                text = cleanHtmlContent(firstReview.content),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3
                            )

                            if (showAllReviews) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(reviews.chunked(reviewsPerPage).getOrNull(currentPage - 1) ?: emptyList()) { review ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            elevation = CardDefaults.elevatedCardElevation()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "Author: ${review.author}",
                                                    style = MaterialTheme.typography.labelLarge
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = cleanHtmlContent(review.content),
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }

                                // Pagination controls
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = { if (currentPage > 1) currentPage-- },
                                        enabled = currentPage > 1
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.arrow_back),
                                            contentDescription = "Previous"
                                        )
                                    }
                                    Text("${currentPage} / $totalPages", style = MaterialTheme.typography.bodyMedium)
                                    IconButton(
                                        onClick = { if (currentPage < totalPages) currentPage++ },
                                        enabled = currentPage < totalPages
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.arrow_forward),
                                            contentDescription = "Next"
                                        )
                                    }
                                }
                            }

                            // Show/Hide All Reviews button
                            TextButton(onClick = { showAllReviews = !showAllReviews }) {
                                Text(text = if (showAllReviews) "Hide All Reviews" else "Read All Reviews")
                            }
                        }
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading movie details...")
                    }
                }
            }
        }
    )
}
