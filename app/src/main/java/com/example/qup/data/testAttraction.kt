package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class testAttraction(
    val statusCode: Int,
    val body: String
)