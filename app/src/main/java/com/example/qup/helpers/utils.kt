package com.example.qup.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.CaseMap.Title
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.qup.MainActivity
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

fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
    //bring app to foreground when notification pressed - https://developer.android.com/develop/ui/views/notifications/build-notification#kotlin
    //return app with current state to foreground - Generative AI Usage 5.
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


    val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)                //removes notification when pressed

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())     //permission to send notifications always granted by this point
    }
}