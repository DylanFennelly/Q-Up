package com.example.qup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.qup.ui.theme.QueueTheme
import android.Manifest
import android.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.qup.helpers.sendNotification

class MainActivity : ComponentActivity() {
    //Requesting permissions: https://developer.android.com/training/permissions/requesting

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // permission granted -> continue
        } else {
            // permission denied -> explain consequence of denying permissions
            showPermissionDeniedDialog()
        }
    }

    //Check permissions for notifications
    private fun checkNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {}

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Explain to user reason for permission
                showExplanationDialog() { requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            }
            else -> {
                // directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showExplanationDialog(onContinue: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This app requires notification permissions to send you alerts for your queued attractions. Please allow this permission in the next prompt.")
            .setPositiveButton("Ok") { dialog, which ->
                onContinue()
            }
            .create()
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Notification permission was denied. Queue notifications will not be available. Re-open to app to allow permissions.")
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
            .create()
            .show()
    }

    // Creating notification channel - https://developer.android.com/develop/ui/views/notifications/build-notification#kotlin
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Channel Name"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        setContent {
            QueueTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QueueApp()
                }
            }
        }
    }
}