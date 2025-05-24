package com.example.hogwartshoppers.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.R
import com.example.hogwartshoppers.model.Broom
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.BroomViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun BroomDetailsScreen(navController: NavController, selectedBroomName: String) {
    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }
    val viewModel = BroomViewModel()

    var selectedBroom by remember { mutableStateOf<Broom?>(null) }

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
                            .padding(start = 30.dp, top = 50.dp) // Adjust position on the screen
                            .size(60.dp), // Make the button larger for better content alignment
                        containerColor = Color(0xff321f12), // Brown background for the button
                        contentColor = Color.White // White color for the content inside
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
                            .size(200.dp) // Logo size remains the same
                            .padding(bottom = 15.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Broom details title
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(text = "Broom details", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Broom details
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        DetailItem(
                            label = selectedBroomName,
                            imageRes = R.drawable.broom_details,
                            imageSize = 100.dp,
                            fontSize = 18.sp,
                            textWidth = 140.dp
                        )
                        selectedBroom?.let {
                            DetailItem(
                                label = it.category,
                                imageRes = R.drawable.speed_details,
                                imageSize = 100.dp,
                                fontSize = 18.sp,
                                textWidth = 140.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        DetailItem(
                            label = selectedBroom?.distance.toString() + " m",
                            imageRes = R.drawable.km_details,
                            imageSize = 100.dp,
                            fontSize = 18.sp,
                            textWidth = 140.dp
                        )
                        DetailItem(
                            label = selectedBroom?.price.toString() + " Gal/Min",
                            imageRes = R.drawable.money_details,
                            imageSize = 100.dp,
                            fontSize = 18.sp,
                            textWidth = 140.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Buttons
                    Button(
                        onClick = {
                            navController.navigate(Screens.CustomizeBroom.route
                                .replace(
                                    oldValue = "{broom}",
                                    newValue = selectedBroomName
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB9753)),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Customize Broom", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val context = LocalContext.current

                    Button(
                        onClick = { selectedBroom?.let { rentBroom(navController, authUser?.email.toString(), it, viewModel, context) } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB9753)),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(text = "Alohomora", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

fun rentBroom(controller: NavController, email: String, broom: Broom, viewModel: BroomViewModel, context: Context) {
    viewModel.checkAvailable(broom.name) { res ->
        if (res) {
            viewModel.startTrip(email, broom.name)

            controller.navigate(Screens.HomeScreen.route
                .replace(
                    oldValue = "{email}",
                    newValue = email
                )
            )
        }
        else {
            MaterialAlertDialogBuilder(context)
                .setTitle("Unable to Start Trip")
                .setMessage("This broom was already rented.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()

                    controller.navigate(Screens.HomeScreen.route
                        .replace(
                            oldValue = "{email}",
                            newValue = email
                        )
                    )
                }
                .show()
        }
    }
}

@Composable
fun DetailItem(label: String, imageRes: Int, imageSize: Dp, fontSize: TextUnit, textWidth: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.size(140.dp)
    ) {
        // Image for the detail item
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = label,
            modifier = Modifier
                .size(imageSize)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(textWidth)
        )
    }
}
