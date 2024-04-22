package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(): GetAttractionsApiResponse
    suspend fun joinQueue(body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun getUserQueues(userId: Int): List<QueueEntry>
    suspend fun leaveQueue(body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun updateQueueCallNum(body: UpdateCallNumBody): UpdateCallNumApiResponse
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(): GetAttractionsApiResponse = facilityApiService.getAttractions()
    override suspend fun joinQueue(body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse = facilityApiService.joinQueue(body)
    override suspend fun getUserQueues(userId: Int): List<QueueEntry>  = facilityApiService.getUserQueues(userId)
    override suspend fun leaveQueue(body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse  = facilityApiService.leaveQueue(body)
    override suspend fun updateQueueCallNum(body: UpdateCallNumBody): UpdateCallNumApiResponse = facilityApiService.updateQueueCallNum(body)
}