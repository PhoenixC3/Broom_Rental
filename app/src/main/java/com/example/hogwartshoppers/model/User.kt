package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val email: String,
    val name: String,
    val house: String,
    val distance: Double,
    val records: Int,
    val flying: Boolean
)