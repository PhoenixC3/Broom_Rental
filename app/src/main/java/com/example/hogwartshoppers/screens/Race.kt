package com.example.hogwartshoppers.screens

import android.util.Log
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.example.hogwartshoppers.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.model.Race
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.RaceViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun Race(navController: NavController, friendEmail: String) {

    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }
    var friend by remember { mutableStateOf<User?>(null) }

    val raceViewModel: RaceViewModel = viewModel()
    var currRace by remember { mutableStateOf<Race?>(null) }
    var markerPosition by remember { mutableStateOf(LatLng(38.757969, -9.155979)) }

    var invitedCondition by remember { mutableStateOf<Boolean?>(null) }

    val db = FirebaseDatabase.getInstance()
    val racesRef = db.reference.child("Races")

    LaunchedEffect(authUser?.email.toString()) {
        userViewModel.getUserInfo(authUser?.email.toString()) { user ->
            currUser = user // Update currUser with the fetched data
        }

        userViewModel.getUserInfo(friendEmail) { user ->
            friend = user // Update friend with the fetched data
        }

        raceViewModel.getRace(authUser?.email.toString(), friendEmail) { race ->
            currRace = race
            if (currRace == null) {
                raceViewModel.getRace(friendEmail, authUser?.email.toString()) { race ->
                    currRace = race
                    markerPosition = LatLng(currRace!!.latitude, currRace!!.longitude)

                    racesRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            raceViewModel.getRace(friendEmail, authUser?.email.toString()) { race ->
                                currRace = race
                                if(currRace?.invite == true) {
                                    invitedCondition = true
                                }
                                else if(currRace?.invite == false) {
                                    invitedCondition = false
                                    raceViewModel.deleteRace(friendEmail, authUser?.email.toString()) { success ->
                                        if (success) {
                                            Log.d("Rejected", "Rejected invite and deleted race")
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.d("Race", "Error fetching race: ${error.message}")
                        }
                    })
                }
            }
            else {
                markerPosition = LatLng(currRace!!.latitude, currRace!!.longitude)

                racesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        raceViewModel.getRace(authUser?.email.toString(), friendEmail) { race ->
                            currRace = race
                            if (currRace?.invite == true) {
                                invitedCondition = true
                            } else if (currRace?.invite == false || currRace == null) {
                                invitedCondition = false
                                raceViewModel.deleteRace(authUser?.email.toString(), friendEmail) { success ->
                                    if (success) {
                                        Log.d("Rejected", "Rejected invite and deleted race")
                                    }
                                }
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.d("Race", "Error fetching race: ${error.message}")
                    }
                })
            }
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
                                modifier = Modifier
                                    .size(50.dp)
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
                        .padding(top = 80.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(350.dp, 70.dp) // Set specific width and height
                            .padding(bottom = 30.dp)
                            .background(
                                color = Color(0xff4b2f1b), // Brown background
                                shape = RoundedCornerShape(16.dp) // Makes corners rounded
                            ),
                        contentAlignment = Alignment.Center // Centers the text inside the box

                    ) {
                        Text(
                            text = "Race",
                            color = Color.White
                        )
                    }


                    Column(
                        modifier = Modifier
                            .size(350.dp, 500.dp)
                            .background(
                                Color(0xffe9dbc0),
                                shape = RoundedCornerShape(16.dp)
                            ), // Background and rounded corners
                        verticalArrangement = Arrangement.spacedBy(8.dp), // Adds space between buttons
                        horizontalAlignment = Alignment.CenterHorizontally // Centers the buttons horizontally
                    ) {
                        if(invitedCondition == true) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.SpaceEvenly, // Space images evenly
                                verticalAlignment = Alignment.CenterVertically // Align images vertically in the center
                            ) {
                                // First Image with texts below
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally // Center texts under the image
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.default_ahh),
                                        contentDescription = "Default Ahh",
                                        modifier = Modifier.size(100.dp)
                                            .clip(CircleShape) // Make the image circular
                                            .border(2.dp, Color(0xff321f12), CircleShape),
                                    )
                                    Text(text = "${currUser?.username}", fontSize = 12.sp, color = Color.Black) // First text
                                }

                                // Second image: "vs"
                                Image(
                                    painter = painterResource(id = R.drawable.vs),
                                    contentDescription = "VS",
                                    modifier = Modifier.size(75.dp)
                                )

                                // Second Image with texts below
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally // Center texts under the image
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.default_ahh),
                                        contentDescription = "Default Ahh",
                                        modifier = Modifier.size(100.dp)
                                            .clip(CircleShape) // Make the image circular
                                            .border(2.dp, Color(0xff321f12), CircleShape),
                                    )
                                    Text(text = "${friend?.username}", fontSize = 12.sp, color = Color.Black)
                                }
                            }

                            Text(
                                text = "Finish Line:",
                                color = Color(0xff4b2f1b),
                                fontSize = 18.sp
                            )

                            Box(
                                modifier = Modifier
                                    .size(200.dp)  // Adjust size of the square
                                    .padding(start = 16.dp, end = 16.dp)
                                    .background(Color(0xFFBB9753), shape = RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center  // Centers the text inside the square
                            ) {
                                val cameraPositionState = rememberCameraPositionState()
                                val markerPositionState = rememberMarkerState()

                                cameraPositionState.move(
                                    CameraUpdateFactory.newLatLngZoom(
                                        markerPosition,
                                        16f
                                    )
                                )

                                markerPositionState.position = markerPosition

                                GoogleMap(
                                    cameraPositionState = cameraPositionState,
                                    uiSettings = remember {
                                        com.google.maps.android.compose.MapUiSettings(
                                            zoomControlsEnabled = true,
                                            compassEnabled = true
                                        )
                                    },
                                    properties = remember {
                                        com.google.maps.android.compose.MapProperties(isMyLocationEnabled = true)
                                    },
                                    modifier = Modifier.matchParentSize()
                                ) {
                                    Marker(
                                        state = markerPositionState
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center, // Adds space between the buttons
                                verticalAlignment = Alignment.CenterVertically // Aligns buttons vertically
                            ) {
                                // First button (Cancel Race)
                                Button(
                                    onClick = {
                                        if(currRace?.userRace == authUser?.email.toString())
                                           raceViewModel.rejectInvite(authUser?.email.toString(), friendEmail) { success ->
                                               if (success) {
                                                   Log.d("Rejected", "Rejected invite")
                                               }
                                           }
                                        else {
                                            raceViewModel.rejectInvite(friendEmail, authUser?.email.toString()) { success ->
                                                if (success) {
                                                    Log.d("Rejected", "Rejected invite")
                                                }
                                            }
                                        }
                                        navController.navigate(
                                            Screens.HomeScreen.route
                                        )
                                    },
                                    modifier = Modifier
                                        .size(135.dp, 35.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe22134))
                                ) {
                                    Text("Cancel Race")
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = {
                                        navController.navigate(
                                            Screens.HomeScreen.route
                                        )
                                    },
                                    modifier = Modifier
                                        .size(135.dp, 35.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b))
                                ) {
                                    Text("Map")
                                }
                            }
                        }
                        else if(invitedCondition == false) {
                            Spacer(modifier = Modifier.height(200.dp))
                            Text(text = "The race is over",
                                color = Color.Black,
                                fontSize = 30.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
                            Text(text = "Your friend ended the race, no one wins",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))

                            Button(
                                onClick = {
                                    navController.navigate(
                                        Screens.HomeScreen.route
                                    )
                                },
                                modifier = Modifier
                                    .size(135.dp, 35.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b))
                            ) {
                                Text("Map")
                            }


                        }
                        else {
                            Spacer(modifier = Modifier.height(200.dp))
                            Text(text = "Waiting for your friend to accept the invite",
                                color = Color.Black,
                                fontSize = 30.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
                            Text(text = "The race will start as soon as your friend accepts the invite. Be prepared!",
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(10.dp))
                        }
                    }
                }
            }
        }
    }
}