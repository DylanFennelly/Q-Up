package com.example.qup.data

//middle-man between data and application
interface AttractionRepository {
    suspend fun getAttractions(): List<Attraction>
}

class SetuAttractionRepository(private val attractionData: AttractionData): AttractionRepository{
    override suspend fun getAttractions() = attractionData.getSetuAttractions()
}