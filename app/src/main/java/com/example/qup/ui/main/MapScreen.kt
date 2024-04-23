package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startForegroundService
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.data.QueueEntry
import com.example.qup.helpers.calculateEstimatedQueueTime
import com.example.qup.helpers.loadMapStyle
import com.example.qup.ui.attraction.queueTimeColour
import com.example.qup.ui.camera.RequestLoading
import com.example.qup.ui.navigation.NavigationDestination
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object MapDestination : NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map_title
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToList: () -> Unit,
    navigateToQueues: () -> Unit,
    onBack: () -> Unit,
    navigateToAttraction: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navController: NavController = rememberNavController(),
    mapLatLng: LatLng,
    mapZoom: Float,
    mainUiState: MainUiState,
    queuesUiState: QueuesUiState,
    facilityName: String
) {
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshThreshold = 80.dp,
        onRefresh = { mainViewModel.refreshData() })
    val context = LocalContext.current
    val showExitDialogState = remember { mutableStateOf(false) }
    val showInfoDialogState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    BackHandler {
        showExitDialogState.value = true
    }

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = {
                    showExitDialogState.value = true
                },
                showInfo = true,
                onInfoClick = {showInfoDialogState.value = true}
            )
        },
        bottomBar = {
            QueueBottomAppBar(
                listSelected = false,
                mapSelected = true,
                queuesSelected = false,
                navigateToList = { navigateToList() },
                navigateToQueues = { navigateToQueues() })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .pullRefresh(pullRefreshState)
        ) {
            if (showExitDialogState.value) {
                ShowExitFacilityDialog(
                    showDialog = showExitDialogState,
                    onExit = onBack,
                    mainViewModel = mainViewModel,
                    scope = scope
                )
            }
            if (showInfoDialogState.value) {
                ShowInfoDialog(
                    showInfoDialog = showInfoDialogState,
                    title = stringResource(id = R.string.map_title),
                    description = stringResource(id = R.string.map_info)
                )
            }
            when (mainUiState) {
                is MainUiState.Loading -> RequestLoading()
                is MainUiState.Success -> {
                    when (queuesUiState) {
                        is QueuesUiState.Loading -> RequestLoading()
                        is QueuesUiState.Success -> {
                            MapBody(
                                attractions = mainUiState.attractions,
                                queues = queuesUiState.userQueues,
                                latLng = mapLatLng,
                                zoom = mapZoom,
                                onItemClick = navigateToAttraction,
                                facilityName = facilityName
                            )
                        }

                        is QueuesUiState.Error ->
                            InternetError(mainViewModel = mainViewModel)
                    }
                }

                is MainUiState.Error ->
                    InternetError(mainViewModel = mainViewModel)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState)
        }
    }
}

