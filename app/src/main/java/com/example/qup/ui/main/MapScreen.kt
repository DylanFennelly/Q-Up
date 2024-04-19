package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.example.qup.ui.navigation.NavigationDestination
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

object MapDestination: NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map_title
}



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    //navigateToMap: (String) -> Unit,
    navigateToList: () -> Unit,
    navigateToQueues: () -> Unit,
    navigateToAttraction: (Int) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navController: NavController = rememberNavController(),
    facilityName: String,
    mapLatLng: LatLng,
    mapZoom: Float,
    mainUiState: MainUiState,
    queuesUiState: QueuesUiState
){
    //TODO: Add API refresh button
    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = false, mapSelected = true, queuesSelected = false, navigateToList= {navigateToList()}, navigateToQueues = {navigateToQueues()})}
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when(mainUiState) {
                is MainUiState.Loading -> MapLoading()
                is MainUiState.Success -> {
                    when (queuesUiState) {
                        is QueuesUiState.Loading -> MapError()
                        is QueuesUiState.Success -> {
                            MapBody(
                                attractions = mainUiState.attractions,
                                queues = queuesUiState.userQueues,
                                latLng = mapLatLng,
                                zoom = mapZoom,
                                onItemClick = navigateToAttraction
                            )
                        }
                        is QueuesUiState.Error -> MapError()
                    }
                }
                is MainUiState.Error -> MapError()
            }
        }
    }
}
@Composable
fun MapBody(
    attractions: List<Attraction>,
    queues: List<QueueEntry>,
    latLng: LatLng,
    zoom: Float,
    onItemClick: (Int) -> Unit
){
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, zoom)
    }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    //MapStyle generated with: https://mapstyle.withgoogle.com/
    val mapStyle: MapStyleOptions? = loadMapStyle(context)

    Column {
        GoogleMap(
            modifier = Modifier,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = mapStyle)
        ) {
            for (attraction in attractions) {
                val linkedQueue = queues.getOrNull(attraction.id)
                //https://www.boltuix.com/2022/11/custom-info-window-on-map-marker-clicks.html
                val attractionLatLng = LatLng(attraction.lat, attraction.lng)
                MarkerInfoWindow(
                    state = MarkerState(attractionLatLng),
                    onInfoWindowClick = {
                        // fixes an issue with the app freezing - https://stackoverflow.com/questions/72561687/google-maps-in-jetpack-compose-freezes
                        coroutineScope.launch { onItemClick(attraction.id) }
                    }
                ) { marker ->
                    Card(
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.light_baby_blue), contentColor = colorResource(id = R.color.black))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //if the user is queued for this attraction:
                            if (linkedQueue != null){
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
                                if (attraction.status == "Open"){
                                    val queueTime = if (linkedQueue != null){
                                        calculateEstimatedQueueTime(linkedQueue.aheadInQueue, attraction.avg_capacity, attraction.length)
                                    }else{
                                        calculateEstimatedQueueTime(attraction.in_queue, attraction.avg_capacity, attraction.length)
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
                                }else{
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
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.baby_blue))
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
fun MapLoading(modifier: Modifier = Modifier){
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

@Composable
fun MapError(modifier: Modifier = Modifier){
    Column(
        modifier = modifier,
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
    }
}