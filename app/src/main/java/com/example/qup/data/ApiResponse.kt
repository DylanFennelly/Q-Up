package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse (
    val statusCode: Int,
    val list: List<testAttraction>
)