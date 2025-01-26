package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.it2161.dit233000D.movieviewer.data.movie.MovieDao
import com.it2161.dit233000D.movieviewer.data.movie.MovieDatabase
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    navController: NavController,
    onViewProfile: () -> Unit,
    onLogout: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val popularMovies = remember { mutableStateListOf<MovieItem>() }
    val topRatedMovies = remember { mutableStateListOf<MovieItem>() }
    val nowPlayingMovies = remember { mutableStateListOf<MovieItem>() }
    val upcomingMovies = remember { mutableStateListOf<MovieItem>() }

    val movieDao = MovieDatabase.getDatabase(context).movieDao()

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<MovieItem>() }

    // Fetch movies on initial launch
    LaunchedEffect(Unit) {
        fetchMovies("Popular", popularMovies, context, scope, movieDao)
        fetchMovies("Top Rated", topRatedMovies, context, scope, movieDao)
        fetchMovies("Now Playing", nowPlayingMovies, context, scope, movieDao)
        fetchMovies("Upcoming", upcomingMovies, context, scope, movieDao)
    }

    // Search movie updates based on search query
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            searchMovies(searchQuery, searchResults, context, scope)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PopCornMovie", color = MaterialTheme.colorScheme.primary) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                painter = painterResource(
                                    id = if (expanded) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down
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
                                    onFavoritesClick()
                                },
                                text = { Text("My Favorites") }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search for movies") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            if (searchResults.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // 3 columns
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp), // optional padding around the grid
                    verticalArrangement = Arrangement.spacedBy(8.dp), // optional space between rows
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // optional space between columns
                ) {
                    items(searchResults) { movie ->
                        MovieCard(movie = movie) {
                            navController.navigate("movieDetail/${movie.id}")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    item {
                        SectionHeader(title = "Popular")
                        MovieCarousel(movies = popularMovies, navController = navController)
                    }

                    item {
                        SectionHeader(title = "Top Rated")
                        MovieCarousel(movies = topRatedMovies, navController = navController)
                    }

                    item {
                        SectionHeader(title = "Now Playing")
                        MovieCarousel(movies = nowPlayingMovies, navController = navController)
                    }

                    item {
                        SectionHeader(title = "Upcoming")
                        MovieCarousel(movies = upcomingMovies, navController = navController)
                    }
                }
            }
        }
    }
}

fun searchMovies(
    query: String,
    searchResults: SnapshotStateList<MovieItem>,
    context: Context,
    scope: CoroutineScope
) {
    val apiKey = try {
        context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData?.getString("com.movieviewer.API_KEY.233000D")
    } catch (e: Exception) {
        null
    }

    if (apiKey.isNullOrBlank()) {
        Toast.makeText(context, "API key not found", Toast.LENGTH_SHORT).show()
        return
    }

    val apiService = RetrofitInstance.getApiService()
    searchResults.clear()
    scope.launch {
        try {
            val response = apiService.searchMovies(apiKey, query)
            response?.results?.let { searchResults.addAll(it) }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch search results: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}


fun fetchMovies(
    category: String,
    movieList: SnapshotStateList<MovieItem>,
    context: Context,
    scope: CoroutineScope,
    movieDao: MovieDao // Pass MovieDao here
) {
    val apiKey = try {
        context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData?.getString("com.movieviewer.API_KEY.233000D")
    } catch (e: Exception) {
        null
    }

    if (apiKey.isNullOrBlank()) {
        Toast.makeText(context, "API key not found", Toast.LENGTH_SHORT).show()
        return
    }

    val apiService = RetrofitInstance.getApiService()
    movieList.clear()

    scope.launch {
        try {
            // Fetch movies from API
            val response = when (category) {
                "Popular" -> apiService.getPopularMovies(apiKey)
                "Top Rated" -> apiService.getTopRatedMovies(apiKey)
                "Now Playing" -> apiService.getNowPlayingMovies(apiKey)
                "Upcoming" -> apiService.getUpcomingMovies(apiKey)
                else -> null
            }

            response?.results?.let { movies ->
                // Add movies to the UI list
                movieList.addAll(movies)

                // Save movies to the database
                movieDao.insertMovies(movies)
            }
        } catch (e: Exception) {
            // Handle API fetch failure
            Toast.makeText(context, "Failed to fetch movies: ${e.message}", Toast.LENGTH_SHORT).show()

            // Fallback to retrieve movies from the database
            val dbMovies = movieDao.getAllMovies()
            movieList.addAll(dbMovies)
        }
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun MovieCarousel(movies: List<MovieItem>, navController: NavController) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(movies) { movie ->
            MovieCard(movie = movie) {
                navController.navigate("movieDetail/${movie.id}")
            }
        }
    }
}

@Composable
fun MovieCard(movie: MovieItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = movie.title,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3
        )
    }
}