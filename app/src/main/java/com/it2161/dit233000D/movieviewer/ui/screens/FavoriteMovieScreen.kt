package com.it2161.dit233000D.movieviewer.ui.screens

import FavoriteMoviesViewModel
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.movie.MovieDao
import androidx.room.Room
import com.it2161.dit233000D.movieviewer.data.movie.MovieDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMovieScreen(navController: NavController, context: Context) {
    // Initialize the MovieDao and MovieRepository (you should replace this with your actual DAO)
    val movieDao: MovieDao = Room.databaseBuilder(
        context,
        MovieDatabase::class.java, "movie-database"
    ).build().movieDao()

    val movieRepository = remember { MovieRepository.getInstance(context) }

    // Use the factory to initialize the ViewModel with the movieRepository
    val viewModel: FavoriteMoviesViewModel = viewModel(
        factory = FavoriteMoviesViewModel.provideFactory(movieRepository)
    )

    // Observe the favorite movies from the ViewModel
    val favoriteMovies = viewModel.favoriteMovies.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Movies") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.arrow_back), contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            if (favoriteMovies.value.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(favoriteMovies.value) { movie ->
                        FavoriteMovieItem(movie = movie, onClick = {
                            navController.navigate("movieDetail/${movie.id}")
                        })
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No favorite movies added yet.", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    )
}

@Composable
fun FavoriteMovieItem(movie: MovieItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
            contentDescription = movie.title,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = movie.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Release Date: ${movie.release_date}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rating: ${movie.vote_average}", style = MaterialTheme.typography.bodySmall)
        }
    }
}