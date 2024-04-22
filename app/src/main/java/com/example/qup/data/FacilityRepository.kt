package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(url: String): GetAttractionsApiResponse
    suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>
    suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(url: String): GetAttractionsApiResponse = facilityApiService.getAttractions(url)
    override suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse = facilityApiService.joinQueue(url, body)
    override suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>  = facilityApiService.getUserQueues(url, userId)
    override suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse  = facilityApiService.leaveQueue(url, body)
    override suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse = facilityApiService.updateQueueCallNum(url, body)
}