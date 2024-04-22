package com.example.qup

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.qup.app.QueueApplicationContainer
import com.example.qup.data.RequestsRepository

class RefreshService : Service() {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var repository: RequestsRepository
    private val interval = 60000L // 60 seconds

    override fun onCreate(){
        super.onCreate()
        repository = (application as QueueApplicationContainer).requestsRepository
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RefreshService", "Starting service")
        val userId = intent?.getIntExtra("userId", -1) ?: -1
        startForeground(1, createNotification())
        scheduleTask(userId)
        return START_STICKY
    }

    private fun scheduleTask(userId: Int) {
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            // Call your refresh function here
            performRefreshTask(userId)
            // Reschedule the next run of this task
            handler.postDelayed(runnable, interval)
        }
        handler.postDelayed(runnable, interval)
    }

    private fun performRefreshTask(userId: Int) {
        Log.d("RefreshService", "Starting refresh task")
        repository.testFunction()
        // TODO: Perform your data refresh task here
    }

    private fun createNotification(): Notification {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("my_service", "My Background Service")
        } else {
            ""
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Running")
            .setContentText("Doing background work.")
            .setSmallIcon(R.drawable.logo)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
        }
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

}