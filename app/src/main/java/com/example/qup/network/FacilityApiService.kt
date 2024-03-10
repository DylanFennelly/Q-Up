package com.example.qup.network

import com.example.qup.data.testAttraction
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

//Handles API calls

private const val BASE_TEST_URL = "https://owhvjc8fwj.execute-api.eu-west-1.amazonaws.com/q-up-test-api-stage/"     //todo: not hardcoded

private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_TEST_URL)
    .build()

object FacilityApi {
    val retrofitService: FacilityApiService by lazy {
        retrofit.create(FacilityApiService::class.java)
    }
}

interface FacilityApiService{
    @GET("test-data")
    //suspend fun getAttractions(): List<testAttraction>
    suspend fun getAttractions(): testAttraction
}