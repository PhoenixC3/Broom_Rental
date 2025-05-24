package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Invite(
    val from: String,
    val to: String
)