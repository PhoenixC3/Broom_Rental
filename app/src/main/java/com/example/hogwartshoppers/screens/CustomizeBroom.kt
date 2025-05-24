package com.example.hogwartshoppers.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.R
import com.example.hogwartshoppers.model.Broom
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.BroomViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CustomizeBroomScreen(navController: NavController, selectedBroomName: String) {
    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }
    val viewModel = BroomViewModel()

    var selectedBroom by remember { mutableStateOf<Broom?>(null) }

    var selectedColor by remember { mutableStateOf("Red") }
    var selectedAccessory by remember { mutableStateOf("None") }
    var selectedCharm by remember { mutableStateOf("None") }
    var selectedSize by remember { mutableStateOf("Medium") }

    val broomColor = when (selectedColor) {
        "Red" -> Color.Red
        "Blue" -> Color.Blue
        "Green" -> Color.Green
        "Yellow" -> Color.Yellow
        else -> Color.Red
    }

    viewModel.getBroom(selectedBroomName) { broom ->
        if (broom != null) {
            selectedBroom = broom

            println("Broom retrieved: ${broom.name}")
        } else {
            println("Broom not found!")
        }
    }

    LaunchedEffect(authUser?.email.toString()) {
        userViewModel.getUserInfo(authUser?.email.toString()) { user ->
            currUser = user
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
                            .size(200.dp)
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
                            color = Color.White,
                            thickness = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 150.dp)
                        )

                        Text(
                            text = "Welcome " + currUser?.username,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Menu(navController = navController, currUserEmail = currUser?.email)
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
                            .padding(start = 30.dp, top = 50.dp)
                            .size(60.dp),
                        containerColor = Color(0xff321f12),
                        contentColor = Color.White
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xff321f12))
                    .padding(innerPadding)
                    .border(3.dp, Color(0xFFBB9753))
                    .verticalScroll(rememberScrollState()),
                color = Color(0xff321f12)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.hogwartslogo),
                        contentDescription = "Hogwarts Logo",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(200.dp)
                            .padding(bottom = 15.dp)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Title
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(text = "Customize Broom", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Broom Image
                    Image(
                        painter = painterResource(id = R.drawable.nimbus_2000),
                        contentDescription = "Broom",
                        modifier = Modifier.size(200.dp),
                        colorFilter = ColorFilter.tint(broomColor)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Customization options with dropdown selection
                    CustomizationOption("Color", selectedColor, listOf("Red", "Blue", "Green", "Yellow")) {
                        selectedColor = it
                    }
                    CustomizationOption("Accessories", selectedAccessory, listOf("None", "Golden Handle", "Silver Bristles")) {
                        selectedAccessory = it
                    }
                    CustomizationOption("Charms", selectedCharm, listOf("None", "Speed Boost", "Invisibility")) {
                        selectedCharm = it
                    }
                    CustomizationOption("Size", selectedSize, listOf("Small", "Medium", "Large")) {
                        selectedSize = it
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            navController.navigate(Screens.BroomDetails.route
                                .replace(
                                    oldValue = "{broom}",
                                    newValue = selectedBroomName
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                    ) {
                        Text(text = "Confirm", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizationOption(title: String, selectedValue: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8A165))
        ) {
            Text(text = title, color = Color.Black)
        }

        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopStart)
        ) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier.wrapContentWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            ) {
                Text(text = selectedValue, color = Color.Black, maxLines = 1)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}