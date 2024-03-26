package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    val id: Int,
    val name: String,
    val description: String,
    val type: String,
    val status: String,
    val cost: Float,
    val length: Float,
    val lat: Double,
    val lng: Double
)