package com.example.qup.helpers

import android.content.Context
import com.example.qup.R
import com.google.android.gms.maps.model.MapStyleOptions
import java.io.InputStream
import kotlin.math.roundToInt


//Calculates the estimated queue time for an attraction
fun calculateEstimatedQueueTime(inQueue: Int, avgCapacity: Int, attractionTime: Int): Int {
    //calculate time in seconds
    //ensuring timeSecs is a float, then rounding to at end to properly round
    //Integer division in Kotlin truncates decimals without rounding
    val timeSecs = ((inQueue.toFloat() / avgCapacity) * attractionTime) / 60

    return timeSecs.roundToInt()
}

//Loads JSON map style for styling maps
//Generative AI Usage 2.
fun loadMapStyle(context: Context): MapStyleOptions? {
    return try {
        //Reading in file
        val rawResourceStream: InputStream = context.resources.openRawResource(R.raw.map_style)
        //Converting InputStream to bytes
        val bytes = rawResourceStream.readBytes()
        //Converting bytes back into string (JSON)
        val jsonStyle = String(bytes)
        //returning MapStyleOptions built from JSON
        MapStyleOptions(jsonStyle)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}