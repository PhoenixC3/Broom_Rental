package com.example.hogwartshoppers.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.example.hogwartshoppers.R
import com.example.hogwartshoppers.model.Broom
import com.example.hogwartshoppers.model.BroomTrip
import com.example.hogwartshoppers.model.Race
import com.example.hogwartshoppers.model.User
import com.example.hogwartshoppers.viewmodels.AudioViewModel
import com.example.hogwartshoppers.viewmodels.BroomViewModel
import com.example.hogwartshoppers.viewmodels.RaceViewModel
import com.example.hogwartshoppers.viewmodels.UserViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapScreen(navController: NavController) {

    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val userViewModel: UserViewModel = viewModel()
    var currUser by remember { mutableStateOf<User?>(null) }

    val raceViewModel: RaceViewModel = viewModel()
    var currRace by remember { mutableStateOf<Race?>(null) }

    var curse by remember { mutableStateOf(false) }

    var showDialogEndRaceWin by remember { mutableStateOf(false) }
    var showDialogEndRaceLose by remember { mutableStateOf(false) }

    var invited by remember { mutableStateOf(false) }
    var raceOver by remember { mutableStateOf(false) }
    var whoInvited by remember { mutableStateOf("") }
    var userInvited by remember { mutableStateOf("") }

    // Pulsing Box logic here
    var sizeState by remember { mutableStateOf(true) }
    // State to control the offset of the shaking Box
    var offsetCurse by remember { mutableStateOf(0.dp) }

    val size by animateDpAsState(
        targetValue = if (sizeState) 415.dp else 375.dp,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
    )

    // Toggles the size every second
    LaunchedEffect(curse) {
        while (curse) {
            delay(1000)
            sizeState = !sizeState
        }
    }

    // Shake logic: this LaunchedEffect will trigger when 'curse' is true
    LaunchedEffect(curse) {
        if (curse) {
            // Shake the box repeatedly by changing the offset
            // Use a `while` loop but respect the `curse` value toggling
            while (curse) {
                delay(100)
                offsetCurse = 5.dp
                delay(100)
                offsetCurse = -5.dp
            }
        } else {
            offsetCurse = 0.dp // Reset offset when 'curse' is false
        }
    }

    LaunchedEffect(authUser?.email.toString()) {

        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val magicRef = db.getReference("Magic")
        val invitesRef = db.getReference("Race_Invites")
        val racesRef = db.getReference("Races")

        userViewModel.getUserInfo(authUser?.email.toString()) { user ->
            currUser = user // Update currUser with the fetched data

            //get ongoing race with authuser
            raceViewModel.getOngoingRace(currUser?.email ?: "") { race ->
                currRace = race

                invitesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        // Iterate through the children in "Race_Invites"
                        for (child in snapshot.children) {
                            userInvited = child.child("to").value as String
                            whoInvited = child.child("from").value as String
                            if (userInvited == authUser?.email) {
                                if (currRace?.invite == null) {
                                    invited = true // Update event variable
                                } else if (currRace?.invite == false) {
                                    raceOver = true
                                }
                                break // Exit the loop once a match is found
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors if necessary
                    }
                })

                racesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                            raceViewModel.getOngoingRace(currUser?.email ?: "") { race ->
                                currRace = race
                                if (currRace?.finished == true) {
                                    if (currRace!!.winner == authUser?.email.toString()) {
                                        userViewModel.updateUserRecords(authUser?.email.toString())
                                        showDialogEndRaceWin = true
                                    }
                                    else {
                                        showDialogEndRaceLose = true
                                    }

                                    raceOver = true
                                }
                                else {
                                    raceOver = false
                                }
                            }
                        }
                    override fun onCancelled(error: DatabaseError) {
                        // Handle errors if necessary
                    }
                })

            }
        }

        magicRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Iterate through the children in "Magic"
                for (child in snapshot.children) {
                    val toValue = child.child("to").value as? String
                    if (toValue == authUser?.email) {
                        curse = true // Update event variable
                        break // Exit the loop once a match is found
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("Magic", "Error fetching magic: ${error.message}")
            }
        })
    }

    val context = LocalContext.current

    var speed by remember { mutableStateOf(0f) }
    var smoothedSpeed by remember { mutableStateOf(0f) }
    var lastLocation by remember { mutableStateOf<Location?>(null) }
    var lastTime by remember { mutableStateOf(0L) }

    // Smooth the speed using a low-pass filter
    LaunchedEffect(speed) {
        smoothedSpeed = smoothedSpeed * 0.8f + speed * 0.2f
    }

    // State to store user's location
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedMarker by remember { mutableStateOf<Broom?>(null) }

    val sharedPreferences = context.getSharedPreferences("TimerPrefs", Context.MODE_PRIVATE)
    var totalDistance by remember {
        mutableStateOf(sharedPreferences.getFloat("totalDistance", 0f).toDouble())
    }
    var previousLocation: LatLng? by remember { mutableStateOf(null) }
    val editor = sharedPreferences.edit()

    var permissionsGranted by remember { mutableStateOf(false) }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionsGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Check and request location permissions
    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            // Fetch user's location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                    }
                }

                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 500L
                ).setMinUpdateIntervalMillis(300L).build()

                val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                        val newLocation = locationResult.lastLocation
                        val currentTime = System.currentTimeMillis()

                        locationResult.locations.lastOrNull()?.let { location ->
                            val currentLocation = LatLng(location.latitude, location.longitude)
                            userLocation = currentLocation

                            if (previousLocation != null) {
                                totalDistance += calculateDistance(previousLocation!!, currentLocation)

                                editor.putFloat("totalDistance", totalDistance.toFloat()).apply()
                            }

                            previousLocation = currentLocation
                        }

                        if (lastLocation != null && newLocation != null) {
                            val distance = lastLocation!!.distanceTo(newLocation) // Distance in meters
                            val timeElapsed = (currentTime - lastTime) / 1000f // Time in seconds
                            speed = if (timeElapsed > 0) (distance / timeElapsed) * 3.6f else 0f // Convert to km/h
                        }

                        lastLocation = newLocation
                        lastTime = currentTime

                        if (currRace != null) {
                            if (!currRace!!.finished) {
                                if (lastLocation?.let { hasUserReachedTarget(it.latitude, it.longitude, currRace!!.latitude, currRace!!.longitude, 5f) } == true) {
                                    raceViewModel.finishRace(currRace!!.userRace, currRace!!.friendRace, authUser?.email.toString()) { ret ->
                                        if (ret) {
                                            if (currRace!!.winner == authUser?.email.toString()) {
                                                userViewModel.updateUserRecords(authUser?.email.toString())
                                                showDialogEndRaceWin = true
                                            }
                                            else {
                                                showDialogEndRaceLose = true
                                            }

                                            raceOver = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            } catch (e: SecurityException) {
                Log.e("LocationScreen", "Permission denied or revoked: ${e.message}")
            }
        }
    }

    LaunchedEffect(speed) {
        smoothedSpeed = smoothedSpeed * 0.9f + speed * 0.1f
    }

    var pressure by remember { mutableStateOf(0f) } // State to store pressure value
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    DisposableEffect(Unit) {
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
                    pressure = event.values[0] // Atmospheric pressure in hPa
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (pressureSensor != null) {
            sensorManager.registerListener(
                sensorEventListener,
                pressureSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
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
                    modifier = Modifier.fillMaxSize().offset(x = offsetCurse) // Apply the shake offset
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
                        containerColor = if (curse) Color(0xff324e3b) else Color(0xff321f12), // Brown background for the button
                        contentColor = Color.White // White color for the content inside
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            selectedMarker = null
                        }
                    }
            ) {
                var hasTrip by remember { mutableStateOf<Boolean?>(false) }

                // Show the map only if location is available
                if (userLocation != null) {
                    hasTrip?.let {
                        ShowGoogleMap(
                            userLocation = userLocation!!,
                            onMarkerClick = { broom ->
                                selectedMarker = broom
                            },
                            broomVm = BroomViewModel(),
                            hasTrip = it,
                            curse = curse,
                            race = currRace,
                            offsetCurse = offsetCurse
                        )
                    }
                }

                val viewmodel = BroomViewModel()
                var currTrip by remember { mutableStateOf<BroomTrip?>(null) }

                viewmodel.getLastTrip(currUser?.email.toString()) { trip ->
                    if (trip != null) {
                        if (trip.active) {
                            hasTrip = true
                            currTrip = trip
                        }
                    }
                }

                if (hasTrip == true) {

                    var startTime by remember {
                        mutableStateOf(sharedPreferences.getLong("startTime", -1L))
                    }
                    var timer by remember { mutableStateOf(0L) }
                    val coroutineScope = rememberCoroutineScope()

                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    val locationCallback = remember {
                        object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)

                                locationResult.locations.lastOrNull()?.let { location ->
                                    val currentLocation = LatLng(location.latitude, location.longitude)
                                    userLocation = currentLocation

                                    if (previousLocation != null) {
                                        totalDistance += calculateDistance(previousLocation!!, currentLocation)

                                        editor.putFloat("totalDistance", totalDistance.toFloat()).apply()
                                    }

                                    previousLocation = currentLocation
                                }

                                if (currRace != null) {
                                    if (!currRace!!.finished) {
                                        if (lastLocation?.let { hasUserReachedTarget(it.latitude, it.longitude, currRace!!.latitude, currRace!!.longitude, 5f) } == true) {
                                            raceViewModel.finishRace(currRace!!.userRace, currRace!!.friendRace, authUser?.email.toString()) { ret ->
                                                if (ret) {
                                                    if (currRace!!.winner == authUser?.email.toString()) {
                                                        userViewModel.updateUserRecords(authUser?.email.toString())
                                                        showDialogEndRaceWin = true
                                                    }
                                                    else {
                                                        showDialogEndRaceLose = true
                                                    }

                                                    raceOver = true
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(currTrip) {
                        if (currTrip != null && startTime == -1L) {
                            startTime = System.currentTimeMillis()
                            editor.putLong("startTime", startTime).apply() // Save the start time

                            // Start location updates
                            val locationRequest = LocationRequest.create().apply {
                                interval = 1000 // 1 second interval
                                fastestInterval = 500
                                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                            }

                            try {
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
                            } catch (e: SecurityException) {
                                Log.e("LocationScreen", "Permission denied or revoked: ${e.message}")
                            }
                        }
                    }

                    // Update the timer
                    LaunchedEffect(startTime) {
                        coroutineScope.launch {
                            while (true) {
                                delay(1000)
                                if (startTime != -1L) {
                                    timer = (System.currentTimeMillis() - startTime) / 1000
                                }
                            }
                        }
                    }

                    // Formato timer as HH:mm:ss
                    val formattedTime = remember(timer) {
                        val hours = timer / 3600
                        val minutes = (timer % 3600) / 60
                        val seconds = timer % 60
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    }

                    //AUDIOPLAYER
                    val audioViewModel = AudioViewModel()
                    var url by remember { mutableStateOf<String?>(null) }
                    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                    var isPlaying by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        url = currTrip?.let { audioViewModel.fetchAudioFile(it.broomName) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(offsetCurse)

                        ) {
                            if (url != null) {
                                Button(
                                    onClick = {
                                        if (isPlaying) {
                                            mediaPlayer?.pause()
                                            isPlaying = false
                                        } else {
                                            if (mediaPlayer == null) {
                                                mediaPlayer = MediaPlayer().apply {
                                                    setOnErrorListener { mp, what, extra ->
                                                        Log.e("MediaPlayer", "Error occurred: what=$what extra=$extra")
                                                        true
                                                    }
                                                    setOnCompletionListener {
                                                        // Reset state when audio finishes
                                                        isPlaying = false
                                                        Log.e("MediaPlayer", "Playback completed")
                                                    }
                                                    setDataSource(url)
                                                    prepareAsync()
                                                    setOnPreparedListener {
                                                        start()
                                                        isPlaying = true
                                                    }
                                                }
                                            } else {
                                                mediaPlayer?.start()
                                                isPlaying = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (curse) Color(0xff324e3b) else Color(0xff321f12),
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(text = if (isPlaying) "Pause Broom Lore" else "Play Broom Lore",
                                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
                                }
                            } else {
                                Log.e("AudioViewModel", "No URL found for broomName: ${currTrip?.broomName}")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (curse) {
                        Box(
                            modifier = Modifier
                                .size(size)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center,

                            ) {

                            Box(
                                modifier = Modifier
                                    .background(Color.Transparent, shape = RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center // Ensures everything inside is centered
                            ) {
                                // Image
                                Image(
                                    painter = painterResource(id = R.drawable.death_mark),
                                    contentDescription = "Death Mark Inside Box",
                                    modifier = Modifier.fillMaxSize(), // Ensures the image fills the Box
                                    contentScale = ContentScale.Crop,
                                    alpha = 0.7f
                                )

                                // Overlay Texts in a Column (Centered)
                                Box(
                                    modifier = Modifier.fillMaxSize(), // Ensures the column overlays the image
                                    contentAlignment = Alignment.Center // Center the Column inside the Box
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp) // Space between texts
                                    ) {
                                        Text(
                                            text = "You are being cursed! SHAKE YOUR PHONE!",
                                            color = Color.Black,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )

                                        var timeLeft by remember { mutableStateOf(10) }

                                        LaunchedEffect(timeLeft) {
                                            while (timeLeft > 0) {
                                                delay(1000L)
                                                timeLeft--
                                            }

                                            if (timeLeft == 0) {
                                                val userViewmodel = UserViewModel()

                                                userViewmodel.removeCurse(authUser?.email.toString()) {
                                                    curse = false

                                                    if (currRace != null) {
                                                        if (!currRace!!.finished) {
                                                            val winner = if (authUser?.email.toString() == currRace!!.friendRace) {
                                                                currRace!!.userRace
                                                            } else {
                                                                currRace!!.friendRace
                                                            }

                                                            raceViewModel.finishRace(currRace!!.userRace, currRace!!.friendRace, winner) { ret ->
                                                                if (ret) {
                                                                    if (currRace!!.winner == authUser?.email.toString()) {
                                                                        userViewModel.updateUserRecords(authUser?.email.toString())
                                                                        showDialogEndRaceWin = true
                                                                    }
                                                                    else {
                                                                        showDialogEndRaceLose = true
                                                                    }

                                                                    raceOver = true
                                                                }

                                                                viewmodel.endTrip(authUser?.email.toString(), totalDistance, userLocation!!, context) { ret ->
                                                                    if (ret) {
                                                                        hasTrip = false

                                                                        editor.remove("startTime").apply()
                                                                        startTime = -1L
                                                                        timer = 0L

                                                                        if (isPlaying) {
                                                                            mediaPlayer?.pause()
                                                                            isPlaying = false
                                                                        }

                                                                        previousLocation = null
                                                                        totalDistance = 0.0
                                                                        editor.putFloat("totalDistance", 0f).apply()
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Time Left: $timeLeft",
                                            color = Color.Black,
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        ShakeDetector { shaken ->
                            if (shaken) {
                                val userViewmodel = UserViewModel()
                                userViewmodel.removeCurse(authUser?.email.toString()) { }
                                curse = false
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = offsetCurse) // Apply the shake offset
                    ) {
                        // Background to intercept clicks
                        Box(
                            modifier = Modifier.fillMaxSize()
                        )

                        // Overlay content
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(
                                    color = if (curse) Color(0xff324e3b) else Color(0xff321f12), // Brown background
                                    shape = RoundedCornerShape(16.dp) // Rounded corners
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            // Broom details
                            currTrip?.let {
                                Text(
                                    text = it.broomName,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .align(Alignment.TopCenter)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .fillMaxWidth(),

                                ) {
                                Box(
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = if (curse) R.drawable.background_for_broom_curse else R.drawable.background_for_broom),
                                        contentDescription = "Broom Background",
                                        modifier = Modifier.fillMaxSize(),
                                    )

                                    Image(
                                        painter = painterResource(id = R.drawable.nimbus_2000), // Overlay image
                                        contentDescription = "Broom Overlay",
                                        modifier = Modifier
                                            .size(90.dp) // Overlay image size
                                            .align(Alignment.Center) // Center over the background
                                    )
                                }

                                // Details section (time and distance)
                                Column {
                                    // Time Row
                                    Row(
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.time_trip),
                                            contentDescription = "Time Trip Logo",
                                            modifier = Modifier
                                                .size(40.dp) // Adjust icon size
                                                .padding(start = 16.dp, end = 4.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                        Text(
                                            text = formattedTime,
                                            color = Color.White,
                                            fontSize = 18.sp, // Adjust text size as needed
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }

                                    // Distance Row
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.offset(y = (-12).dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.m_logo),
                                            contentDescription = "Distance Logo",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .padding(start = 16.dp, end = 4.dp)
                                                .align(Alignment.CenterVertically)
                                        )
                                        Text(
                                            text = String.format("%.1f", totalDistance),
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                        }

                        // Finish Trip button
                        Button(
                            onClick = {

                                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            userLocation = LatLng(location.latitude, location.longitude)

                                            // Move this inside to ensure userLocation is updated before calling endTrip
                                            viewmodel.endTrip(authUser?.email.toString(), totalDistance, userLocation!!, context) { ret ->
                                                if (ret) {
                                                    hasTrip = false

                                                    // Clear the start time and reset the timer
                                                    editor.remove("startTime").apply()
                                                    startTime = -1L
                                                    timer = 0L

                                                    totalDistance = 0.0
                                                    previousLocation = null
                                                    editor.putFloat("totalDistance", 0f).apply()

                                                    navController.navigate(Screens.Camera.route)
                                                }
                                            }
                                        } else {
                                            Log.e("LocationError", "Failed to get location")
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    Log.e("LocationScreen", "Permission denied or revoked: ${e.message}")
                                }

                                if (currRace != null) {
                                    if (!currRace!!.finished) {
                                        val winner = if (authUser?.email.toString() == currRace!!.friendRace) {
                                            currRace!!.userRace
                                        } else {
                                            currRace!!.friendRace
                                        }

                                        raceViewModel.finishRace(currRace!!.userRace, currRace!!.friendRace, winner) { ret ->
                                            if (ret) {
                                                if (currRace!!.winner == authUser?.email.toString()) {
                                                    userViewModel.updateUserRecords(authUser?.email.toString())
                                                    showDialogEndRaceWin = true
                                                }
                                                else {
                                                    showDialogEndRaceLose = true
                                                }

                                                raceOver = true
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .offset(y = (-16).dp)
                                .background(
                                    color = if (curse) Color(0xffe0eedd) else Color(0xFFDBC7A1),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                        16.dp
                                    )
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (curse) Color(0xffe0eedd) else Color(0xFFDBC7A1) // Match image color
                            )
                        ) {
                            Text(
                                text = "Finish Trip",
                                color = Color(0xFF321F12),
                                fontSize = 18.sp,
                            )
                        }
                    }

                    if(invited) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = offsetCurse)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(16.dp)
                                    .offset(y = (90).dp)
                                    .background(color = if (curse) Color(0xff324e3b) else Color(0xff321f12),
                                        shape = RoundedCornerShape(16.dp))

                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                ) {
                                    var name by remember { mutableStateOf("") }
                                    userViewModel.getUserInfo(whoInvited) { user ->
                                        name = user?.username ?: ""
                                    }
                                    Text(text = "You have received an invite to a race from $name",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        modifier = Modifier.align(Alignment.CenterHorizontally))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Button(
                                            onClick = {
                                                raceViewModel.acceptInvite(whoInvited, authUser?.email.toString()) { success ->
                                                    if (success) {
                                                        invited = false
                                                        navController.navigate("race_screen/${whoInvited}")
                                                    }
                                                }
                                                      },
                                            colors = ButtonDefaults.buttonColors(if (curse) Color(0xffe0eedd) else Color(0xFFDBC7A1)),
                                            shape = RoundedCornerShape(16.dp),
                                        ) {
                                            Text(text = "Accept",
                                                color = Color(0xff321f12),
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(5.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))

                                        Button(
                                            onClick = {
                                                raceViewModel.rejectInvite(whoInvited, authUser?.email.toString()) { success ->
                                                    if (success) {
                                                        invited = false
                                                    }
                                                }


                                                         },
                                            colors = ButtonDefaults.buttonColors(if (curse) Color(0xffe0eedd) else Color(0xFFDBC7A1)),
                                            shape = RoundedCornerShape(16.dp),
                                        ) {
                                            Text(
                                                text = "Decline",
                                                color = Color(0xff321f12),
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(5.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showDialogEndRaceWin) {
                        Thread.sleep(400)

                        currRace?.let { race ->
                            raceViewModel.removeRaceInvite(race.userRace, race.friendRace) { }
                            raceViewModel.deleteRace(race.userRace, race.friendRace) { }
                        }

                        AlertDialog(
                            onDismissRequest = { showDialogEndRaceWin = false
                                               showDialogEndRaceLose = false},
                            containerColor = Color(0xFF3B2A1A),
                            shape = RoundedCornerShape(16.dp),
                            title = {
                                Text(
                                    text = "YOU WON THE RACE",
                                    color = Color.White,
                                    maxLines = 1
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDialogEndRaceWin = false
                                        showDialogEndRaceLose = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD7B98E),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Text(text = "Close")
                                }
                            },
                            modifier = Modifier.wrapContentWidth()
                        )
                    } else if (showDialogEndRaceLose) {
                        Thread.sleep(400)

                        currRace?.let { race ->
                            raceViewModel.removeRaceInvite(race.userRace, race.friendRace) { }
                            raceViewModel.deleteRace(race.userRace, race.friendRace) { }
                        }

                        AlertDialog(
                            onDismissRequest = { showDialogEndRaceWin = false
                                                showDialogEndRaceLose = false},
                            containerColor = Color(0xFF3B2A1A),
                            shape = RoundedCornerShape(16.dp),
                            title = {
                                Text(
                                    text = "YOU LOST THE RACE",
                                    color = Color.White,
                                    maxLines = 1
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDialogEndRaceLose = false
                                        showDialogEndRaceWin = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD7B98E),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Text(text = "Close")
                                }
                            },
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize() // Fill the screen (or adjust as needed)
                        .offset(x = offsetCurse)
                ) {
                    Box(modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = 20.dp,
                            bottom = if (hasTrip == true) 220.dp else 20.dp // Adjust position when hasTrip is true
                        )
                        .background(
                            color = if (curse) Color(0xff324e3b) else Color(0xff321f12),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_altitude),
                                    contentDescription = "Altitude Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = "${"%.0f".format(SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure))} m",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.5.dp),
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_speedometer),
                                    contentDescription = "Speed Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )

                                Spacer(modifier = Modifier.width(5.dp))

                                Text(
                                    text = "${"%.0f".format(smoothedSpeed)} km/h",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                if(currRace?.invite == true && currRace?.finished == false && raceOver == false) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = offsetCurse)
                    ) {
                        Box(modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 20.dp,
                                bottom = if (hasTrip == true) 275.dp else 75.dp // Adjust position when hasTrip is true
                            )
                            .background(
                                color = if (curse) Color(0xff324e3b) else Color(0xff321f12),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 5.dp)
                            .size(42.dp)
                            .clickable {
                                if(currUser?.email == whoInvited) {
                                    Log.d(userInvited, "userInvited")
                                    Log.d(whoInvited, "whoInvited")
                                    navController.navigate("race_screen/$userInvited")
                                }
                                else {
                                    Log.d(userInvited, "userInvited")
                                    Log.d(whoInvited, "whoInvited")
                                    navController.navigate("race_screen/${whoInvited}")
                                }
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.race_logo), // Background image
                                contentDescription = "Broom Image",
                                modifier = Modifier.fillMaxSize() // Fills the Box
                            )
                        }
                    }
                }

                // Overlay content for the selected marker
                selectedMarker?.let { broom ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Background to intercept clicks
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x80000000)) // Dimmed background
                                .clickable(
                                    onClick = {
                                        selectedMarker = null // Dismiss overlay on background click
                                    },
                                    indication = null, // No ripple effect
                                    interactionSource = remember { MutableInteractionSource() } // No interaction state
                                )
                        )

                        // Overlay content
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .background(
                                    color = Color(0xFF321F12), // Brown background
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp) // Rounded corners
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                                .size(160.dp)
                        ) {
                            // Broom details
                            Text(
                                text = broom.name,
                                color = Color.White,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .align(Alignment.TopCenter)
                            )
                            Row(
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .fillMaxWidth(),

                                ) {
                                // Use Box to layer images
                                Box(
                                    modifier = Modifier.size(100.dp) // Size of the Box
                                ) {
                                    // Bottom image
                                    Image(
                                        painter = painterResource(id = R.drawable.background_for_broom), // Background image
                                        contentDescription = "Broom Image",
                                        modifier = Modifier.fillMaxSize() // Fills the Box
                                    )

                                    // Top image
                                    Image(
                                        painter = painterResource(id = R.drawable.nimbus_2000), // Replace with your overlay image resource
                                        contentDescription = "Overlay Image",
                                        modifier = Modifier
                                            .size(90.dp) // Adjust size of the overlay image
                                            .align(Alignment.Center) // Center it on top of the background image
                                    )
                                }

                                Column{

                                    Row(
                                        horizontalArrangement = Arrangement.Start

                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.dolla_dolla),
                                            contentDescription = "Dolla Dolla Logo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(start = 16.dp, end = 4.dp)
                                                .align(Alignment.CenterVertically)
                                        )

                                        Text(
                                            text = broom.price.toString() + " Galleon/Minute",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.Start,
                                        modifier = Modifier.offset(y = (-12).dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.m_logo),
                                            contentDescription = "Mm Logo",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .padding(start = 25.dp, end = 4.dp)
                                                .align(Alignment.CenterVertically)
                                        )

                                        Text(
                                            text = broom.distance.toString() + " m",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }

                            // Accio Broom button
                            Button(
                                onClick = {
                                    val selectedBroom = selectedMarker
                                    selectedMarker = null // Dismiss overlay on button click

                                    if (selectedBroom != null) {
                                        navController.navigate(Screens.BroomDetails.route
                                            .replace(
                                                oldValue = "{broom}",
                                                newValue = selectedBroom.name
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(top = 8.dp)
                                    .background(
                                        color = Color(0xFFDBC7A1),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                                            16.dp
                                        )
                                    ),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDBC7A1)) // Match image color
                            ) {
                                Text(text = "Accio Broom",
                                    color = Color(0xFF321F12),
                                    fontSize = 18.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Function to create a custom BitmapDescriptor with a specific size
fun getScaledMarkerIcon(context: Context, drawableId: Int, width: Int, height: Int): BitmapDescriptor {
    val originalBitmap = BitmapFactory.decodeResource(context.resources, drawableId)
    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}

// Utility function to calculate distance between two locations
private fun calculateDistance(start: LatLng, end: LatLng): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    return results[0].toDouble() // Distance in meters
}

@Composable
fun ShakeDetector(
    shakeThreshold: Float = 300f, // Adjust to prevent false positives
    cooldownTime: Long = 1500, // Prevents multiple rapid triggers
    onShake: (Boolean) -> Unit // Boolean indicates whether a shake was detected
) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var lastUpdate by remember { mutableStateOf(System.currentTimeMillis()) }
    var lastShakeTime by remember { mutableStateOf(0L) }
    var lastX by remember { mutableStateOf(0f) }
    var lastY by remember { mutableStateOf(0f) }
    var lastZ by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val curTime = System.currentTimeMillis()
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate acceleration difference
                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ
                val acceleration = (deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

                if (curTime - lastUpdate > 100) {
                    lastUpdate = curTime

                    // If acceleration exceeds threshold and cooldown time has passed
                    if (acceleration > shakeThreshold && (curTime - lastShakeTime > cooldownTime)) {
                        lastShakeTime = curTime
                        onShake(true) // Trigger shake event
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}

// Utility function to handle different types of distance (Long, Int, Double, etc.)
fun convertToDouble(value: Any?): Double {
    return when (value) {
        is Double -> value
        is Long -> value.toDouble()
        is Int -> value.toDouble()
        else -> 0.0  // Default to 0.0 if the type is unknown or null
    }
}

@Composable
fun ShowGoogleMap(userLocation: LatLng, onMarkerClick: (Broom) -> Unit, broomVm: BroomViewModel, hasTrip: Boolean, curse: Boolean, race: Race?, offsetCurse: Dp) {
    // Define marker locations close to the user's location
    val markerLocations = remember { mutableStateOf<List<Broom>>(emptyList()) }

    LaunchedEffect(Unit) {

        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        val broomRef = db.getReference("Brooms")

        broomRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Collect all users from the snapshot
                val broomsList = mutableListOf<Broom>()
                for (child in snapshot.children) {
                    val broom = Broom(
                        name = child.child("Name").value as String,
                        category = child.child("Category").value as String,
                        distance = convertToDouble(child.child("Distance").value),
                        price = convertToDouble(child.child("Price").value),
                        latitude = convertToDouble(child.child("Latitude").value),
                        longitude = convertToDouble(child.child("Longitude").value),
                        available = child.child("Available").value as Boolean
                    )
                    broomsList.add(broom)
                }

                // Update the state variable with the collected user list
                markerLocations.value = broomsList
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("Magic", "Error fetching magic: ${error.message}")
            }
        })
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 17f)
    }

    LaunchedEffect(userLocation) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(userLocation, 17f)
        )
    }

    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // GoogleMap composable
        GoogleMap(
            cameraPositionState = cameraPositionState,
            uiSettings = remember {
                com.google.maps.android.compose.MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false,
                    compassEnabled = true
                )
            },
            properties = remember {
                com.google.maps.android.compose.MapProperties(isMyLocationEnabled = true)
            },
            modifier = Modifier.matchParentSize()
        ) {
            val customIcon = remember {
                getScaledMarkerIcon(
                    context = context,
                    drawableId = R.drawable.custom_marker,
                    width = 100,
                    height = 100
                )
            }

            // Fetch brooms when userLocation changes
            LaunchedEffect(userLocation) {
                broomVm.getBrooms { broomList ->
                    if (broomList != null) {
                        markerLocations.value = broomList
                    } else {
                        println("No brooms found or an error occurred.")
                    }
                }
            }

            if (race != null && !race.finished) {
                val customIconRace = remember {
                    getScaledMarkerIcon(
                        context = context,
                        drawableId = R.drawable.race_finish,
                        width = 100,
                        height = 100
                    )
                }

                Marker(
                    state = MarkerState(position = LatLng(race.latitude, race.longitude)),
                    icon = customIconRace,
                    title = "Finish Line",
                )
            }

            if (!hasTrip) {
                markerLocations.value.forEach { broom ->
                    if (broom.available) {
                        Marker(
                            state = MarkerState(position = LatLng(broom.latitude, broom.longitude)),
                            icon = customIcon,
                            title = "Custom Marker",
                            onClick = {
                                onMarkerClick(broom)
                                true
                            }
                        )
                    }
                }
            }
        }

        // Location Button with a Box around it
        Box(
            modifier = Modifier
                .fillMaxSize() // Fill the screen (or adjust as needed)
                .padding(16.dp) // Padding around the entire Box
                .offset(x = offsetCurse)
        ) {
            // Box around the button for additional styling
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Align to bottom-right
                    .padding(
                        end = 5.dp,
                        bottom = if (hasTrip) 205.dp else 5.dp
                    )
                    .size(50.dp) // Box size, bigger than the button itself
                    .background(
                        if (curse) Color(0xff324e3b) else Color(0xff321f12),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                // Location Button inside AndroidView
                AndroidView(
                    factory = { context ->
                        val button = ImageButton(context).apply {
                            setImageResource(R.drawable.my_location)
                            setBackgroundResource(0) // Remove background
                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER

                            // Handle click event
                            setOnClickListener {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    try {
                                        val fusedLocationClient =
                                            LocationServices.getFusedLocationProviderClient(context)
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            if (location != null) {
                                                val currentLatLng =
                                                    LatLng(location.latitude, location.longitude)
                                                cameraPositionState.move(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        currentLatLng,
                                                        15f
                                                    )
                                                )
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        e.printStackTrace() // Log or handle the exception appropriately
                                    }
                                } else {
                                    // Permission not granted, show an appropriate message or request permission
                                    Toast.makeText(
                                        context,
                                        "Location permission not granted. Please enable it in settings.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        button
                    },
                    modifier = Modifier
                        .align(Alignment.Center) // Center the button inside the box
                        .zIndex(1f) // Ensure the button stays above the map
                )
            }
        }
    }
}

fun hasUserReachedTarget(userLat: Double, userLng: Double, targetLat: Double, targetLng: Double, radiusMeters: Float): Boolean {
    val userLocation = Location("").apply {
        latitude = userLat
        longitude = userLng
    }

    val targetLocation = Location("").apply {
        latitude = targetLat
        longitude = targetLng
    }

    val distance = userLocation.distanceTo(targetLocation)  // Distance in meters

    return distance <= radiusMeters
}

