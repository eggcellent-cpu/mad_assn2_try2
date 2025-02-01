package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.text.*
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

    var currentPage by remember { mutableStateOf(1) }
    val reviewsPerPage = 5
    val totalPages = if (reviews.isEmpty()) 1 else ((reviews.size + reviewsPerPage - 1) / reviewsPerPage)

    val movieDatabase = MovieDatabase.getDatabase(context)
    var similarMovies by remember { mutableStateOf<List<MovieItem>>(emptyList()) }

    val currentUser = userProfile ?: UserProfile(0, "")

    val isFavorite = remember(currentUser.id) {
        Log.d("MovieViewerApp", "Initializing favorite state for user: ${currentUser.userName}")
        mutableStateOf(false)
    }

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

    LaunchedEffect(movieId, currentUser.id) {
        val movieIdLong = safeConvertToLong(movieId)
        if (movieIdLong == null) {
            errorMessage = "Invalid movie ID format. Unable to load details."
            return@LaunchedEffect
        }

        // Fetch favorite status from the database
        val isMovieFavorited = viewModel.isMovieFavorited(movieIdLong, currentUser.id)
        Log.d("MovieViewerApp", "Initial favorite status for movie $movieIdLong: $isMovieFavorited")
        isFavorite.value = isMovieFavorited

        val apiKey = try {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            ).metaData.getString("com.movieviewer.API_KEY.233000D") ?: run {
                errorMessage = "API key not found."
                return@LaunchedEffect
            }
        } catch (e: Exception) {
            errorMessage = "Failed to retrieve API key: ${e.message}"
            return@LaunchedEffect
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

                // Fetch Similar Movies
                val similarMoviesResponse = RetrofitInstance.getApiService().getSimilarMovies(
                    movieId = movieIdLong.toInt(),
                    apiKey = apiKey
                )
                similarMovies = similarMoviesResponse.results.map { movie ->
                    movie.copy(
                        title = movie.title,
                        poster_path = movie.poster_path ?: "unavailable",
                    )
                }
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
    }
    offlineMessage?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }

    // Function to toggle favorite status
    fun toggleFavorite(movie: MovieItem) {
        scope.launch {
            val favoriteMovie = movie.toFavoriteMovieItem(currentUser.userName, currentUser.id)

            if (isFavorite.value) {
                // Remove from favorites
                viewModel.removeFavoriteMovie(favoriteMovie)
                Log.d("MovieViewerApp", "Removed favorite - Movie: ${movie.title}, User: ${currentUser.userName}")
            } else {
                // Add to favorites
                viewModel.addFavoriteMovie(favoriteMovie)
                Log.d("MovieViewerApp", "Added favorite - Movie: ${movie.title}, User: ${currentUser.userName}")
            }

            isFavorite.value = !isFavorite.value
            Log.d("MovieViewerApp", "Updated favorite status - Movie: ${movie.title}, IsFavorite: ${isFavorite.value}")
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
                },
                actions = {
                    IconButton(onClick = {
                        movieDetail.value?.let { movie ->
                            toggleFavorite(movie)
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
                    .padding(horizontal = 8.dp),
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Rating: ${movie.vote_average}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(4.dp)) // Add some spacing between text and icon
                                        Icon(
                                            painter = painterResource(id = R.drawable.star),
                                            contentDescription = "Star Icon",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFFFFD700)
                                        )
                                    }
                                }

                                // Right side: Movie details
                                Column(
                                    modifier = Modifier
                                        .weight(2f)
                                        .fillMaxHeight()
                                ) {
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme.typography.headlineLarge,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Release Date: ")
                                            }
                                            append(movie.release_date)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Adult: ")
                                            }
                                            append(if (movie.adult) "Yes" else "No")
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Runtime: ")
                                            }
                                            append("${movie.runtime} min")
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Vote Average: ")
                                            }
                                            append(movie.vote_average.toString())
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Vote Count: ")
                                            }
                                            append(movie.vote_count.toString())
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Revenue: ")
                                            }
                                            append("$${movie.revenue}")
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Original Language: ")
                                            }
                                            append(movie.original_language)
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append("Genres: ")
                                            }
                                            append(movie.genres?.joinToString(", ") { it.name } ?: "N/A")
                                        },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Overview:",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
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
                            )
                            if (similarMovies.isEmpty()) {
                                Text(
                                    text = "No similar movies available.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    items(similarMovies) { movie ->
                                        Card(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .width(140.dp)
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
                                                    text = movie.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Reviews section
                            Text(
                                text = "Reviews:",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                            )


                            if (reviews.isNotEmpty()) {
                                val paginatedReviews = reviews.chunked(reviewsPerPage).getOrNull(currentPage - 1) ?: emptyList()
                                Log.d("MovieViewerApp", "Paginated Reviews: ${paginatedReviews.size}")

                                // Display all reviews with pagination
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(400.dp)
                                ) {
                                    // Inside your LazyColumn items block
                                    items(paginatedReviews) { review ->
                                        var isExpanded by remember { mutableStateOf(false) } // Track expanded state

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Author: ${review.author}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Display truncated or full review based on expanded state
                                                val reviewText = cleanHtmlContent(review.content)
                                                val maxLines = if (isExpanded) Int.MAX_VALUE else 5 // Show 5 lines when collapsed

                                                Text(
                                                    text = reviewText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = maxLines,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                // Show "Read More" or "Show Less" based on expanded state
                                                if (reviewText.length > 200) { // Adjust length threshold as needed
                                                    Text(
                                                        text = if (isExpanded) "Show Less" else "Read More",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Blue,
                                                        modifier = Modifier
                                                            .clickable { isExpanded = !isExpanded }
                                                            .padding(top = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Pagination controls (always visible)
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
                                    Text(
                                        "${currentPage} / $totalPages",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
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