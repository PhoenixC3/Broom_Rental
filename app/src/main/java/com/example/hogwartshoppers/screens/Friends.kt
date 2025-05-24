package com.example.hogwartshoppers.screens

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hogwartshoppers.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.model.Race
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.BroomViewModel
import com.example.hogwartshoppers.viewmodels.RaceViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(navController: NavController, acceptedRequest: Boolean) {
    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }

    var allUsers by remember { mutableStateOf<List<User>?>(null) }

    var friendsEmails by remember { mutableStateOf<List<String>?>(null) }
    var friendRequests by remember { mutableStateOf<List<String>?>(null) }
    var emailInput by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }


    LaunchedEffect(authUser?.email.toString()) {
        // Testar interacao dinamica (real time)
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val usersRef = db.getReference("Users")

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Collect all users from the snapshot
                val usersList = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = User(
                        username = child.child("username").value as? String ?: "",
                        email = child.child("email").value as? String ?: "",
                        name = child.child("name").value as? String ?: "",
                        house = child.child("house").value as? String ?: "",
                        distance = when (val distanceValue = child.child("distance").value) {
                            is Long -> distanceValue.toDouble()  // Convert Long to Double
                            is Double -> distanceValue          // Keep Double as is
                            else -> 0.0                         // Default value
                        },
                        records = (child.child("records").value as? Long)?.toInt() ?: 0,
                        flying = child.child("flying").value as? Boolean ?: false
                    )
                    usersList.add(user)
                }
                // Update the state variable with the collected user list
                allUsers = usersList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("User", "Error fetching Users: ${error.message}")
            }
        })

        userViewModel.getUserInfo(authUser?.email.toString()) { user ->
            currUser = user // Update currUser with the fetched data
        }

        userViewModel.getFriends(authUser?.email.toString()) { emails ->
            friendsEmails = emails
        }

        userViewModel.getFriendRequests(authUser?.email.toString()) { requests ->
            friendRequests = requests
        }
    }

    var selectedTab by remember { mutableStateOf(if (acceptedRequest) "Friend Requests" else "My Friends") }
    val switchPosition by animateDpAsState(
        targetValue = if (selectedTab == "My Friends") 0.dp else 200.dp, label = "" // Adjust width
    )


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
                        Menu(navController = navController, currUser?.email)
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
                            .padding(bottom = 16.dp)
                            .size(350.dp, 50.dp) // Set specific width and height
                            .background(
                                color = Color(0xff4b2f1b), // Brown background
                                shape = RoundedCornerShape(16.dp) // Makes corners rounded
                            ),
                        contentAlignment = Alignment.Center // Centers the text inside the box

                    ) {
                        Text(
                            text = "Friends List",
                            color = Color.White
                        )
                    }
                    // Tab buttons for "My Friends" and "Friend Requests"
                    // Tab Switcher Row
                    Box(
                        modifier = Modifier
                            .size(350.dp, 60.dp)
                            .padding(bottom = 8.dp)
                            .background(
                                color = Color(0xff321f12), // Background color for unselected area
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        // Moving switch (animated)
                        Box(
                            modifier = Modifier
                                .offset(x = switchPosition)
                                .size(150.dp, 60.dp) // Match button sizes
                                .background(
                                    color = Color(0xffBB9753), // Highlight color for the selected tab
                                    shape = RoundedCornerShape(16.dp)
                                )
                        )

                        // "My Friends" Button
                        Button(
                            onClick = { selectedTab = "My Friends" },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(150.dp, 60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = if (selectedTab == "My Friends") ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753))
                                    else ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b)),
                            elevation = ButtonDefaults.elevatedButtonElevation(0.dp)
                        ) {
                            Text(
                                text = "My Friends",
                                color = Color.White, // Text color based on state
                                modifier = Modifier.zIndex(1f)
                            )
                        }

                        // "Friend Requests" Button
                        Button(
                            onClick = { selectedTab = "Friend Requests" },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(150.dp, 60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = if (selectedTab == "Friend Requests") ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753))
                            else ButtonDefaults.buttonColors(containerColor = Color(0xff4b2f1b)),
                            elevation = ButtonDefaults.elevatedButtonElevation(0.dp)
                        ) {
                            Text(
                                text = "Friend Requests",
                                color = Color.White, // Text color based on state
                                modifier = Modifier.zIndex(1f)
                                )
                            }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {

                        Button(
                            onClick = {
                                showDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSent) Color.Green else Color(0xffBB9753) // Dynamic color
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (isSent) "Friend Request Sent!" else "Add Friend", // Dynamic text
                                color = Color.White
                            )
                        }

                        // Reset state after 2 seconds when `isSent` is true
                        if (isSent) {
                            LaunchedEffect(isSent) {
                                kotlinx.coroutines.delay(2000) // Wait for 2 seconds
                                isSent = false // Reset the button state
                            }
                        }


                    }

                    // Content Box
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(Color(0xff4b2f1b), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        // Display friends list when "My Friends" tab is selected
                        if (selectedTab == "My Friends") {

                            if (friendsEmails.isNullOrEmpty())
                                Text(
                                    text = "You have no friends",
                                    color = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                        .align(Alignment.Center))
                            else {
                                Box(
                                    modifier = Modifier.wrapContentSize().align(Alignment.TopCenter) // Adjusts the size to fit the content
                                ) {
                                    Column(
                                        modifier = Modifier.wrapContentSize(), // Ensures the Column is only as big as its content
                                        verticalArrangement = Arrangement.spacedBy(12.dp) // Add spacing between items
                                    ) {
                                        friendsEmails?.forEach { email ->
                                            val user = allUsers?.find { user -> user.email == email } // Find the user in allUsers
                                            val isFlying = user?.flying ?: false // Get the flying status or default to false

                                            FriendBox(
                                                userEmail = authUser?.email.toString(),
                                                email = email,
                                                isFlying = isFlying,
                                                navController = navController
                                            )
                                        }
                                    }
                                }

                            }
                        } else if (selectedTab == "Friend Requests") {
                            // Check if friendRequests is not null or empty
                            if (friendRequests.isNullOrEmpty()) {
                                // Display message when no friend requests
                                Text(
                                    text = "You have no friend requests",
                                    color = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                            .align(Alignment.Center)
                                )
                            } else {
                                // Display friend requests list when "Friend Requests" tab is selected
                                friendRequests?.let { requests ->
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        requests.forEach { requestEmail ->
                                            FriendRequestItem(
                                                requestEmail = requestEmail,
                                                navController = navController,
                                                userViewModel = userViewModel,
                                                email = authUser?.email.toString(),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Pop-up Dialog for entering email
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false }, // Dismiss on outside touch
                            title = {
                                Text(
                                    text = "Enter Friend's Email",
                                    color = Color.White // Title text color
                                )
                            },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Friend's Email") },
                                        isError = emailInput.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches(),
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.White)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (emailInput.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                                            "Please enter a valid email address"
                                        } else "",
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )

                                    // Display result message
                                    resultMessage?.let {
                                        Text(
                                            text = it,
                                            color = if (it == "Friend Request Sent!") Color.Green else Color.Red,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                var userExists = false
                                userViewModel.userWithEmailExists(emailInput) { exists ->
                                    userExists = exists
                                }
                                Button(
                                    onClick = {
                                        resultMessage = "Waiting for response..."
                                        if (emailInput.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() && emailInput != authUser?.email.toString()) {
                                            if ((friendsEmails?.contains(emailInput) == false || friendsEmails.isNullOrEmpty()) && userExists) {
                                                userViewModel.addFriendRequest(
                                                    email = emailInput,
                                                    friendEmail = authUser?.email.toString()
                                                ) { success ->
                                                    isSent = true
                                                    resultMessage = if (success) {
                                                        "Friend Request Sent!" // Success message
                                                    } else {
                                                        "" // Error message
                                                    }
                                                    showDialog =
                                                        false // Close the dialog after the action

                                                }
                                            }
                                            else if (userExists == false)
                                                resultMessage = "User does not exist"
                                            else {
                                                resultMessage = "You are already friends!"
                                                Log.d("UserExists", "User already friends")
                                            }
                                        }
                                        else {
                                            if(emailInput == authUser?.email.toString()) {
                                                resultMessage = "You can't add yourself as a friend!"
                                                Log.d("UserExists", "User already friends")
                                            }
                                            else {
                                                resultMessage = "Please enter a valid email address"
                                                Log.d("UserExists", "User does not exist")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xffBB9753) // Button background color
                                    )
                                ) {
                                    Text("Send Request", color = Color.White) // Button text color
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDialog = false },
                                        colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xffBB9753) // Button background color
                                        )
                                ){
                                    Text("Cancel", color = Color.White) // Button text color
                                }
                            },
                            shape = RoundedCornerShape(16.dp), // Rounded corners
                            containerColor = Color(0xff4b2f1b)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendBox(userEmail: String,email: String, isFlying: Boolean, navController: NavController, broomViewModel: BroomViewModel = viewModel()) {
    val userViewModel: UserViewModel = viewModel()
    val raceViewModel: RaceViewModel = viewModel()
    var friend by remember { mutableStateOf<User?>(null) }
    var userFlying by remember { mutableStateOf(false) }

    // Fetch the user's info for each friend
    LaunchedEffect(email) {
        userViewModel.getUserInfo(email) { user ->
            friend = user
        }

        userViewModel.getUserInfo(userEmail) { user ->
            userFlying = user?.flying == true
        }
    }

    friend?.let { f ->

        var isFriendRiding by remember { mutableStateOf(true) }
        var raceAlreadyExists by remember { mutableStateOf(false) }
        var showImageDialog by remember { mutableStateOf(false) }
        var friendRemoved by remember { mutableStateOf(false) }

        // First pop-up dialog
        if (showImageDialog) {
            AlertDialog(
                onDismissRequest = { showImageDialog = false },
                title = {
                    Text(text = "Are you sure you wanna remove ${f.username} from your friends list?",
                        color = Color.White)
                },
                text = {
                    Text("You won't be able to race or see this user's profile after this action.",
                        color = Color.White)
                },
                confirmButton = {
                    Button(onClick = {  showImageDialog = false
                                        userViewModel.removeFriend(userEmail, f.email) { success ->
                                             friendRemoved = success
                                             if (success) {
                                                 Thread.sleep(400)
                                                 navController.navigate(
                                                     Screens.Friends.route
                                                         .replace(
                                                             oldValue = "{email}",
                                                             newValue = userEmail
                                                         )
                                                         .replace(
                                                             oldValue = "{acceptedRequest}",
                                                             newValue = "false"
                                                         )
                                                 )
                                             } else {
                                                Log.d("FriendBox", "Failed to remove friend")
                                             }
                                        }
                                    },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffBB9753) // Button background color
                        ),
                        modifier = Modifier.fillMaxWidth(0.3f)) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showImageDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xffBB9753) // Button background color
                        ),
                        modifier = Modifier.fillMaxWidth(0.3f)) {
                        Text("No")
                    }
                },
                shape = RoundedCornerShape(16.dp), // Rounded corners
                containerColor = Color(0xff4b2f1b)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.red_x),
                    contentDescription = "X",
                    modifier = Modifier
                        .size(20.dp) // Set image size
                        .clickable {
                            showImageDialog = true
                        }
                        .align(Alignment.End)
                )
                // Row to hold the image, username, and name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular image of the friend
                    Image(
                        painter = painterResource(id = R.drawable.default_ahh),
                        contentDescription = "Friend Placeholder",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape) // Make the image circular
                            .border(2.dp, Color(0xff321f12), CircleShape), // Optional border for the circle
                        contentScale = ContentScale.Crop // Ensures the image fits within the circle
                    )

                    Column(
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        // Username
                        Text(
                            text = f.username, // Friend's username
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 8.dp) // Add space between username and name
                        )

                        // Name
                        Text(
                            text = f.name, // Friend's name
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button to navigate to their profile
                    Button(
                        onClick = {
                            // Navigate to their profile screen
                            navController.navigate("profile_screen/${f.email}")
                        },
                        modifier = Modifier.weight(1f), // Take equal space on each side of the Row
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "View Profile", color = Color.White)
                    }

                    // Spacer to add space between the buttons
                    Spacer(modifier = Modifier.width(16.dp))

                    // Button for "Challenge for Race"
                    Button(
                        onClick = {
                                if (isFlying && userFlying) {
                                    var races: List<Race>? = null
                                    raceViewModel.getRaces { races = it
                                        Log.d("Races", races.toString())
                                            // check if there is already a race with the user and friend
                                            val raceUser = races!!.find { it.userRace == userEmail && it.friendRace == f.email }
                                            val raceFriend = races!!.find { it.userRace == f.email && it.friendRace == userEmail }

                                            if (raceUser != null || raceFriend != null)
                                                // check if there is a race without invites = null
                                                if (raceUser?.invite != null || raceFriend?.invite != null)
                                                    raceAlreadyExists = true
                                                else {
                                                    // delete races in raceUser and raceFriend
                                                    if (raceUser != null)
                                                        raceViewModel.deleteRace(userEmail, f.email) { success ->
                                                            if (success) {
                                                                Log.d("Race Deleted", "Race deleted")
                                                            }
                                                            else
                                                                Log.e("Race Not Deleted", "Race not deleted")
                                                        }
                                                    if (raceFriend != null)
                                                        raceViewModel.deleteRace(f.email, userEmail) { success ->
                                                            if (success) {
                                                                Log.d("Race Deleted", "Race deleted")
                                                            }
                                                            else
                                                                Log.e("Race Not Deleted", "Race not deleted")
                                                        }
                                                    navController.navigate("race_conditions_screen/${f.email}")
                                                }
                                            else
                                                navController.navigate("race_conditions_screen/${f.email}")
                                        }
                                }
                                else {
                                  isFriendRiding = false
                                }
                        },
                        modifier = Modifier.weight(1f), // Take equal space on each side of the Row
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Race Friend", color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        userViewModel.curseUser(email)
                    },
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth(), // Button will take up full width of its parent
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // Make button's background transparent
                    shape = RoundedCornerShape(16.dp) // Apply rounded corners to the button
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.curse_texture),
                            contentDescription = "Button Background",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop,
                            colorFilter = if(!isFlying) { ColorFilter.colorMatrix(
                                ColorMatrix().apply { setToSaturation(0f) }
                                )} else null
                        )


                        Text(
                            text = "Curse",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                if(!isFriendRiding) {
                    Text(
                        text = "You and your friend need to be riding brooms to race!",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                if(raceAlreadyExists) {
                    Text(
                        text = "You already have a race with this friend!",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    requestEmail: String,
    navController: NavController,
    userViewModel: UserViewModel,
    email: String
) {
    var friendInfo by remember { mutableStateOf<User?>(null) }

    // Fetch the user's info
    LaunchedEffect(requestEmail) {
        userViewModel.getUserInfo(requestEmail) { user ->
            friendInfo = user
        }
    }

    friendInfo?.let { friend ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.default_ahh), // Placeholder image
                        contentDescription = "Friend Placeholder",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = friend.username,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Button to navigate to their profile
                    Button(
                        onClick = {
                            // Navigate to their profile screen
                            navController.navigate("profile_screen/${friend.email}")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "View Profile", color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Button to accept the friend request
                    Button(
                        onClick = {
                            // Accept the friend request
                            userViewModel.acceptFriendRequest(email, friend.email) { success ->
                                if (success) {
                                    navController.navigate(
                                        Screens.Friends.route
                                            .replace(
                                                oldValue = "{email}",
                                                newValue = email
                                            )
                                            .replace(
                                                oldValue = "{acceptedRequest}",
                                                newValue = "true"
                                            )
                                    )
                                } else {
                                    // Handle failure (e.g., show an error message)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffBB9753)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Accept", color = Color.White)
                    }
                }
            }
        }
    }
}