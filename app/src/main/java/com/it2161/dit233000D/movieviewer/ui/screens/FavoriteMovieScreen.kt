package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModel
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMovieScreen(
    navController: NavController,
    context: Context,
    userProfile: UserProfile?,
    onMovieClick: (Long) -> Unit,
) {
    val repository = remember { MovieRepository.getInstance(context) }
    val viewModel: FavoriteMovieViewModel = viewModel(
        factory = FavoriteMovieViewModelFactory(repository, userProfile)
    )

    val currentUser = userProfile ?: UserProfile(0, "")

    LaunchedEffect(currentUser.id) {
        viewModel.loadFavoriteMovies() // load favorite movies on screen load
    }

    val favoriteMovies = viewModel.favoriteMovies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Movies") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = "Back"
                        )
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
                    items(favoriteMovies.value) { favMovie ->
                        FavoriteMovieItem(
                            movie = favMovie,
                            onClick = { onMovieClick(favMovie.id) },
                            onDelete = {
                                // Pass only the movieId to removeFavoriteMovie
                                viewModel.removeFavoriteMovie(favMovie.id)
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "There are no favorite movies added yet. Consider adding one!",
                        style = MaterialTheme.typography.bodyLarge,

                    )
                }
            }
        }
    )
}

@Composable
fun FavoriteMovieItem(
    movie: MovieItem,
    onClick: () -> Unit,
    onDelete: (MovieItem) -> Unit
) {
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
        Column(Modifier.weight(1f)) {
            Text(text = movie.title, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Release Date: ${movie.release_date}", style = MaterialTheme.typography.bodyMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rating: ${movie.vote_average}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "Star Icon",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFD700)
                )
            }
        }
        IconButton(onClick = { onDelete(movie) }) {  // remove the movie
            Icon(
                painter = painterResource(id = R.drawable.delete),
                contentDescription = "Delete",
                tint = Color.Red
            )
        }
    }
}
