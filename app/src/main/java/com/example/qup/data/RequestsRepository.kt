package com.example.qup.data

import android.content.Context
import android.util.Log

//Repository to contain all requests being made
class RequestsRepository(private val context: Context, private val facilityRepository: FacilityRepository){
    fun testFunction(){
        Log.d("RefreshService", "function trigger")
    }
}