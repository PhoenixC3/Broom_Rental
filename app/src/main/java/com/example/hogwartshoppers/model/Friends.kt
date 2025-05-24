package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Friends(
    val userEmail: String,
    val friends: List<String>
)