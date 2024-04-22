package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class GetAttractionsApiResponse (
    val statusCode: Int,
    val body: List<Attraction>
)

@Serializable
data class JoinLeaveQueueApiResponse (
    val statusCode: Int,
    val body: String
)

@Serializable
data class JoinLeaveQueueBody (
    val attractionId: Int,
    val userId: Int
)

@Serializable
data class UpdateCallNumBody (
    val attractionId: Int,
    val userId: Int,
    val callNum: Int
)

@Serializable
data class UpdateCallNumApiResponse (
    val statusCode: Int,
    val body: String
)

@Serializable
data class UserIdResponseBody (
    val message: String,
    val userId: Int,
    val facilityName: String,
    val baseUrl: String,
    val mapLat: Double,
    val mapLng: Double
)

@Serializable
data class UserIdApiResponse (
    val statusCode: Int,
    val body: UserIdResponseBody
)