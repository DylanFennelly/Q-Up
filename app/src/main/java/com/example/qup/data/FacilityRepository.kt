package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(): GetAttractionsApiResponse
    suspend fun joinQueue(body: JoinQueueBody): JoinQueueApiResponse
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(): GetAttractionsApiResponse = facilityApiService.getAttractions()
    override suspend fun joinQueue(body: JoinQueueBody): JoinQueueApiResponse = facilityApiService.joinQueue(body)
}