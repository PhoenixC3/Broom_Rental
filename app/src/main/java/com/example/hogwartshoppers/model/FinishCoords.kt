package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class FinishCoords(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)