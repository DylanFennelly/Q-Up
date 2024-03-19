package com.example.qup.network

import com.example.qup.data.testAttraction
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

//Handles API calls
interface FacilityApiService{
    @GET("test-data")
    //suspend fun getAttractions(): List<testAttraction>
    suspend fun getAttractions(): testAttraction
}