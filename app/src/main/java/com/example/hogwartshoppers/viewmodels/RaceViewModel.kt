package com.example.hogwartshoppers.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.hogwartshoppers.model.Invite
import com.example.hogwartshoppers.model.Race
import com.google.firebase.database.FirebaseDatabase

sealed interface RaceUIState {
    data class Success(val races: List<Race>) : RaceUIState
    object Error : RaceUIState
    object Loading : RaceUIState
}

class RaceViewModel: ViewModel() {

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val usersRef = db.reference.child("Users")
    val racesRef = db.reference.child("Races")
    val racesInvitesRef = db.reference.child("Race_Invites")
    val finishCoordsRef = db.reference.child("Finish_Coords")

    var raceUiState: RaceUIState by mutableStateOf(RaceUIState.Loading)
        private set

    init {

    }

    fun getRace(user: String, friend: String, callback: (Race?) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            val race = snapshot.children.find {
                it.child("userRace").value == user && it.child("friendRace").value == friend
            }
            if (race != null) {
                val raceData = Race(
                    userRace = race.child("userRace").value as String,
                    friendRace = race.child("friendRace").value as String,
                    finished = race.child("finished").value as Boolean,
                    latitude = convertToDouble(race.child("latitude").value),
                    longitude = convertToDouble(race.child("longitude").value),
                    time = race.child("time").value as Long,
                    invite = race.child("invite").value as Boolean?,
                    winner = race.child("winner").value as String?
                )
                callback(raceData)
            } else {
                callback(null)
            }
        }
    }

    fun getOngoingRace (user: String, callback: (Race?) -> Unit) {
        // gets the race with the user as one of the participants and invite as true
        racesRef.get().addOnSuccessListener { snapshot ->
            val race = snapshot.children.find {
                (it.child("userRace").value == user || it.child("friendRace").value == user) && it.child("invite").value == true
            }
            if (race != null) {
                val raceData = Race(
                    userRace = race.child("userRace").value as String,
                    friendRace = race.child("friendRace").value as String,
                    finished = race.child("finished").value as Boolean,
                    latitude = convertToDouble(race.child("latitude").value),
                    longitude = convertToDouble(race.child("longitude").value),
                    time = race.child("time").value as Long,
                    invite = race.child("invite").value as Boolean?,
                    winner = race.child("winner").value as String?
                )
                callback(raceData)
            } else {
                callback(null)
            }
        }
    }

    fun getRaces(callback: (List<Race>) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            val racesList = mutableListOf<Race>()
                for (raceSnapshot in snapshot.children) {
                    val raceData = Race(
                        userRace = raceSnapshot.child("userRace").value as String,
                        friendRace = raceSnapshot.child("friendRace").value as String,
                        finished = raceSnapshot.child("finished").value as Boolean,
                        latitude = convertToDouble(raceSnapshot.child("latitude").value),
                        longitude = convertToDouble(raceSnapshot.child("longitude").value),
                        time = raceSnapshot.child("time").value as Long,
                        invite = raceSnapshot.child("invite").value as Boolean?,
                        winner = raceSnapshot.child("winner").value as String?
                    )
                    racesList.add(raceData)
                }
            callback(racesList)
        }
    }


    fun getFinishCoords(name: String, callback: (Pair<Double, Double>?) -> Unit) {
        finishCoordsRef.get().addOnSuccessListener { snapshot ->
            val finishCoords = snapshot.children.find {
                it.child("name").value == name
            }
            if (finishCoords != null) {
                val latitude = convertToDouble(finishCoords.child("latitude").value)
                val longitude = convertToDouble(finishCoords.child("longitude").value)
                callback(Pair(latitude, longitude))
            }
            else {
                callback(null)
            }
        }
    }

    fun getRaceCoords(user: String, friend: String, callback: (Pair<Double, Double>?) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            val race = snapshot.children.find {
                it.child("userRace").value == user && it.child("friendRace").value == friend
            }
            if (race != null) {
                val latitude = convertToDouble(race.child("latitude").value)
                val longitude = convertToDouble(race.child("longitude").value)
                callback(Pair(latitude, longitude))
            }
            else {
                callback(null)
            }
        }
    }

    //function to create a new race
    fun createRace(user: String, friend: String, callback: (Boolean) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            // Create a new user entry
            val race = Race(
                userRace = user,
                friendRace = friend,
                finished = false,
                latitude = 0.0,
                longitude = 0.0,
                time = 0,
                invite = null,
                winner = null
            )
            racesRef.push().setValue(race).addOnCompleteListener {
                callback(it.isSuccessful)
            }
        }
    }

    fun deleteRace(user: String, friend: String, callback: (Boolean) -> Unit) {

        racesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists() && snapshot.hasChildren()) {
                var removed = false

                snapshot.children.forEach { child ->
                    val userRace = child.child("userRace").value as? String
                    val friendsRace = child.child("friendRace").value as? String

                    if (userRace == user && friendsRace == friend) {
                        // Remove the matching child
                        child.ref.removeValue()
                        removed = true
                    }
                }

                callback(removed)
            } else {
                callback(false)
            }
        }.addOnFailureListener { error ->
            Log.d("Magic", "Failed to remove curse: ${error.message}")
        }
    }

    fun updateCoordsRace(user: String, friend: String, latitude: Double, longitude: Double, callback: (Boolean) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            val race = snapshot.children.find {
                it.child("userRace").value == user && it.child("friendRace").value == friend
            }
            if (race != null) {
                race.ref.child("latitude").setValue(latitude)
                race.ref.child("longitude").setValue(longitude)
                Log.d("updateCoordsRace", "Latitude: $latitude, Longitude: $longitude")
                Log.d("race lat and long", "${race.child("latitude").value} ${race.child("longitude").value}")
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun finishRace(user: String, friend: String, winner: String, callback: (Boolean) -> Unit) {
        racesRef.get().addOnSuccessListener { snapshot ->
            val race = snapshot.children.find {
                it.child("userRace").value == user && it.child("friendRace").value == friend
            }
            if (race != null) {
                race.ref.child("finished").setValue(true)
                race.ref.child("winner").setValue(winner)

                callback(true)
            } else {
                callback(false)
            }
        }
    }

    // invite user to race
    fun inviteUser(user: String, friend: String, callback: (Boolean) -> Unit){
        racesInvitesRef.get().addOnSuccessListener { snapshot ->
            val invite = Invite(
                from = user,
                to = friend
            )
            racesInvitesRef.push().setValue(invite).addOnCompleteListener {
                callback(it.isSuccessful)
            }
        }
    }

    fun getInvites(callback: (List<Invite>) -> Unit) {
        racesInvitesRef.get().addOnSuccessListener { snapshot ->
            val invitesList = mutableListOf<Invite>()
            for (inviteSnapshot in snapshot.children) {
                val inviteData = Invite(
                    from = inviteSnapshot.child("from").value as String,
                    to = inviteSnapshot.child("to").value as String
                )
                invitesList.add(inviteData)
            }
            callback(invitesList)
        }
    }

    fun acceptInvite(from: String, to: String, callback: (Boolean) -> Unit) {
        racesInvitesRef.get().addOnSuccessListener { snapshot ->
            val invite = snapshot.children.find {
                it.child("from").value == from && it.child("to").value == to
            }
            if (invite != null) {
                racesRef.get().addOnSuccessListener { snapshot ->
                    val race = snapshot.children.find {
                        it.child("userRace").value == from && it.child("friendRace").value == to
                    }
                    if (race != null) {
                        race.ref.child("invite").setValue(true)
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            } else {
                callback(false)
            }
        }
    }

    fun rejectInvite(from: String, to: String, callback: (Boolean) -> Unit) {
        racesInvitesRef.get().addOnSuccessListener { snapshot ->
            val invite = snapshot.children.find {
                it.child("from").value == from && it.child("to").value == to
            }
            if (invite != null) {
                invite.ref.removeValue().addOnCompleteListener {
                    //change to false the invite of the race
                    racesRef.get().addOnSuccessListener { snapshot ->
                        val race = snapshot.children.find {
                            it.child("userRace").value == from && it.child("friendRace").value == to
                        }
                        if (race != null) {
                            race.ref.child("invite").setValue(false)
                            race.ref.child("finished").setValue(true)
                            callback(true)
                        } else {
                            callback(false)
                        }
                    }
                }
            } else {
                callback(false)
            }
        }
    }

    fun removeRaceInvite(fromEmail: String, toEmail: String, callback: (Boolean) -> Unit) {
        val invitesRef = FirebaseDatabase.getInstance().getReference("Race_Invites")

        invitesRef.orderByChild("from").equalTo(fromEmail).get()
            .addOnSuccessListener { snapshot ->
                for (invite in snapshot.children) {
                    val toValue = invite.child("to").getValue(String::class.java)
                    if (toValue == toEmail) {
                        invite.ref.removeValue()
                        Log.d("Firebase", "Invite removed successfully")
                        callback(true)
                    }
                }
                Log.d("Firebase", "No matching invite found")
            }
            .addOnFailureListener { error ->
                Log.e("Firebase", "Failed to remove invite: ${error.message}")
            }

        callback(false)
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
}