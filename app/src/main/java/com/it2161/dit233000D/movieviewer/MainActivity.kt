package com.it2161.dit233000D.movieviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.it2161.dit233000D.movieviewer.data.UserProfile
import com.it2161.dit233000D.movieviewer.ui.screens.EditProfileScreen
import com.it2161.dit233000D.movieviewer.ui.screens.LandingScreen
import com.it2161.dit233000D.movieviewer.ui.screens.LoginScreen
import com.it2161.dit233000D.movieviewer.ui.screens.MovieDetailScreen
import com.it2161.dit233000D.movieviewer.ui.screens.MovieListScreen
import com.it2161.dit233000D.movieviewer.ui.screens.ProfileScreen
import com.it2161.dit233000D.movieviewer.ui.screens.RegisterUserScreen
import com.it2161.dit233000D.movieviewer.ui.theme._233000DMovieViewer2Theme

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
                LoginScreen(
                    onLoginSuccess = { user ->
                        currentUser = user
                        navController.navigate("landing")
                    },
                    onRegister = { navController.navigate("register") }
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
                ProfileScreen(
                    userProfile = currentUser ?: UserProfile(),
                    onEditProfileClick = {
                        navController.navigate("edit_profile")
                    },
                    onBackClick = {
                        navController.navigate("landing") {
                            popUpTo("profile") { inclusive = true }
                        }
                    },
                    onSaveClick = { updatedProfile ->
                        currentUser = updatedProfile
                        navController.popBackStack("landing", false)
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    userProfile = currentUser ?: UserProfile(),
                    onBackClick = {
                        navController.navigate("profile")
                    },
                    onSaveClick = { updatedProfile ->
                        currentUser = updatedProfile
                        navController.popBackStack("profile", false)
                    },
                    navController = navController
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
                val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
                movieId?.let {
                    MovieDetailScreen(movieId = it, navController = navController)
                }
            }

        }
    }
}
