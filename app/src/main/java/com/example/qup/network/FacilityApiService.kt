package com.example.qup.network

import com.example.qup.data.GetAttractionsApiResponse
import com.example.qup.data.JoinQueueApiResponse
import com.example.qup.data.JoinQueueBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

//Handles API calls
interface FacilityApiService{
    @GET("test-data")
    suspend fun getAttractions(): GetAttractionsApiResponse

    @POST("join-queue")
    suspend fun joinQueue(@Body body: JoinQueueBody): JoinQueueApiResponse
}