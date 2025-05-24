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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.model.Race
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.RaceViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun RaceConditions(navController: NavController, friendEmail: String) {

    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    val raceViewModel: RaceViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }
    var friend by remember { mutableStateOf<User?>(null) }
    var showPopup by remember { mutableStateOf(false) }
    var race by remember { mutableStateOf<Race?>(null) }
    var finishLine by remember { mutableStateOf("") }
    var alreadyInvited by remember { mutableStateOf(false) }
    var noCoordsSelected by remember { mutableStateOf(false) }

    LaunchedEffect(authUser?.email.toString()) {
        userViewModel.getUserInfo(authUser?.email.toString()) { user ->
            currUser = user // Update currUser with the fetched data
        }

        userViewModel.getUserInfo(friendEmail) { user ->
            friend = user // Update friend with the fetched data
        }

        raceViewModel.createRace(authUser?.email.toString(), friendEmail) {
            if (!it) {
                navController.navigate(
                    Screens.Friends.route
                        .replace(
                            oldValue = "{acceptedRequest}",
                            newValue = "false"
                        )
                )
            }
        }

        raceViewModel.getRace(authUser?.email.toString(), friendEmail) {
            race = it
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
                            .padding(start = 30.dp, top = 50.dp)
                            .size(60.dp),
                        containerColor = Color(0xff321f12),
                        contentColor = Color.White
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
                        .size(200.dp)
                        .padding(bottom = 15.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 150.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(350.dp, 70.dp)
                            .padding(bottom = 30.dp)
                            .background(
                                color = Color(0xff4b2f1b),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center

                    ) {
                        Text(
                            text = "Race Conditions",
                            color = Color.White
                        )
                    }


                    Column(
                        modifier = Modifier
                            .width(350.dp)
                            .fillMaxHeight()
                            .background(
                                Color(0xffe9dbc0),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Spacer(modifier = Modifier.height(14.dp))


                        Text(
                            text = "Note:",
                            color = Color(0xff4b2f1b),
                            modifier = Modifier.padding(start = 5.dp, end = 300.dp),
                            fontSize = 12.sp,
                            )

                        Text(
                            text = "If you want a fair race, make sure you and the person you invited" +
                                    " are both at the same location.",
                            color = Color(0xff4b2f1b),
                            modifier = Modifier.padding(start = 10.dp).offset(y = (-10).dp),
                            fontSize = 18.sp
                            )

                        Button(
                            onClick = { showPopup = true },
                            modifier = Modifier
                                .size(275.dp, 50.dp)
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff4b2f1b)
                            )
                        ) {
                            Text("Select finish line")
                        }

                        if(finishLine != "")
                            Text(text = "Selected finish line: $finishLine",
                                color = Color(0xff4b2f1b),)


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                Text(text = "${currUser?.username}",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    maxLines = 1, // Set the max number of lines for the text
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Second image: "vs"
                            Image(
                                painter = painterResource(id = R.drawable.vs),
                                contentDescription = "VS",
                                modifier = Modifier.size(75.dp)
                                    .align(Alignment.CenterVertically)
                                    .padding(start = 16.dp)
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
                                Text(text = "${friend?.username}",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    maxLines = 1, // Set the max number of lines for the text
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = {
                                raceViewModel.deleteRace(authUser?.email.toString(), friendEmail) {
                                    if(!it)
                                        Log.d("Error", "Error deleting race")
                                    else
                                        Log.d("Success", "Race deleted")
                                }
                                navController.navigate(
                                    Screens.Friends.route
                                        .replace(
                                            oldValue = "{acceptedRequest}",
                                            newValue = "false"
                                        )
                                )
                            },
                            modifier = Modifier
                                .size(275.dp, 35.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xffe22134)
                            )
                        ) {
                            Text("Cancel Race")
                        }

                        Button(
                            onClick = {


                                raceViewModel.getInvites() { invites ->
                                    var count = 0
                                    for (invite in invites) {
                                        if (invite.to == friendEmail) {
                                            count++
                                        }
                                    }

                                    raceViewModel.getRaceCoords(authUser?.email.toString(), friendEmail
                                    ) { coords ->
                                        // get the coords from the Pair and check if they are 0
                                        noCoordsSelected =
                                            if (coords != null) {
                                                coords.first == 0.0 && coords.second == 0.0
                                            } else {
                                                true
                                            }

                                        alreadyInvited = count > 0

                                        if (!noCoordsSelected && !alreadyInvited) {

                                            raceViewModel.inviteUser(
                                                authUser?.email.toString(),
                                                friendEmail
                                            ) { success ->
                                                if (success) {
                                                    navController.navigate("race_screen/${friendEmail}")
                                                } else {
                                                    Log.d("Error", "Error inviting user")
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(275.dp, 50.dp)
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff44ba3c)
                            )
                        ) {
                            Text(text = "Start race against ${friend?.username}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                        }

                        if(noCoordsSelected) {
                            Text(
                                text = "You need to select a finish line!",
                                color = Color.Red,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .align(Alignment.CenterHorizontally),
                                fontSize = 12.sp
                            )
                        } else if(alreadyInvited) {
                            Text(
                                text = "Looks like this user was already invited to another race\n " +
                                        "Wait for them to reject the invite or finish the race",
                                color = Color.Red,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .align(Alignment.CenterHorizontally),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Popup appears when the button is clicked
            if (showPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center)
                            .padding(16.dp)
                                .border(3.dp, Color(0xffBB9753), shape = RoundedCornerShape(16.dp))
                            .background(Color(0xff4b2f1b),
                                shape = RoundedCornerShape(16.dp)),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val finishLines = listOf("Quidditch Pitch", "Forbidden Forest", "Hogsmeade Station","Hogwarts Castle")

                            Text(text ="Select one of the following:",
                                color = Color.White,
                                fontSize = 20.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                                    .padding(bottom = 10.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                items(finishLines.size) { line ->
                                    Button(
                                        onClick = {
                                            var latitude = 0.0
                                            var longitude = 0.0
                                            raceViewModel.getFinishCoords(finishLines[line]) {
                                                if (it != null) {
                                                    latitude = it.first
                                                    longitude = it.second
                                                }
                                                Log.d("latitute", latitude.toString())
                                                Log.d("longitude", longitude.toString())
                                                raceViewModel.updateCoordsRace(user = authUser?.email.toString(),friendEmail, latitude, longitude) {
                                                    if(!it)
                                                        Log.d("Error", "Error updating coords")
                                                    else
                                                        Log.d("Success", "Coords updated")
                                                        finishLine = finishLines[line]

                                                }
                                            }
                                            showPopup = false // Close popup after selection
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xffBB9753)
                                        )
                                    ) {
                                        Text(finishLines[line])
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Close button for the popup
                            Button(
                                onClick = { showPopup = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xffe22134))
                            ) {
                                Text("Close")
                            }
                        }
                    }
                }
            }
        }
    }
}