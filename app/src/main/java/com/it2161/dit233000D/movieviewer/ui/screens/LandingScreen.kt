package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.MovieItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    navController: NavController,
    onViewProfile: () -> Unit,
    onLogout: () -> Unit,
    onMovieListClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Popular") }
    val movieList = remember { mutableStateListOf<MovieItem>() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PopCornMovie", color = MaterialTheme.colorScheme.primary) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                painter = painterResource(
                                    id = if (expanded) R.drawable.arrow_drop_up
                                    else R.drawable.arrow_drop_down
                                ),
                                contentDescription = null
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    onViewProfile()
                                },
                                text = { Text("View Profile") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    onLogout()
                                },
                                text = { Text("Logout") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    expanded = false
                                    onFavoritesClick() // Navigate to favorites
                                },
                                text = { Text("My Favorites") }
                            )
                            listOf("Popular", "Top Rated", "Now Playing", "Upcoming").forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                        fetchMovies(category, movieList, context, scope)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(movieList) { movie ->
                    MovieItemView(movie = movie) {
                        navController.navigate("movieDetail/${movie.id}")
                    }
                }
            }

            // Button for navigating to the movie list
            Button(
                onClick = onMovieListClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text("Go to Movie List")
            }
        }
    }
}

fun fetchMovies(
    category: String,
    movieList: SnapshotStateList<MovieItem>,
    context: Context,
    scope: CoroutineScope
) {
    // Retrieve the API key from AndroidManifest metadata
    val apiKey = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData.getString("com.movieviewer.API_KEY.233000D")

    val apiService = RetrofitInstance.getApiService() // Instance of TMDBApiService
    movieList.clear()
    scope.launch {
        try {
            val response = when (category) {
                "Popular" -> apiService.getPopularMovies(apiKey!!)
                "Top Rated" -> apiService.getTopRatedMovies(apiKey!!)
                "Now Playing" -> apiService.getNowPlayingMovies(apiKey!!)
                "Upcoming" -> apiService.getUpcomingMovies(apiKey!!)
                else -> null
            }
            response?.results?.let { movieList.addAll(it) }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch movies: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// Display individual movie items
@Composable
fun MovieItemView(movie: MovieItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = movie.title,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = movie.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}