package com.example.qup.helpers

import kotlin.math.roundToInt


//Calculates the estimated queue time for an attraction
fun calculateEstimatedQueueTime(inQueue: Int, avgCapacity: Int, attractionTime: Int): Int {
    //calculate time in seconds
    //ensuring timeSecs is a float, then rounding to at end to properly round
    //Integer division in Kotlin truncates decimals without rounding
    val timeSecs = ((inQueue.toFloat() / avgCapacity) * attractionTime) / 60

    return timeSecs.roundToInt()
}