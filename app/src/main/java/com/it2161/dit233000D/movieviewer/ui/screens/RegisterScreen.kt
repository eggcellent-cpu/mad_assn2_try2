package com.it2161.dit233000D.movieviewer.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.it2161.dit233000D.movieviewer.data.user.UserProfile
import com.it2161.dit233000D.movieviewer.R
import com.it2161.dit233000D.movieviewer.data.user.UserDatabase

import java.util.Calendar

@Composable
fun RegisterUserScreen(
    navController: NavController,
    userProfile: UserProfile,
    userProfileChange: (UserProfile) -> Unit,
) {
    var userName by remember { mutableStateOf(userProfile.userName) }
    var password by remember { mutableStateOf(userProfile.password) }
    var confirmPassword by remember { mutableStateOf(userProfile.confirmPassword) }
    var yob by remember { mutableStateOf(userProfile.yob) }
    var updates by remember { mutableStateOf(userProfile.updates) }
    var preferredName by remember { mutableStateOf(userProfile.preferredName ?: "") }

    var errorMessageUsername by remember { mutableStateOf<String?>(null) }
    var errorMessagePassword by remember { mutableStateOf<String?>(null) }
    var errorMessageConfirmPassword by remember { mutableStateOf<String?>(null) }
    var errorMessageYob by remember { mutableStateOf<String?>(null) }
    var errorMessagePreferredName by remember { mutableStateOf<String?>(null) }

    val years = (1920..Calendar.getInstance().get(Calendar.YEAR)).toList()
    val context = LocalContext.current

    val db = UserDatabase.getDatabase(context)
    val userProfileDao = db.userProfileDao()
    val lifecycleOwner = LocalLifecycleOwner.current


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
            "yob" -> if (value.isEmpty()) "Year of birth selection is required" else null
            "preferredName" -> if (value.isEmpty()) "Preferred name is optional" else null
            else -> null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("User Registration", style = MaterialTheme.typography.headlineMedium)

            // Username
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter Username:")
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        errorMessageUsername = validateField("userName", it)
                        userProfileChange(userProfile.copy(userName = it))
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
                        userProfileChange(userProfile.copy(password = it))
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Password", color = Color.LightGray) },
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                painter = painterResource(id = if (passwordVisibility) R.drawable.visibility else R.drawable.visibility_off),
                                contentDescription = if (passwordVisibility) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = errorMessagePassword != null
                )
                errorMessagePassword?.let { Text(it, color = Color.Red) }
            }

            // Confirm password
            var confirmPasswordVisibility by remember { mutableStateOf(false) }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Confirm Password:")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessageConfirmPassword = validateField("confirmPassword", it)
                        userProfileChange(userProfile.copy(confirmPassword = it))
                    },
                    label = { Text("") },
                    placeholder = { Text("Confirm Password", color = Color.LightGray) },
                    visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                            Icon(
                                painter = painterResource(id = if (confirmPasswordVisibility) R.drawable.visibility else R.drawable.visibility_off),
                                contentDescription = if (confirmPasswordVisibility) "Hide password" else "Show password"
                            )
                        }
                    },
                    isError = errorMessageConfirmPassword != null
                )
                errorMessageConfirmPassword?.let { Text(it, color = Color.Red) }
            }

            // Preferred Name
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Preferred Name (Optional):")
                OutlinedTextField(
                    value = preferredName,
                    onValueChange = {
                        preferredName = it
                        errorMessagePreferredName = validateField("preferredName", it)
                        userProfileChange(userProfile.copy(preferredName = it))
                    },
                    label = { Text("") },
                    placeholder = { Text("Enter Preferred Name (Optional)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessagePreferredName != null
                )
                errorMessagePreferredName?.let { Text(it, color = Color.Red) }
            }

            // Year of Birth Dropdown
            var expanded by remember { mutableStateOf(false) }
            Column {
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
                                    yob = year.toString()
                                    userProfileChange(userProfile.copy(yob = yob))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Updates
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = updates,
                    onCheckedChange = {
                        updates = it
                        userProfileChange(userProfile.copy(updates = it))
                    }
                )
                Text("I want to receive updates")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    var isValid = true
                    if (userName.isEmpty()) {
                        Toast.makeText(context, "Username is required", Toast.LENGTH_SHORT).show()
                        isValid = false
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(context, "Password is required", Toast.LENGTH_SHORT).show()
                        isValid = false
                    }
                    if (confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Confirm Password is required", Toast.LENGTH_SHORT).show()
                        isValid = false
                    }
                    if (yob.isEmpty()) {
                        Toast.makeText(context, "Year of birth selection is required", Toast.LENGTH_SHORT).show()
                        isValid = false
                    }

                    if (errorMessageUsername != null ||
                        errorMessagePassword != null ||
                        errorMessageConfirmPassword != null ||
                        errorMessageYob != null ||
                        errorMessagePreferredName != null
                    ) {
                        Toast.makeText(context, "Please make sure to correct the format of required fields in the form", Toast.LENGTH_SHORT).show()
                        isValid = false
                    }

                    if (isValid) {
                        val newUserProfile = UserProfile(
                            userName = userName,
                            password = password,
                            confirmPassword = "",
                            updates = updates,
                            avatar = R.drawable.avatar_1,
                            yob = yob,
                            preferredName = preferredName
                        )

                        lifecycleOwner.lifecycleScope.launch {
                            // insert user into database
                            userProfileDao.insertUser(newUserProfile)

                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            navController.navigate("landing")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Register")
            }

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                )
            ) {
                val annotatedText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Black)) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = Color.DarkGray, textDecoration = TextDecoration.Underline)) {
                        append("Login here.")
                    }
                }

                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center, fontSize = 17.sp
                    )
                )
            }
        }
    }
}

