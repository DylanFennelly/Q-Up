package com.example.qup.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.qup.R
import com.example.qup.helpers.sendNotification
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.camera.RequestLoading
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.navigation.NavigationDestination
import com.example.qup.ui.theme.QueueTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_title

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navigateToMap: () -> Unit,
    navigateToPermissions: () -> Unit,
    navigateToCamera: () -> Unit,

    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionsValid = (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
                    && (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
                    && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            )

    BackHandler(enabled = true) {
        //https://stackoverflow.com/questions/72043735/jetpack-compose-implement-home-button-functionality-on-backpress
        val startMain = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(startMain)
    }

    Scaffold(
        //topBar = { QueueTopAppBar(title = "Home") }
    ) { innerPadding ->         //https://stackoverflow.com/questions/66573601/bottom-nav-bar-overlaps-screen-content-in-jetpack-compose
        Box(modifier = Modifier.padding(innerPadding)) {
            when (homeViewModel.homeUiState) {
                is HomeUiState.Idle -> {
                    HomeBody(
                        mainViewModel,
                        homeViewModel,
                        scope
                    )
                }

                is HomeUiState.Loading -> {
                    RequestLoading()
                }

                is HomeUiState.Success -> {
                    RequestLoading()
                    //check boolean
                    //if valid and has permissions, go to map
                    //      else, go to permission screen
                    //else, wipe DataStore and go to camera or permissions
                    val isValid = (homeViewModel.homeUiState as HomeUiState.Success).isValid

                    if (isValid) {
                        if (permissionsValid) {
                            navigateToMap()
                        }else{
                            navigateToPermissions()
                        }
                    } else {
                        LaunchedEffect(true) {
                            scope.launch {
                                //Reset values to default
                                mainViewModel.saveUserData(
                                    userId = -1,
                                    facilityName = "",
                                    baseUrl = "https://failed.com/",
                                    mapLat = 0.0,
                                    mapLng = 0.0
                                )

                                var attempts = 0
                                while (attempts < 5) {
                                    val facilityName = mainViewModel.facilityName.first()
                                    val userId = mainViewModel.userId.first()
                                    val baseUrl = mainViewModel.baseUrl.first()
                                    val mapLat = mainViewModel.mapLat.first()
                                    val mapLng = mainViewModel.mapLng.first()
                                    //Datastore can be slow to update - ensuring data has been updated and looping until it has

                                    if (userId == -1 &&
                                        facilityName == "" &&
                                        baseUrl == "https://failed.com/" &&
                                        mapLat == 0.0 &&
                                        mapLng == 0.0
                                    ) {
                                        Log.i("HomeViewModel", "Beginning Nav")
                                        if (permissionsValid) {
                                            navigateToCamera()
                                        }else{
                                            navigateToPermissions()
                                        }

                                        attempts = 5        //break the loop
                                    } else {
                                        Log.i("HomeViewModel", "Data not updated in time.")
                                        attempts++
                                        delay(1000L)    //wait 1 sec before trying again
                                    }
                                }
                            }
                        }
                    }

                }

                is HomeUiState.Error -> {
                    //wipe DataStore and go to camera
                    LaunchedEffect(true) {
                        scope.launch {
                            //Reset values to default
                            mainViewModel.saveUserData(
                                userId = -1,
                                facilityName = "",
                                baseUrl = "https://failed.com/",
                                mapLat = 0.0,
                                mapLng = 0.0
                            )

                            var attempts = 0
                            while (attempts < 5) {
                                val facilityName = mainViewModel.facilityName.first()
                                val userId = mainViewModel.userId.first()
                                val baseUrl = mainViewModel.baseUrl.first()
                                val mapLat = mainViewModel.mapLat.first()
                                val mapLng = mainViewModel.mapLng.first()
                                //Datastore can be slow to update - ensuring data has been updated and looping until it has

                                if (userId == -1 &&
                                    facilityName == "" &&
                                    baseUrl == "https://failed.com/" &&
                                    mapLat == 0.0 &&
                                    mapLng == 0.0
                                ) {
                                    Log.i("HomeViewModel", "Beginning Nav")
                                    if (permissionsValid) {
                                        navigateToCamera()
                                    }else{
                                        navigateToPermissions()
                                    }
                                    attempts = 5        //break the loop
                                } else {
                                    Log.i("HomeViewModel", "Data not updated in time.")
                                    attempts++
                                    delay(1000L)    //wait 1 sec before trying again
                                }
                            }
                        }
                    }
                }

                is HomeUiState.InternetError -> {
                    InternetError(scope =scope, mainViewModel =  mainViewModel, homeViewModel = homeViewModel)
                }

            }

        }
    }
}

@Composable
fun HomeBody(
    mainViewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    scope: CoroutineScope
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )
        Text(
            text = stringResource(R.string.app_welcome),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 64.dp)
        )
        Button(
            onClick = {
                scope.launch {
                    val userId = mainViewModel.userId.first()
                    val baseUrl = mainViewModel.baseUrl.first()
                    homeViewModel.checkUserIdValidity(baseUrl, userId)
                }
            },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
        ) {
            Text(text = stringResource(id = R.string.enter_facility_button))
        }
    }
}

@Composable
fun InternetError(modifier: Modifier = Modifier, scope: CoroutineScope, mainViewModel: MainViewModel, homeViewModel: HomeViewModel){
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = ""
        )
        Text(
            text = stringResource(R.string.loading_failed),
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = {
                scope.launch {
                    val userId = mainViewModel.userId.first()
                    val baseUrl = mainViewModel.baseUrl.first()
                    homeViewModel.checkUserIdValidity(baseUrl, userId)
                }
            },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
        ) {
            Text(text = stringResource(R.string.retry_button))
        }
    }
}
