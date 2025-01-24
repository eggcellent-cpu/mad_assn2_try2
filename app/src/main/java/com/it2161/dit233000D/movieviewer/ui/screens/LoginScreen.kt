package com.it2161.dit233000D.movieviewer.ui.screens

import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.data.user.UserProfileDao
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (UserProfile) -> Unit,
    onRegister: () -> Unit,
    userProfileDao: UserProfileDao
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()  // To launch coroutines

    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var preferredName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessageUsername by remember { mutableStateOf<String?>(null) }
    var errorMessagePassword by remember { mutableStateOf<String?>(null) }
    var errorMessagePreferredName by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.studio_background_concept_abstract_empty_light_gradient_purple_studio_room_background_product),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.5f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.movie_viewer_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Enter Username") },
                modifier = Modifier.fillMaxWidth()
            )

            // Preferred Name (added here)
            OutlinedTextField(
                value = preferredName,
                onValueChange = { preferredName = it },
                label = { Text("Preferred Name") },
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessagePreferredName.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.error_icon),
                        contentDescription = "Error",
                        tint = androidx.compose.ui.graphics.Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessagePreferredName ?: "",
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMessagePassword.isNullOrEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.error_icon),
                        contentDescription = "Error",
                        tint = androidx.compose.ui.graphics.Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessagePassword ?: "",
                        color = androidx.compose.ui.graphics.Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    Log.d("LoginScreen", "Attempting login with userName: $userId and password: $password")

                    if (userId.isEmpty()) {
                        errorMessageUsername = "Username cannot be empty"
                    } else if (userId.length < 3) {
                        errorMessageUsername = "Username must be at least 3 characters long"
                    } else {
                        errorMessageUsername = null
                    }

                    if (preferredName.isEmpty()) {
                        errorMessagePreferredName = "Preferred name cannot be empty"
                    } else {
                        errorMessagePreferredName = null
                    }

                    if (password.isEmpty()) {
                        errorMessagePassword = "Password cannot be empty"
                    } else if (password.length !in 8..20) {
                        errorMessagePassword = "Password must be between 8 and 20 characters"
                    } else {
                        errorMessagePassword = null
                    }

                    if (errorMessageUsername == null && errorMessagePassword == null && errorMessagePreferredName == null) {
                        scope.launch {
                            val user = userProfileDao.getUserByUserName(userId)
                            if (user != null && user.password == password) {
                                onLoginSuccess(user)
                            } else {
                                Toast.makeText(context, "Invalid credentials entered. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            TextButton(onClick = onRegister) {
                Text("Register")
            }
        }
    }
}
