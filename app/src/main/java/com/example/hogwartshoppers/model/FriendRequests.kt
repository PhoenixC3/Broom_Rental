package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class FriendRequests(
    val email: String,
    val requests: List<String> // email das pessoas que enviaram a solicitação
)