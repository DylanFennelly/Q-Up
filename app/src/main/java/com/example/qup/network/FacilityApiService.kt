package com.example.qup.network

import com.example.qup.data.ApiResponse
import retrofit2.http.GET

//Handles API calls
interface FacilityApiService{
    @GET("test-data")
    suspend fun getAttractions(): ApiResponse
}