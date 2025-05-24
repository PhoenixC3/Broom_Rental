package com.example.hogwartshoppers.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class AudioViewModel : ViewModel() {

    private val storage = FirebaseStorage.getInstance()

    suspend fun fetchAudioFile(broomName: String): String? {
        val storageRef = storage.reference.child("audio/")
        var resultUrl: String? = null

        try {
            val listResult = storageRef.listAll().await()
            for (fileRef in listResult.items) {
                val fileName = fileRef.name
                Log.e("Firebase", "Checking file: $fileName")

                if (fileName.equals("$broomName.mp3", ignoreCase = true)) {
                    resultUrl = fileRef.downloadUrl.await().toString()
                    Log.e("Firebase", "Matched URL: $resultUrl")
                    break
                }
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Error fetching file for broomName: $broomName", e)
        }

        if (resultUrl == null) {
            Log.e("Firebase", "No matching file found for broomName: $broomName")
        }

        return resultUrl
    }
}