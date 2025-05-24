package com.example.hogwartshoppers.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hogwartshoppers.viewmodels.BroomViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(navController: NavController) {
    val broomViewModel: BroomViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()
    val authUser = auth.currentUser

    val context = LocalContext.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var cameraExecutor by remember { mutableStateOf<ExecutorService?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionsGranted = isGranted
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted && cameraExecutor == null) {
            cameraExecutor = Executors.newSingleThreadExecutor()
            previewView = PreviewView(context).apply {
                startCamera(this, context) { capture ->
                    imageCapture = capture
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Take a picture of the broom safely parked:", color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        previewView?.let { view ->
            AndroidView(factory = { view }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { imageCapture?.let { takePhoto(navController,broomViewModel,authUser?.email.toString(), it, context) } }) {
            Text("Capture")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor?.shutdown()
        }
    }
}

private fun startCamera(previewView: PreviewView, context: Context, onImageCaptureReady: (ImageCapture) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder().build()
        onImageCaptureReady(imageCapture)

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(context as ComponentActivity, cameraSelector, preview, imageCapture)
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun takePhoto(navController: NavController,broomViewModel: BroomViewModel, userMail: String, imageCapture: ImageCapture, context: Context) {
    val photoFile = File(context.externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions, ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                uploadToFirebase(photoFile,userMail,broomViewModel)

                val savedUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                    .appendPath(photoFile.name).build()
                Log.d("CameraX", "Photo saved successfully: $savedUri")

                navController.navigate(Screens.HomeScreen.route)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

private fun uploadToFirebase(photoFile: File,userMail: String, broomViewModel: BroomViewModel) {
    
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    
    val name = System.currentTimeMillis() // nome da foto para guardar
    
    val imageRef = storageRef.child("broom/${name}.jpg")
    broomViewModel.updateTripPic(userMail,name.toString())

    val uploadTask = imageRef.putFile(Uri.fromFile(photoFile))

    uploadTask.addOnSuccessListener {
        Log.d("Firebase", "Image uploaded successfully!")
    }.addOnFailureListener { exception ->
        Log.e("Firebase", "Image upload failed: ${exception.message}")
    }
}


