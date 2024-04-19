package com.example.qup.network

import com.example.qup.data.GetAttractionsApiResponse
import com.example.qup.data.JoinQueueApiResponse
import com.example.qup.data.JoinQueueBody
import com.example.qup.data.QueueEntry
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

//Handles API calls
interface FacilityApiService{
    @GET("test-data")
    suspend fun getAttractions(): GetAttractionsApiResponse

    @POST("join-queue")
    suspend fun joinQueue(@Body body: JoinQueueBody): JoinQueueApiResponse

    //query params: https://stackoverflow.com/questions/36730086/retrofit-2-url-query-parameter
    //query does not have a body -> returns list directly
    @GET("user-queues")
    suspend fun getUserQueues(@Query("userId") userId: Int): List<QueueEntry>
}