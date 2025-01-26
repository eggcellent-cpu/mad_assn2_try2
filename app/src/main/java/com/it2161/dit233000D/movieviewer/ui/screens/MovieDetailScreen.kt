package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.movie.MovieDatabase
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.movie.Review
import com.it2161.dit233000D.movieviewer.data.movie.toFavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.utils.isNetworkAvailable
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModel
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModelFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Any,
    navController: NavController,
    repository: MovieRepository,
    userProfile: UserProfile?
) {
    val movieDetail = remember { mutableStateOf<MovieItem?>(null) }
    val reviews = remember { mutableStateListOf<Review>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var offlineMessage by remember { mutableStateOf<String?>(null) }

    var showAllReviews by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(1) }
    val reviewsPerPage = 5
    val totalPages = (reviews.size + reviewsPerPage - 1) / reviewsPerPage

    val movieDatabase = MovieDatabase.getDatabase(context)
    var similarMovies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }

    val currentUser = userProfile ?: UserProfile(0, "")
    val isFavorite = remember { mutableStateOf(movieDetail.value?.is_favorite ?: false) }

    val viewModel: FavoriteMovieViewModel = viewModel(
        factory = FavoriteMovieViewModelFactory(repository)
    )

    // Function to clean HTML tags from review content
    fun cleanHtmlContent(content: String): String {
        return Jsoup.parse(content).text()
    }

    // Safe conversion of movieId to Long
    fun safeConvertToLong(id: Any): Long? {
        return when (id) {
            is Long -> id
            is Int -> id.toLong()
            is String -> {
                // Attempt direct conversion first
                id.toLongOrNull() ?: run {
                    Log.e("MovieViewerApp", "Invalid movie ID: $id")
                    null
                }
            }

            else -> {
                Log.e("MovieViewerApp", "Unsupported movie ID type: ${id::class.java}")
                null
            }
        }
    }

    LaunchedEffect(movieId, currentUser.userName) {
        scope.launch {
            // Validate and convert movie ID
            val movieIdLong = safeConvertToLong(movieId)
            if (movieIdLong == null) {
                errorMessage = "Invalid movie ID format. Unable to load details."
                return@launch
            }

            val favoriteMovies = repository.getFavoriteMovies(currentUser.userName).firstOrNull() ?: emptyList()
            isFavorite.value = favoriteMovies.any { it.id == movieIdLong }

            val apiKey = try {
                context.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                ).metaData.getString("com.movieviewer.API_KEY.233000D") ?: run {
                    errorMessage = "API key not found."
                    return@launch
                }
            } catch (e: Exception) {
                errorMessage = "Failed to retrieve API key: ${e.message}"
                return@launch
            }

            val isOnline = isNetworkAvailable(context)
            Log.d("MovieViewerApp", "Movie ID: $movieIdLong")
            Log.d("MovieViewerApp", "Is Online: $isOnline")

            if (isOnline) {
                try {
                    val movieDetailResponse =
                        RetrofitInstance.getApiService().getMovieDetails(movieIdLong, apiKey)
                    val reviewsResponse =
                        RetrofitInstance.getApiService().getMovieReviews(movieIdLong, apiKey)

                    // Update Room database
                    movieDatabase.movieDao().insert(movieDetailResponse)
                    movieDatabase.reviewDao().insertReviews(reviewsResponse.results)

                    movieDetail.value = movieDetailResponse
                    reviews.addAll(reviewsResponse.results)
                } catch (e: Exception) {
                    Log.e("MovieViewerApp", "Error loading movie details: ${e.localizedMessage}")
                    errorMessage = "Error loading movie details."
                }
            } else {
                // Load from Room database
                val offlineMovieDetail = movieDatabase.movieDao().getMovieById(movieIdLong)
                val offlineReviews = movieDatabase.reviewDao().getReviewsForMovie(movieIdLong)

                if (offlineMovieDetail != null) {
                    movieDetail.value = offlineMovieDetail
                    reviews.addAll(offlineReviews)
                } else {
                    errorMessage = "Movie details not available offline."
                }
                offlineMessage = "This feature is unavailable offline."
            }

            // Fetch Similar Movies
            if (isOnline && movieIdLong != null) {
                try {
                    val similarMoviesResponse = RetrofitInstance.getApiService().getSimilarMovies(
                        movieId = movieIdLong.toInt(),
                        apiKey = apiKey
                    )
                    similarMovies = similarMoviesResponse.results
                } catch (e: Exception) {
                    Log.e("MovieViewerApp", "Error loading similar movies: ${e.localizedMessage}")
                }
            }
        }
    }
    offlineMessage?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
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
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            movieDetail.value?.let { movie ->
                                val favoriteMovie = movie.toFavoriteMovieItem(currentUser.userName)

                                if (isFavorite.value) {
                                    viewModel.removeFavoriteMovie(favoriteMovie) // Only pass the movie
                                } else {
                                    viewModel.addFavoriteMovie(favoriteMovie) // Only pass the movie
                                }
                                isFavorite.value = !isFavorite.value
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(
                                if (isFavorite.value) R.drawable.heart_check else R.drawable.favorite
                            ),
                            contentDescription = "Favorite",
                            tint = if (isFavorite.value) Color.Red else Color.Black // Make heart red when favorited
                        )
                    }
                }
            )
        },
        content = { padding ->
            // Proper use of LazyColumn modifier
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                item {
                    // Show error message if any
                    errorMessage?.let {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    movieDetail.value?.let { movie ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                // Left side: Movie image and rating
                                Column(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .padding(end = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val imageUrl = movie.poster_path
                                    if (imageUrl.isNullOrEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(250.dp)
                                                .background(Color.Gray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No image available",
                                                color = Color.White
                                            )
                                        }
                                    } else {
                                        Image(
                                            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                                            contentDescription = movie.title,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(250.dp) // Adjust height as needed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Rating: ${movie.vote_average ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                // Right side: Movie details
                                Column(
                                    modifier = Modifier
                                        .weight(2f)
                                        .fillMaxHeight()
                                ) {
                                    Text(
                                        text = movie.title ?: "No title available",
                                        style = MaterialTheme.typography.headlineLarge,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Release Date: ${movie.release_date ?: "No release date available"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Adult: ${movie.adult ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Runtime: ${movie.runtime ?: "N/A"} min",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Vote Average: ${movie.vote_average ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Vote Count: ${movie.vote_count ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Revenue: $${movie.revenue ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Original Language: ${movie.original_language ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Genres: ${movie.genres?.joinToString(", ") { it.name } ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Overview:",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = movie.overview ?: "No overview available.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                            }

                            // Similar Movies Section
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Similar Movies",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(start = 24.dp)
                            )
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                items(similarMovies) { movie ->
                                    Card(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .width(140.dp) // Adjust width as needed
                                            .clickable {
                                                navController.navigate("movieDetail/${movie.id}")
                                            }
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val movieImageUrl = movie.poster_path
                                            if (movieImageUrl.isNullOrEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .height(200.dp)
                                                        .fillMaxWidth()
                                                        .background(Color.Gray),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No image",
                                                        color = Color.White
                                                    )
                                                }
                                            } else {
                                                Image(
                                                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                                                    contentDescription = movie.title,
                                                    modifier = Modifier
                                                        .height(200.dp)
                                                        .fillMaxWidth()
                                                )
                                            }

                                            // Movie Title Text
                                            Text(
                                                text = movie.title ?: "No title",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

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
                                            .weight(2f)
                                    ) {
                                        items(reviews.chunked(reviewsPerPage).getOrNull(currentPage - 1) ?: emptyList()) { review ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
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
                            } else {
                                Text(
                                    text = "No reviews available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
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
        }
    )
}