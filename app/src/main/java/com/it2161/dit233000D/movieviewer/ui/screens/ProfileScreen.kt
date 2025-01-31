package com.it2161.dit233000D.movieviewer.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.it2161.dit233000D.movieviewer.R
import androidx.navigation.NavController
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.data.user.UserProfileDao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    navController: NavController,
    userProfileDao: UserProfileDao
) {
    var savedProfile by remember { mutableStateOf<UserProfile?>(null) }

    // fetch profile asynchronously
    LaunchedEffect(userProfile.id) {
        Log.d("ProfileScreen", "User ID: ${userProfile.id}")
        savedProfile = userProfileDao.getProfile(userProfile.id)
        Log.d("ProfileScreen", "Fetched profile: $savedProfile")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("landing")
                    }) {
                        Icon(painter = painterResource(id = R.drawable.arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (savedProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Image(
                    painter = painterResource(id = savedProfile!!.avatar),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 25.dp)
                )

                // User Profile Details
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "User ID: ")
                        Text(text = savedProfile!!.id.toString())
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Username: ")
                        Text(text = savedProfile!!.userName)
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Password: ")
                        Text(text = savedProfile!!.password)
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Preferred Name: ")
                        Text(text = savedProfile!!.preferredName)
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Year of Birth: ")
                        Text(text = savedProfile!!.yob)
                    }

                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Receive Updates?: ")
                        Text(text = if (savedProfile!!.updates) "Yes" else "No")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("landing") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Landing Page")
                }
            }
        }
    }
}