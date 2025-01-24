package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.utils.isNetworkAvailable
import kotlinx.coroutines.launch

@Composable
fun MovieListScreen(navController: NavController) {
    val selectedTab = remember { mutableStateOf("popular") }
    val movieList = remember { mutableStateOf<List<MovieItem>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Movie categories
    val movieCategories = listOf("Popular", "Top Rated", "Now Playing", "Upcoming")

    // Retrieve API key from AndroidManifest
    val apiKey = try {
        context.packageManager.getApplicationInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_META_DATA
        ).metaData.getString("com.movieviewer.API_KEY.233000D")
    } catch (e: Exception) {
        null
    }

    if (apiKey == null) {
        Toast.makeText(context, "API key is missing", Toast.LENGTH_SHORT).show()
        return
    }

    // Fetch movies based on selected category
    fun fetchMovies() {
        scope.launch {
            try {
                val repository = MovieRepository.getInstance(context)

                // Check network connectivity
                if (isNetworkAvailable(context)) {
                    val movies = repository.getMovies(context, apiKey, selectedTab.value)

                    movieList.value = movies
                } else {
                    // Fetch from the local database if offline
                    val localMovies = repository.getMovies(context, apiKey, selectedTab.value)
                    movieList.value = localMovies
                    Toast.makeText(context, "Offline Mode: Displaying cached data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to fetch movies: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch movies whenever the selected tab changes
    LaunchedEffect(selectedTab.value) { fetchMovies() }

    // UI Components
    Column(modifier = Modifier.padding(16.dp)) {
        // Tab options for selecting movie categories
        Row {
            movieCategories.forEach { category ->
                Text(
                    text = category,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { selectedTab.value = category.lowercase().replace(" ", "") },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Movie list
        LazyColumn {
            items(movieList.value, key = { it.id }) { movie ->
                MovieItemCard(movie = movie, navController = navController)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}


@Composable
fun MovieItemCard(
    movie: MovieItem,
    navController: NavController,
) {
    Column(modifier = Modifier
        .padding(8.dp)
        .clickable {
            navController.navigate("movieDetail/${movie.id}")
        }) {

        // Movie title and poster
        Text(text = movie.title, style = MaterialTheme.typography.bodySmall)
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = "Movie Poster",
            modifier = Modifier.fillMaxWidth()
        )
    }
}