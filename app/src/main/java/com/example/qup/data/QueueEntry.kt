package com.example.qup.data

import kotlinx.serialization.Serializable

//User queue entrys from API response
@Serializable
data class QueueEntry (
    val attractionId: Int,      //ID of attraciton queue entry is for
    val callNum: Int,           //The number of times the user has been called to go to an attraction
    val aheadInQueue: Int,      //number of people ahead of user in queue
    val lastUpdated: String     //timestamp of last time call num was updated -> for tracking how long its been since user was last called
)

//Call Nums:
//
//0 = Has not been called yet
//
//1 = Called to start heading towards attraction (5 mins + distance)
//
//2 = 5 mins. left in queue -> create entrance ticket
//
//3 = 10 mins. after 2 (using lastUpdate timestamp) → reminder to go enter attraction
//
//4 = 5mins. after 3 → final reminder to enter attraction
//
//5 = 5 mins after 4 → removed from queue