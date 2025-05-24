package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Posts(
    val userEmail: String,
    val title: String,
    val text: String
)