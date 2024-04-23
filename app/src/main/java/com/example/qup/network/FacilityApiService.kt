package com.example.qup.network

import com.example.qup.data.GetAttractionsApiResponse
import com.example.qup.data.JoinLeaveQueueApiResponse
import com.example.qup.data.JoinLeaveQueueBody
import com.example.qup.data.QueueEntry
import com.example.qup.data.UpdateCallNumApiResponse
import com.example.qup.data.UpdateCallNumBody
import com.example.qup.data.UserIdApiResponse
import com.example.qup.data.UserIdValidityApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Url

//Handles API calls
interface FacilityApiService{
    //allowing url to change after init: https://stackoverflow.com/questions/32559333/retrofit-2-dynamic-url
    @GET
    suspend fun getAttractions(@Url url: String): GetAttractionsApiResponse

    @POST
    suspend fun joinQueue(@Url url: String, @Body body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse

    //query params: https://stackoverflow.com/questions/36730086/retrofit-2-url-query-parameter
    //query does not have a body -> returns list directly
    @GET
    suspend fun getUserQueues(@Url url: String, @Query("userId") userId: Int): List<QueueEntry>

    //body and API response have same structure
    @POST
    suspend fun leaveQueue(@Url url: String, @Body body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse

    @PUT
    suspend fun updateQueueCallNum(@Url url: String, @Body body: UpdateCallNumBody): UpdateCallNumApiResponse

    //Query param is included as part of QR code
    @GET
    suspend fun getUserId(@Url url: String): Response<UserIdApiResponse>
    @GET
    suspend fun checkUserIdValidity(@Url url: String, @Query("userId") userId: Int): Response<UserIdValidityApiResponse>
}