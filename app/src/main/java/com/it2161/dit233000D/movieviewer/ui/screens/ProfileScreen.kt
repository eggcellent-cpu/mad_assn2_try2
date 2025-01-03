package com.it2161.dit233000D.movieviewer.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.UserProfile
import androidx.navigation.NavController
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onEditProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    val savedUserName = sharedPreferences.getString("userName", "TestUser1") ?: "TestUser1"
    val savedPassword = sharedPreferences.getString("password", "TestPassword1") ?: "TestPassword1"
    val savedPreferredName = sharedPreferences.getString("preferredName", "TestName") ?: "TestName"
    val savedYOB = sharedPreferences.getString("yob", "2000") ?: "2000"
    val savedUpdates = sharedPreferences.getBoolean("updates", false)
    val savedAvatar = sharedPreferences.getInt("avatar", R.drawable.avatar_1) // default: avatar_1

    Log.d("ProfileScreen", "Saved User Data: userName: $savedUserName, avatar: $savedAvatar")

    val retrievedProfile = UserProfile(
        userName = savedUserName,
        password = savedPassword,
        preferredName = savedPreferredName,
        updates = savedUpdates,
        avatar = savedAvatar,
        yob = savedYOB
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(painter = painterResource(id = R.drawable.arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditProfileClick() }) {
                        Icon(painter = painterResource(id = R.drawable.edit), contentDescription = "Edit")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display Avatar
            Image(
                painter = painterResource(id = retrievedProfile.avatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 25.dp)
            )

            // Display User Profile Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display Username
                Row {
                    Text(text = "Username: ")
                    Text(text = retrievedProfile.userName, fontWeight = FontWeight.Bold)
                }

                // Display Password
                Row {
                    Text(text = "Password: ")
                    Text(text = retrievedProfile.password, fontWeight = FontWeight.Bold)
                }

                // Display Preferred name
                Row {
                    Text(text = "Preferred Name: ")
                    Text(text = retrievedProfile.preferredName, fontWeight = FontWeight.Bold)
                }

                // Display Year of Birth
                Row {
                    Text(text = "Year of Birth: ")
                    Text(text = retrievedProfile.yob, fontWeight = FontWeight.Bold)
                }

                // Display receiving updates
                Row {
                    Text(text = "Receive Updates?: ")
                    Text(text = if (retrievedProfile.updates) "Yes, true" else "No, false", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val editor = sharedPreferences.edit()
                    editor.putString("userName", retrievedProfile.userName)
                    editor.putString("password", retrievedProfile.password)
                    editor.putString("preferredName", retrievedProfile.preferredName)
                    editor.putString("yob", retrievedProfile.yob)
                    editor.putBoolean("updates", retrievedProfile.updates)
                    editor.putInt("avatar", retrievedProfile.avatar)
                    editor.apply()

                    onSaveClick(retrievedProfile)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}


@Composable
fun EditProfileScreen(
    userProfile: UserProfile,
    onBackClick: () -> Unit,
    onSaveClick: (UserProfile) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var userName by remember { mutableStateOf(userProfile.userName) }
    var password by remember { mutableStateOf(userProfile.password) }
    var confirmPassword by remember { mutableStateOf(userProfile.confirmPassword) }
    var preferredName by remember { mutableStateOf(userProfile.preferredName)}
    var yob by remember { mutableStateOf(userProfile.yob) }
    var updates by remember { mutableStateOf(userProfile.updates) }
    var selectedAvatar by remember { mutableStateOf(userProfile.avatar) }

    var errorMessageUsername by remember { mutableStateOf<String?>(null) }
    var errorMessagePassword by remember { mutableStateOf<String?>(null) }
    var errorMessageConfirmPassword by remember { mutableStateOf<String?>(null) }
    var errorMessagePreferredName by remember { mutableStateOf<String?>(null) } // Added this line

    LaunchedEffect(Unit) {
        userName = sharedPreferences.getString("userName", "") ?: ""
        password = sharedPreferences.getString("password", "") ?: ""
        preferredName = sharedPreferences.getString("preferredName", "") ?: ""
        yob = sharedPreferences.getString("yob", "") ?: ""
        updates = sharedPreferences.getBoolean("updates", false)
        selectedAvatar = sharedPreferences.getInt("avatar", R.drawable.avatar_1)
    }

    // red messages
    fun validateField(fieldName: String, value: String): String? {
        return when (fieldName) {
            "userName" -> {
                when {
                    value.isEmpty() -> "Username is required"
                    value.length < 3 -> "Username must be at least 3 characters long"
                    else -> null
                }
            }

            "password" -> when {
                value.isEmpty() -> "Password is required"
                value.length < 8 -> "Password must be at least 8 characters long"
                value.length > 20 -> "Password must be a maximum of 20 characters long"
                else -> null
            }

            "confirmPassword" -> when {
                value.isEmpty() -> "Confirm password is required"
                value != password -> "Passwords do not match"
                else -> null
            }

            "preferredName" -> if (value.isEmpty()) "Preferred name is optional" else null

            else -> null
        }
    }

    fun handleProfile() {
        var isValid = true

        // Check for validation errors
        errorMessageUsername = validateField("userName", userName)
        errorMessagePassword = validateField("password", password)
        errorMessageConfirmPassword = validateField("confirmPassword", confirmPassword)
        errorMessagePreferredName = validateField("preferredName", preferredName)

        // Check if there are any errors
        if (errorMessageUsername != null || errorMessagePassword != null || errorMessageConfirmPassword != null || errorMessagePreferredName != null) {
            Toast.makeText(context, "Please correct the format of required fields", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        // Save and navigate if validation passes
        if (isValid) {
            // Save user data to SharedPreferences
            sharedPreferences.edit().apply {
                putString("userName", userName)
                putString("password", password)
                putString("preferredName", preferredName)
                putString("yob", yob)
                putBoolean("updates", updates)
                putInt("avatar", selectedAvatar)
                apply()
            }

            // Trigger onSaveClick with updated profile
            onSaveClick(
                UserProfile(
                    userName = userName,
                    password = password,
                    preferredName = preferredName,
                    yob = yob,
                    updates = updates,
                    avatar = selectedAvatar
                )
            )

            // Navigate to the profile screen
            navController.navigate("profile")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Make the outer column scrollable only, to prevent the error
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Profile", style = MaterialTheme.typography.headlineMedium)

            // Display Avatar selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf(R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3).forEach { avatarId ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(4.dp)
                            .clickable { selectedAvatar = avatarId }
                    ) {
                        Image(
                            painter = painterResource(id = avatarId),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = if (selectedAvatar == avatarId) Color.Blue else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter Username:")
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        errorMessageUsername = validateField("userName", it)
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Username", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessageUsername != null
                )
                errorMessageUsername?.let { Text(it, color = Color.Red) }
            }

            // Password
            var passwordVisibility by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter Password:")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessagePassword = validateField("password", it)
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Password", color = Color.LightGray) },
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisibility) R.drawable.visibility else R.drawable.visibility_off
                                ),
                                contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = errorMessagePassword != null
                )
                errorMessagePassword?.let { Text(it, color = Color.Red) }
            }

            // Confirm Password
            var confirmPasswordVisibility by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Confirm Password:")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessageConfirmPassword = validateField("confirmPassword", it)
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Confirm Password", color = Color.LightGray) },
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                            Icon(
                                painter = painterResource(
                                    id = if (confirmPasswordVisibility) R.drawable.visibility else R.drawable.visibility_off
                                ),
                                contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = errorMessageConfirmPassword != null
                )
                errorMessageConfirmPassword?.let { Text(it, color = Color.Red) }
            }

            // Email
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter Preferred Name:")
                OutlinedTextField(
                    value = preferredName,
                    onValueChange = {
                        preferredName = it
                        errorMessagePreferredName = validateField("preferredName", it)
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Email", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessagePreferredName != null
                )
                errorMessagePreferredName?.let { Text(it, color = Color.Red) }
            }

            // Year of Birth Dropdown
            var expanded by remember { mutableStateOf(false) }
            val years = (1920..Calendar.getInstance().get(Calendar.YEAR)).toList()
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Year of Birth")
                Box {
                    OutlinedTextField(
                        value = yob,
                        onValueChange = { /* read-only */ },
                        label = { Text("") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    painter = painterResource(id = if (expanded) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down),
                                    contentDescription = if (expanded) "Collapse" else "Expand"
                                )
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    yob = year.toString() // Update the Year of Birth
                                    expanded = false // Close the dropdown
                                }
                            )
                        }
                    }
                }
            }

            // Updates
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = updates,
                    onCheckedChange = { updates = it }
                )
                Text("I want to receive updates")
            }

            Button(
                onClick = { handleProfile() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            Button(
                onClick = { onBackClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

