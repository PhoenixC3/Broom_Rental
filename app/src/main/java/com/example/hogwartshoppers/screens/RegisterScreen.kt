package com.example.hogwartshoppers.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.R
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    val userViewModel: UserViewModel = viewModel()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff321f12))
            .border(3.dp, Color(0xFFBB9753))
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.hogwartslogo),
            contentDescription = "Hogwarts Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(200.dp)
                .padding(bottom = 15.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {            // Username Input
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp) // This makes the corners rounded
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)

            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            Button(
                onClick = {
                    if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        // Create user in Firebase Authentication
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        // Register user in the Realtime Database using the ViewModel
                                        userViewModel.registerUser(username, email) { success ->
                                            isLoading = false
                                            if (success) {
                                                navController.navigate(Screens.Login.route)
                                            } else {
                                                errorMessage = "Failed to save user data to Realtime Database"
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = "Failed to retrieve user ID"
                                    }
                                } else {
                                    isLoading = false
                                    errorMessage = task.exception?.localizedMessage ?: "Registration failed"
                                }
                            }
                            .addOnFailureListener { error ->
                                isLoading = false
                                errorMessage = error.localizedMessage ?: "Registration failed"
                            }
                    } else {
                        errorMessage = "All fields are required"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBB9753)
                )
            )
            {
                if (isLoading) {
                    Text("Registering...")
                } else {
                    Text("Register")
                }
            }

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Already have an account?",
                    color = Color.White
                )

                TextButton(
                    onClick = {
                        navController.navigate(Screens.Login.route)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFBB9753) // Default theme color
                    )
                ) {
                    Text("Login")
                }
            }
        }
        Image(
            painter = painterResource(id = R.drawable.castlebrownbg),
            contentDescription = "Hogwarts Castle",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(200.dp) // Adjust size as needed
                .padding(top = 20.dp)
        )
    }
}
