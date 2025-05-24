package com.example.hogwartshoppers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hogwartshoppers.screens.NavGraph
import com.example.hogwartshoppers.screens.Screens
import com.example.hogwartshoppers.ui.theme.HogwartsHoppersTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the status bar color to the default background color
        window.statusBarColor = ContextCompat.getColor(this, R.color.default_background)

        enableEdgeToEdge()
        setContent {
            HogwartsHoppersTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun Login(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(Unit) {
        if(currentUser != null) {
            navController.navigate(Screens.HomeScreen.route){
                popUpTo(Screens.Login.route) {
                    inclusive = true
                }
            }
        }
    }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp) // This makes the corners rounded
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp) // This makes the corners rounded
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        // Use Firebase Authentication to log in
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    // Navigate to the HomeScreen and pass the email as a parameter
                                    navController.navigate(
                                        Screens.HomeScreen.route.replace(
                                            oldValue = "{email}",
                                            newValue = email
                                        )
                                    )
                                } else {
                                    // Display error message if login fails
                                    errorMessage = task.exception?.localizedMessage ?:
                                            "Login failed: Email/Password are incorrect!"
                                }
                            }
                            .addOnFailureListener { error ->
                                isLoading = false
                                errorMessage = error.localizedMessage ?: "An unknown error occurred"
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
                    Text("Validating...")
                } else {
                    Text("Login")
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
                Text(text = "Don't have an account?",
                    color = Color.White
                )

                TextButton(
                    onClick = {
                        navController.navigate(Screens.Register.route)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFBB9753) // Default theme color
                    )
                ) {
                    Text("Register")
                }
            }
        }
        Image(
            painter = painterResource(id = R.drawable.castlebrownbg),
            contentDescription = "Hogwarts Castle",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(200.dp) // Adjust size as needed
        )
    }
}