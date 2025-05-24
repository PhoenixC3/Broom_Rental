package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Replies(
    val title: String, // Title of the Post
    val userEmail: String,
    val text: String
)