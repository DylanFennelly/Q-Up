package com.example.qup.app

import com.example.qup.data.FacilityData
import com.example.qup.data.FacilityRepository
import com.example.qup.data.SetuFacilityRepository

//Container to instantiate data repositories
interface AppContainer {
    val facilityRepository: FacilityRepository
}

class AppDataContainer: AppContainer{
    override val facilityRepository: FacilityRepository by lazy {
        SetuFacilityRepository(FacilityData())
    }
}