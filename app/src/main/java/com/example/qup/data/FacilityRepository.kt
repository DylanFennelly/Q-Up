package com.example.qup.data

import com.example.qup.network.FacilityApiService
import retrofit2.Response

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(url: String): GetAttractionsApiResponse
    suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>
    suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse
    suspend fun getUserId(url: String): UserIdApiResponse
}

//custom exception to pass error code messages to the CameraScreen
class ApiException(val code: Int, message: String): Exception(message)

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(url: String): GetAttractionsApiResponse = facilityApiService.getAttractions(url)
    override suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse = facilityApiService.joinQueue(url, body)
    override suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>  = facilityApiService.getUserQueues(url, userId)
    override suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse  = facilityApiService.leaveQueue(url, body)
    override suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse = facilityApiService.updateQueueCallNum(url, body)
    override suspend fun getUserId(url: String): UserIdApiResponse {
        val response = facilityApiService.getUserId(url)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No data received")
        } else {
            throw ApiException(response.code(), "Error checking ticket: ${response.message()}")
        }
    }
}