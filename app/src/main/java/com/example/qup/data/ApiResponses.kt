package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class GetAttractionsApiResponse (
    val statusCode: Int,
    val body: List<Attraction>
)

@Serializable
data class JoinQueueApiResponse (
    val statusCode: Int,
    val body: String
)

@Serializable
data class JoinQueueBody (
    val attractionId: Int,
    val userId: Int
)