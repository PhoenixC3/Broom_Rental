package com.example.hogwartshoppers.model

import kotlinx.serialization.Serializable

@Serializable
data class Race(
    val userRace: String,
    val friendRace: String,
    val finished: Boolean,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val invite: Boolean?,
    val winner: String?
)