package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(): ApiResponse
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(): ApiResponse = facilityApiService.getAttractions()
}