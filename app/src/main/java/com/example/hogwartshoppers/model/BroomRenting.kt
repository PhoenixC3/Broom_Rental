package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class BroomRenting(
    val name: String, // Name of the broom
    val color: String, // Color of the broom
    val accessories: List<String>, // List of accessories
    val charms: List<String>, // List of charms
    val size: String, // Size of the broom
    val perks: List<String>, // List of perks
)