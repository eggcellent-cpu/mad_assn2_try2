package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.movie.MovieDao
import com.it2161.dit233000D.movieviewer.data.movie.MovieDatabase
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
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
    val repository = MovieRepository.getInstance(context)

    var searchQuery by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<MovieItem>() }

    // State to track the selected section
    var selectedSection by remember { mutableStateOf("All") }

    // Fetch movies on initial launch
    LaunchedEffect(Unit) {
        fetchMovies("Popular", popularMovies, context, scope, repository, movieDao)
        fetchMovies("Top Rated", topRatedMovies, context, scope, repository, movieDao)
        fetchMovies("Now Playing", nowPlayingMovies, context, scope, repository, movieDao)
        fetchMovies("Upcoming", upcomingMovies, context, scope, repository, movieDao)
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search for movies") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.search),
                        contentDescription = "Search Icon",
                        modifier = Modifier.clickable {
                            if (searchQuery.isNotBlank()) {
                                searchMovies(searchQuery, searchResults, context, scope)
                            }
                        }
                    )
                }
            )

            if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                // Search Results Section
                Text(
                    text = "Search Result: '$searchQuery'",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(searchResults.chunked(3)) { rowMovies ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowMovies.forEach { movie ->
                                MovieCard(
                                    movie = movie,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate("movieDetail/${movie.id}")
                                    }
                                )
                            }
                            repeat(3 - rowMovies.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (searchQuery.isBlank()) {
                // Section Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SectionItem(
                        text = "All",
                        isSelected = selectedSection == "All",
                        onClick = { selectedSection = "All" }
                    )
                    SectionItem(
                        text = "Popular",
                        isSelected = selectedSection == "Popular",
                        onClick = { selectedSection = "Popular" }
                    )
                    SectionItem(
                        text = "Top Rated",
                        isSelected = selectedSection == "Top Rated",
                        onClick = { selectedSection = "Top Rated" }
                    )
                    SectionItem(
                        text = "Now Playing",
                        isSelected = selectedSection == "Now Playing",
                        onClick = { selectedSection = "Now Playing" }
                    )
                    SectionItem(
                        text = "Upcoming",
                        isSelected = selectedSection == "Upcoming",
                        onClick = { selectedSection = "Upcoming" }
                    )
                }

                // Movie Sections Content
                when (selectedSection) {
                    "All" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
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
                    "Popular" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                SectionHeader(title = "Popular")
                                MovieCarousel(movies = popularMovies, navController = navController)
                            }
                        }
                    }
                    "Top Rated" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                SectionHeader(title = "Top Rated")
                                MovieCarousel(movies = topRatedMovies, navController = navController)
                            }
                        }
                    }
                    "Now Playing" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                SectionHeader(title = "Now Playing")
                                MovieCarousel(movies = nowPlayingMovies, navController = navController)
                            }
                        }
                    }
                    "Upcoming" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                SectionHeader(title = "Upcoming")
                                MovieCarousel(movies = upcomingMovies, navController = navController)
                            }
                        }
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
    repository: MovieRepository,
    movieDao: MovieDao
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
                val detailedMovies = mutableListOf<MovieItem>()
                // fetch full details for each movie
                movies.forEach { movie ->
                    val movieDetail = apiService.getMovieDetails(movie.id, apiKey)
                    detailedMovies.add(movieDetail)
                }
                // update UI list
                movieList.addAll(detailedMovies)

                // save movies to the database
                movieDao.insertMovies(detailedMovies)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to fetch movies: ${e.message}", Toast.LENGTH_SHORT).show()

            // fallback to retrieve movies from the database
            val dbMovies = repository.getAllMovies(context, apiKey)
            movieList.addAll(dbMovies)
        }
    }
}

@Composable
fun SectionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }

    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                // Detect hover state
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter -> isHovered = true
                            PointerEventType.Exit -> isHovered = false
                            else -> Unit
                        }
                    }
                }
            }
            .padding(8.dp),
        style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            textDecoration = if (isSelected || isHovered) TextDecoration.Underline else TextDecoration.None
        )
    )
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
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
fun MovieCard(
    movie: MovieItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(150.dp)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = movie.title,
            modifier = Modifier
                .height(200.dp)
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