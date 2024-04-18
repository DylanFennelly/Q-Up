package com.example.qup.app

import com.example.qup.data.FacilityRepository
import com.example.qup.data.NetworkFacilityRepository
import com.example.qup.network.FacilityApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

//Container to instantiate data repositories
interface AppContainer {
    val facilityRepository: FacilityRepository
}

class AppDataContainer: AppContainer{
    private val baseTestUrl = "https://owhvjc8fwj.execute-api.eu-west-1.amazonaws.com/q-up-test-api-stage/"     //todo: not hardcoded

    //prints API requests to logs with "OkHttp" tag - https://stackoverflow.com/questions/32514410/logging-with-retrofit-2
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseTestUrl)
        .client(httpClient)
        .build()

    private val retrofitService: FacilityApiService by lazy {
        retrofit.create(FacilityApiService::class.java)
    }

    override val facilityRepository: FacilityRepository by lazy {
        NetworkFacilityRepository(retrofitService)
    }
}