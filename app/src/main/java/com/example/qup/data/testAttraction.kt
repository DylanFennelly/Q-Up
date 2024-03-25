package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class testAttraction(
    val name: String,
    val lat: Double,
    val lng: Double
)