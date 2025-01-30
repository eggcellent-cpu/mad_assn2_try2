package com.it2161.dit233000D.movieviewer

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.it2161.dit233000D.movieviewer.api.RetrofitInstance
import com.it2161.dit233000D.movieviewer.data.movie.MovieItem
import com.it2161.dit233000D.movieviewer.data.movie.MovieRepository
import com.it2161.dit233000D.movieviewer.data.user.UserDatabase
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.data.user.UserProfileDao
import com.it2161.dit233000D.movieviewer.ui.screens.FavoriteMovieScreen
import com.it2161.dit233000D.movieviewer.ui.screens.LandingScreen
import com.it2161.dit233000D.movieviewer.ui.screens.LoginScreen
import com.it2161.dit233000D.movieviewer.ui.screens.MovieDetailScreen
import com.it2161.dit233000D.movieviewer.ui.screens.MovieListScreen
import com.it2161.dit233000D.movieviewer.ui.screens.ProfileScreen
import com.it2161.dit233000D.movieviewer.ui.screens.RegisterUserScreen
import com.it2161.dit233000D.movieviewer.ui.theme._233000DMovieViewer2Theme
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModel
import com.it2161.dit233000D.movieviewer.viewmodel.FavoriteMovieViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _233000DMovieViewer2Theme {
                MovieViewerApp()
            }
        }
    }
}

@Composable
fun MovieViewerApp() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<UserProfile?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                val userProfileDao = UserDatabase.getDatabase(LocalContext.current).userProfileDao()
                LoginScreen(
                    onLoginSuccess = { user ->
                        currentUser = user
                        navController.navigate("landing")
                    },
                    onRegister = { navController.navigate("register") },
                    userProfileDao = userProfileDao
                )
            }

            composable("register") {
                RegisterUserScreen(
                    navController = navController,
                    userProfile = UserProfile(),
                    userProfileChange = { }
                )
            }

            composable("profile") {
                val userProfileDao = UserDatabase.getDatabase(LocalContext.current).userProfileDao()

                ProfileScreen(
                    userProfile = currentUser ?: UserProfile(),
                    navController = navController,  // Pass navController here
                    userProfileDao = userProfileDao
                )
            }

            composable("landing") {
                LandingScreen(
                    navController = navController,
                    onViewProfile = { navController.navigate("profile") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("landing") { inclusive = true }
                        }
                    },
                    onFavoritesClick = { navController.navigate("favorites") }
                )
            }

            composable("movieList") {
                MovieListScreen(navController = navController)
            }

            composable("movieDetail/{movieId}") { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId")?.toLongOrNull()

                if (movieId != null) {
                    val context = LocalContext.current
                    val app = context.applicationContext as MovieViewerApplication
                    val repository = app.movieRepository

                    val viewModel: FavoriteMovieViewModel = viewModel(
                        factory = FavoriteMovieViewModelFactory(repository, userProfile = currentUser)
                    )

                    // State to handle loading and error
                    var movieDetails: MovieItem? by remember { mutableStateOf(null) }
                    var errorMessage: String? by remember { mutableStateOf(null) }

                    // Use LaunchedEffect to fetch movie details from API
                    LaunchedEffect(movieId) {
                        try {
                            val apiKey = context.packageManager
                                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
                                .metaData.getString("com.movieviewer.API_KEY.233000D")

                            if (!apiKey.isNullOrBlank()) {
                                // Make the API call and update the movie details
                                movieDetails = RetrofitInstance.getApiService().getMovieDetails(movieId, apiKey!!)
                            } else {
                                errorMessage = "Invalid movie ID format."
                            }

                        } catch (e: Exception) {
                            // Handle the error by setting an error message
                            errorMessage = "Error loading movie details: ${e.message}"
                        }
                    }

                    // Check for error or loading state
                    if (errorMessage != null) {
                        Text(text = errorMessage!!)
                    } else if (movieDetails != null) {
                        // Movie details are ready, pass them to MovieDetailScreen
                        MovieDetailScreen(
                            movieId = movieId, // Directly use movieId as Long
                            navController = navController,
                            repository = repository,
                            userProfile = currentUser ?: UserProfile(),                        )
                    } else {
                        // While loading
                        Text(text = "Loading movie details...")
                    }
                } else {
                    Text(text = "Invalid movie ID")
                }
            }

            composable("favorites") {
                val context = LocalContext.current  // Get the current context
                FavoriteMovieScreen(
                    navController = navController,
                    context = context,
                    userProfile = currentUser,
                    onMovieClick = { movieId ->
                        navController.navigate("movieDetail/$movieId")
                    }
                )
            }

        }
    }
}
