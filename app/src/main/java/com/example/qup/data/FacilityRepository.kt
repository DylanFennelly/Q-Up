package com.example.qup.data

import com.example.qup.network.FacilityApi

//middle-man between data and application
interface FacilityRepository {
//    suspend fun getFacilities(): List<Facility>
    suspend fun getAttractions(): testAttraction
}

class NetworkFacilityRepository(): FacilityRepository{
//    override suspend fun getFacilities(): List<Facility> {
//        return FacilityApi.retrofitService.getAttractions()
//    }
    override suspend fun getAttractions(): testAttraction {
        return FacilityApi.retrofitService.getAttractions()
    }
}