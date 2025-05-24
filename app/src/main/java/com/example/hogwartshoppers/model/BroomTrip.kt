package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class BroomTrip(
    val broomName: String,
    val user: String,
    val distance: Double,
    val date: String,
    val time: String,
    val price: Double,
    val active: Boolean,
    val size: String,
    val charms: String,
    val pic: String
)