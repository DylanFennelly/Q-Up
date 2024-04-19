package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(): GetAttractionsApiResponse
    suspend fun joinQueue(body: JoinQueueBody): JoinQueueApiResponse
    suspend fun getUserQueues(userId: Int): List<QueueEntry>
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(): GetAttractionsApiResponse = facilityApiService.getAttractions()
    override suspend fun joinQueue(body: JoinQueueBody): JoinQueueApiResponse = facilityApiService.joinQueue(body)
    override suspend fun getUserQueues(userId: Int): List<QueueEntry>  = facilityApiService.getUserQueues(userId)
}