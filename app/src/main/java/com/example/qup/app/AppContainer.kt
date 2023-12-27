package com.example.qup.app

import com.example.qup.data.AttractionData
import com.example.qup.data.AttractionRepository
import com.example.qup.data.SetuAttractionRepository

//Container to instantiate data repositories
interface AppContainer {
    val attractionRepository: AttractionRepository
}

class AppDataContainer: AppContainer{
    override val attractionRepository: AttractionRepository by lazy {
        SetuAttractionRepository(AttractionData())
    }
}