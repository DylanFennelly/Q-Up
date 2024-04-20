package com.example.qup.data

import kotlinx.serialization.Serializable

//User queue entrys from API response
@Serializable
data class QueueEntry (
    val attractionId: Int,      //ID of attraciton queue entry is for
    val callNum: Int,           //The number of times the user has been called to go to an attraction
    val aheadInQueue: Int       //number of people ahead of user in queue
)