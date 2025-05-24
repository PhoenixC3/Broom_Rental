package com.example.hogwartshoppers.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.R
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, userMail: String) {

    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }

    var editing by remember { mutableStateOf(false) }

    LaunchedEffect(userMail) {
        userViewModel.getUserInfo(userMail) { user ->
            currUser = user // Update currUser with the fetched data
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen || drawerState.isAnimationRunning,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xff321f12))

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hogwartslogo),
                        contentDescription = "Hogwarts Logo",
                        modifier = Modifier
                            .size(200.dp) // Adjust size as needed
                            .align(Alignment.TopCenter)
                            .offset(y = (-25).dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        Divider(
                            color = Color.White,  // Color of the line
                            thickness = 1.dp,     // Line thickness
                            modifier = Modifier
                                .fillMaxWidth()   // Makes the line span the width
                                .padding(horizontal = 24.dp)
                                .padding(top = 150.dp)
                        )

                        Text(
                            text = "Welcome " + currUser?.username,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Menu(navController = navController, currUserEmail = authUser?.email)
                    }
                }
            }
        },
    ) {
        Scaffold(
            floatingActionButton = {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text("") },
                        icon = {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Menu",
                                modifier = Modifier.size(50.dp)
                                    .align(Alignment.CenterStart)
                                    .padding(start = 4.dp),

                                tint = Color.White
                            )
                        },
                        onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(start = 30.dp, top = 50.dp) // Adjust position on the screen
                            .size(60.dp), // Make the button larger for better content alignment
                        containerColor = Color(0xff321f12), // Brown background for the button
                        contentColor = Color.White // White color for the content inside
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xff321f12))
                    .border(3.dp, Color(0xFFBB9753))
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hogwartslogo),
                    contentDescription = "Hogwarts Logo",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(200.dp) // Adjust size as needed
                        .padding(bottom = 15.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 150.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(350.dp, 70.dp) // Set specific width and height
                            .padding(bottom = 20.dp)
                            .background(
                                color = Color(0xff4b2f1b), // Brown background
                                shape = RoundedCornerShape(16.dp) // Makes corners rounded
                            ),
                        contentAlignment = Alignment.Center // Centers the text inside the box

                    ) {
                        Text(
                            text = "Profile",
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xff321f12)) // Brown background
                    ) {
                        if (!editing){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Profile Picture
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .background(Color.Gray, shape = RoundedCornerShape(50.dp))
                                        .border(2.dp, Color(0xFFBB9753), shape = CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.default_ahh), // Placeholder
                                        contentDescription = "Profile Picture",
                                        modifier = Modifier.fillMaxSize()
                                            .clip(CircleShape) // Make the image circular
                                            .border(2.dp, Color(0xff321f12), CircleShape)
                                    )
                                }

                                if (currUser?.name != null){
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                // User Details
                                Text(
                                    text = currUser?.name ?: "",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = currUser?.username ?: "",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = currUser?.house ?: "---------",
                                    color = Color(0xFF66FF66),
                                    fontSize = 22.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                // Stats Section
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Distance Travelled:",
                                            color = Color.hsv(50f,0.4f,0.95f),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = String.format("%.1f", currUser?.distance ?: 0.0) + " m",
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "My Records:",
                                            color = Color.hsv(50f,0.4f,0.95f),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = currUser?.records.toString() + " wins",
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(48.dp))

                                // Edit Profile Button
                                if (authUser?.email.toString() == userMail) {
                                    Button(
                                        onClick = { editing = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFBB9753),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(horizontal = 32.dp)
                                    ) {
                                        Text(
                                            text = "Edit Profile",
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                        }else{
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Text Field for Name
                                var name by remember { mutableStateOf(currUser?.name ?: "") }
                                TextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Name") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )

                                // Text Field for Username
                                var username by remember { mutableStateOf(currUser?.username ?: "") }
                                TextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("Username") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )

                                // Dropdown for House Selection
                                var expanded by remember { mutableStateOf(false) }
                                var selectedHouse by remember { mutableStateOf(currUser?.house ?: "Choose House") }
                                val houses = listOf("Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin")

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clickable { expanded = true }
                                        .padding(16.dp)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedHouse,
                                        color = if (selectedHouse == "Choose House") Color.Gray else Color(0xFF66FF66)
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        houses.forEach { house ->
                                            DropdownMenuItem(modifier = Modifier
                                                .fillMaxWidth()  // Make the item take up the full width
                                                .padding(horizontal = 16.dp)
                                                .background(Color.White),text = {Text(text = house,modifier = Modifier.align(Alignment.CenterHorizontally))},onClick = {
                                                selectedHouse = house
                                                expanded = false
                                            })
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Existing Buttons
                                Button(
                                    onClick = { editing = false;
                                        userViewModel.updateUserHouse(currUser?.email ?: "", selectedHouse);
                                        userViewModel.updateUserName(currUser?.email ?: "", name);
                                        userViewModel.updateUserUsername(currUser?.email ?: "", username);
                                        Thread.sleep(800)
                                        navController.navigate(Screens.Profile.route
                                            .replace(
                                                oldValue = "{email}",
                                                newValue = currUser?.email ?: ""
                                            )
                                        )
                                              },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Green,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .padding(horizontal = 32.dp)
                                ) {
                                    Text(
                                        text = "Save Changes",
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { editing = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .padding(horizontal = 32.dp)
                                ) {
                                    Text(
                                        text = "Go Back",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}