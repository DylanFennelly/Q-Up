package com.example.qup.data

//middle-man between data and application
interface FacilityRepository {
    suspend fun getFacilities(): List<Facility>
}

class SetuFacilityRepository(private val facilityData: FacilityData): FacilityRepository{
    override suspend fun getFacilities() = facilityData.getFacilities()
}