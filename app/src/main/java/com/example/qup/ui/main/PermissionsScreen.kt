package com.example.qup.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination

//Screen to explain and ask for permissions
object PermissionsDestination: NavigationDestination {
    override val route = "permissions"
    override val titleRes = R.string.permissions_title
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    navigateToMap: (String) -> Unit,
    onNavigateUp: () -> Unit,
){
    val context = LocalContext.current
    val showDeniedDialogState = remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    //Requesting permissions: https://developer.android.com/training/permissions/requesting
    // Generative AI Usage 6.
    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d("Permissions", "All permissions granted")
            navigateToMap("SETU")
        } else {
            showDeniedDialogState.value = true
        }
    }


//    Generate AI Usage 4.
//    Observes for changes in the settings for permission updates
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            //when app is resumed, check permissions
            if (event == Lifecycle.Event.ON_RESUME) {
                if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED
                    ) {
                    Log.d("Permissions", "Permission check on resume: GRANTED")
                    navigateToMap("SETU")
                } else {
                    Log.d("Permissions", "Permission check on resume: DENIED")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }



    Scaffold(
        topBar = { QueueTopAppBar(title = stringResource(id = R.string.permissions_title), navigateUp = onNavigateUp, canNavigateBack = canNavigateBack)}
    ) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            PermissionsBody(context = context, requestPermissionLauncher = requestMultiplePermissionsLauncher)
        }
        ShowPermissionDeniedDialog(showDeniedDialogState, context)
    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PermissionsBody(
    modifier: Modifier = Modifier,
    context: Context,
    requestPermissionLauncher:  ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
){
    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.permissions_top),
                style = MaterialTheme.typography.titleMedium
                )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(id = R.string.permissions_notification_title),
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = stringResource(id = R.string.permissions_notification_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.permissions_notification_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = stringResource(id = R.string.permissions_Location_title),
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = stringResource(id = R.string.permissions_Location_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.permissions_Location_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.photo_camera_24px),
                contentDescription = stringResource(id = R.string.permissions_camera_title),
                modifier = Modifier.padding(end = 16.dp)
            )
            Column {
                Text(
                    text = stringResource(id = R.string.permissions_camera_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(id = R.string.permissions_camera_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Text(
            text = stringResource(id = R.string.permissions_ask),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                checkAllPermissions(context, requestPermissionLauncher)
                      },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
        ) {
            Text(
                text = stringResource(id = R.string.permissions_button),
            )
        }
    }
}

@Composable
fun ShowPermissionDeniedDialog(showDialog: MutableState<Boolean>, context: Context) {
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(id = R.string.permissions_denied)) },
            text = {
                Text(stringResource(id = R.string.permissions_denied_desc))
            },
            confirmButton = {
                Button(onClick = {
                    showDialog.value = false

                    //Open app settings: https://stackoverflow.com/questions/32822101/how-can-i-programmatically-open-the-permission-screen-for-a-specific-app-on-andr
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }

                    context.startActivity(intent)
                }) {
                    Text(stringResource(id = R.string.open_settings_button))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog.value = false
                }) {
                    Text(stringResource(id = R.string.alert_cancel))
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkAllPermissions(context: Context, requestMultiplePermissionsLauncher:  ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>) {
    val requiredPermissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    val isNotificationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    //Granted either Fine or Coarse location
    val isLocationPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val isCameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED

    //if one of the permissions is missing
    if (!isNotificationPermissionGranted || !isLocationPermissionGranted || !isCameraPermissionGranted ) {
        // only requesting permissions that have not been granted yet
        // filtering granted permissions out of the permissions array
        val permissionsToRequest = requiredPermissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()

        requestMultiplePermissionsLauncher.launch(permissionsToRequest)
    } else {
        Log.d("Permissions", "All permissions are already granted")
        // Navigate or perform next steps directly
    }
}

