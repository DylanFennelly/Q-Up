package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
//    suspend fun getFacilities(): List<Facility>
    suspend fun getAttractions(): testAttraction
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
//    override suspend fun getFacilities(): List<Facility> {
//        return FacilityApi.retrofitService.getAttractions()
//    }
    override suspend fun getAttractions(): testAttraction = facilityApiService.getAttractions()
}