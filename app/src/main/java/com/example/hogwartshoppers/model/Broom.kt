package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Broom(
    val name: String,
    val category: String,
    val distance: Double,
    val price: Double,
    val latitude: Double,
    val longitude: Double,
    val available: Boolean
)