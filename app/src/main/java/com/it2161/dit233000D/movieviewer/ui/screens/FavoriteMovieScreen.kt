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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.movie.FavoriteMovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModel
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteMovieScreen(
    navController: NavController,
    context: Context,
    userProfile: UserProfile?, // Change parameter type
    onMovieClick: (Long) -> Unit
) {
    val repository = remember { MovieRepository.getInstance(context) }
    val viewModel: FavoriteMovieViewModel = viewModel(
        factory = FavoriteMovieViewModelFactory(repository)
    )

    // Use the passed user profile or fallback to a default
    val currentUser = userProfile ?: UserProfile(0, "")

    // Load favorite movies
    LaunchedEffect(currentUser.userName) {
        viewModel.loadFavoriteMovies(currentUser.userName)
    }

    // Observe the favorite movies
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
                    items(favoriteMovies.value) { movie ->
                        FavoriteMovieItem(
                            movie = movie,
                            onClick = { navController.navigate("movieDetail/${movie.id}") },
                            onToggleFavorite = { isFavorite ->
                                viewModel.toggleFavorite(movie, isFavorite)
                            }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No favorite movies added yet.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )
}

@Composable
fun FavoriteMovieItem(movie: FavoriteMovieItem, onClick: () -> Unit, onToggleFavorite: (Boolean) -> Unit) {
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
            Text(text = "Rating: ${movie.vote_average}", style = MaterialTheme.typography.bodySmall)
        }
        IconToggleButton(
            checked = movie.is_favorite,
            onCheckedChange = { isChecked -> onToggleFavorite(isChecked) }
        ) {
            Icon(
                painter = painterResource(
                    id = if (movie.is_favorite) R.drawable.heart_check else R.drawable.favorite
                ),
                contentDescription = if (movie.is_favorite) "Remove from favorites" else "Add to favorites"
            )
        }
    }
}
