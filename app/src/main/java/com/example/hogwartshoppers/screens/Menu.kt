package com.example.hogwartshoppers.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hogwartshoppers.R

@Composable
fun Menu(navController: NavController, currUserEmail: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Add spacing between buttons
    ) {
        Button(
            onClick = {
                navController.navigate(
                    Screens.HomeScreen.route
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map_logo),
                    contentDescription = "Map Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .padding(end = 12.dp)
                        .align(Alignment.CenterVertically)
                        .offset(x = (-5).dp)
                )

                Text(
                    text = "Map",
                    style = TextStyle(fontSize = 18.sp), // Increases the font size
                    modifier = Modifier.offset(x = (-9).dp)
                )
            }
        }

        Button(
            onClick = {
                navController.navigate(
                    Screens.Friends.route
                        .replace(
                            oldValue = "{acceptedRequest}",
                            newValue = "false"
                        )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.friends_logo),
                    contentDescription = "Profile Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 12.dp)
                        .align(Alignment.CenterVertically)
                        .offset(x = (-5).dp)
                )

                Text(
                    text = "Friends",
                    style = TextStyle(fontSize = 18.sp), // Increases the font size
                    modifier = Modifier.offset(x = (-9).dp)
                )
            }
        }

        Button(onClick = {
            navController.navigate(Screens.Profile.route
                .replace(
                    oldValue = "{email}",
                    newValue = currUserEmail.toString()
                )
            )
        },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = "Profile",
                    style = TextStyle(fontSize = 18.sp) // Increases the font size
                )
            }
        }

        Button(onClick = {
            navController.navigate(Screens.TripHistory.route
            )
        },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.trip_history_logo),
                    contentDescription = "Trip History Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = "Trip History",
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.offset(x = (-5).dp)
                )
            }
        }

        Button(onClick = {
            navController.navigate(Screens.Forum.route)
        },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.forum_logo),
                    contentDescription = "Forum Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = "Forum",
                    style = TextStyle(fontSize = 18.sp) ,// Increases the font size
                    modifier = Modifier.offset(x = (-6).dp)
                )
            }
        }

        Button(onClick = {
            navController.navigate(Screens.Settings.route)
        },
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff321f12) // Set the button background color
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(end = 16.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = "Settings",
                    style = TextStyle(fontSize = 18.sp) // Increases the font size
                )
            }
        }
    }
}