@Composable
fun MapBody(
    attractions: List<Attraction>,
    queues: List<QueueEntry>,
    latLng: LatLng,
    zoom: Float,
    onItemClick: (Int) -> Unit,
    facilityName: String,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, zoom)
    }

    val coroutineScope = rememberCoroutineScope()


    val context = LocalContext.current
    //MapStyle generated with: https://mapstyle.withgoogle.com/
    val mapStyle: MapStyleOptions? = loadMapStyle(context)


    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp, bottom = 4.dp, top = 12.dp)
        )
        Text(
            text = facilityName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp, bottom = 16.dp)
        )

        GoogleMap(
            modifier = Modifier,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = mapStyle, isMyLocationEnabled = true),
        ) {
            for (attraction in attractions) {
                val linkedQueue = queues.find { it.attractionId == attraction.id }
                //https://www.boltuix.com/2022/11/custom-info-window-on-map-marker-clicks.html
                val attractionLatLng = LatLng(attraction.lat, attraction.lng)

                //Generate AI Usage 8.
                val mapIconBitMap =
                    BitmapFactory.decodeResource(context.resources, R.drawable.map_marker)
                val scaledBitmap = Bitmap.createScaledBitmap(mapIconBitMap, 115, 115, false)
                val mapIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                MarkerInfoWindow(
                    state = MarkerState(attractionLatLng),
                    icon = mapIcon,
                    onInfoWindowClick = {
                        // fixes an issue with the app freezing - https://stackoverflow.com/questions/72561687/google-maps-in-jetpack-compose-freezes
                        coroutineScope.launch { onItemClick(attraction.id) }
                    }
                ) { marker ->
                    Card(
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorResource(id = R.color.light_baby_blue),
                            contentColor = colorResource(id = R.color.black)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //if the user is queued for this attraction:
                            if (linkedQueue != null) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .background(
                                            color = colorResource(id = R.color.emerald_green),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.attraction_in_queue_label),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colorResource(id = R.color.white)
                                    )
                                }
                            }
                            Text(
                                text = attraction.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // if attraction is open, display queue time -> else, display status (Maintenance/Closed)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (attraction.status == "Open") {
                                    val queueTime = if (linkedQueue != null) {
                                        calculateEstimatedQueueTime(
                                            linkedQueue.aheadInQueue,
                                            attraction.avg_capacity,
                                            attraction.length
                                        )
                                    } else {
                                        calculateEstimatedQueueTime(
                                            attraction.in_queue,
                                            attraction.avg_capacity,
                                            attraction.length
                                        )
                                    }
                                    Text(
                                        text = stringResource(id = R.string.attraction_queue_time_short_label),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = "$queueTime " + stringResource(id = R.string.attraction_queue_time_unit),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = queueTimeColour(time = queueTime)
                                    )
                                } else {
                                    Text(
                                        text = stringResource(id = R.string.attraction_status_label),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                    Text(
                                        text = attraction.status,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = statusColor(staus = attraction.status)
                                    )
                                }
                            }

                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(
                                        id = R.color.baby_blue
                                    )
                                )
                            ) {    //button is not clickable, whole window is rendered as image -> https://stackoverflow.com/questions/15924045/how-to-make-the-content-in-the-marker-info-window-clickable-in-android
                                Text(text = stringResource(id = R.string.attraction_detail_button))
                            }

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InternetError(modifier: Modifier = Modifier, mainViewModel: MainViewModel) {
    Column(
        modifier = modifier.fillMaxSize(),
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
                mainViewModel.mainUiState = MainUiState.Loading
                mainViewModel.refreshData()
            },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
        ) {
            Text(text = stringResource(R.string.retry_button))
        }
    }
}

@Composable
fun ShowInfoDialog(
    showInfoDialog: MutableState<Boolean>,
    title: String,
    description: String
){
    Dialog(onDismissRequest = { showInfoDialog.value = false }) {
        Surface(
            shape = MaterialTheme.shapes.medium, elevation = 8.dp,
            color = colorResource(id = R.color.light_baby_blue)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Button(
                        onClick = {
                            showInfoDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue)),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(id = R.string.alert_close))
                    }
                }
            }
        }
    }
}

@Composable
fun ShowExitFacilityDialog(
    showDialog: MutableState<Boolean>,
    onExit: () -> Unit,
    mainViewModel: MainViewModel,
    scope: CoroutineScope
) {
    Dialog(onDismissRequest = { showDialog.value = false }) {
        Surface(
            shape = MaterialTheme.shapes.medium, elevation = 8.dp,
            color = colorResource(id = R.color.light_baby_blue)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    stringResource(id = R.string.exit_facility_alert_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(id = R.string.exit_facility_alert_desc),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = { showDialog.value = false },
                            colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
                        ) {
                            Text(stringResource(id = R.string.alert_cancel))
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            showDialog.value = false
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(R.color.baby_blue))
                    ) {
                        Text(stringResource(id = R.string.exit_facility_button))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Button(
                        onClick = {
                            showDialog.value = false

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
                                        onExit()
                                        attempts = 5        //break the loop
                                    } else {
                                        Log.i("HomeViewModel", "Data not updated in time.")
                                        attempts++
                                        delay(1000L)    //wait 1 sec before trying again
                                    }
                                }
                            }
                            onExit()
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(R.color.closed_red))
                    ) {
                        Text(stringResource(id = R.string.exit_facility_and_reset_id_button))
                    }
                }
            }
        }
    }
}