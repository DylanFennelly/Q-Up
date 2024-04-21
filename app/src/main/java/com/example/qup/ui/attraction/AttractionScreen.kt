package com.example.qup.ui.attraction

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.data.QueueEntry
import com.example.qup.helpers.calculateEstimatedQueueTime
import com.example.qup.helpers.loadMapStyle
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.main.JoinQueueUiState
import com.example.qup.ui.main.ListError
import com.example.qup.ui.main.ListLoading
import com.example.qup.ui.main.MainUiState
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.main.QueuesUiState
import com.example.qup.ui.main.statusColor
import com.example.qup.ui.navigation.NavigationDestination
import com.example.qup.ui.theme.QueueTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object AttractionDestination : NavigationDestination {
    override val route = "attraction"
    override val titleRes = R.string.attraction_detail_button
    const val attractionID = "id"             //determines which attraction data to load
    val routeWithArgs = "$route/{$attractionID}"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AttractionScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToQueues: () -> Unit,
    mainViewModel: MainViewModel,
    attractionUiState: MainUiState,
    queuesUiState: QueuesUiState,
    attractionViewModel: AttractionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val attractionId = attractionViewModel.attractionId
    val joinQueueUiState = mainViewModel.joinQueueUiState
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshThreshold = 80.dp,
        onRefresh = { mainViewModel.refreshData(0) })  //TODO: hardcoded user ID
    var lastRequest by remember { mutableStateOf("") }       //string to keep track of whether last request made was join or leave -> for alerts
    var leaveConfirmation by rememberSaveable { mutableStateOf(false) }

    when (attractionUiState) {
        //TODO: add Loading and Error states
        is MainUiState.Loading -> {}
        is MainUiState.Error -> {}
        is MainUiState.Success -> {

            val attraction = attractionUiState.attractions[attractionId]

            when (queuesUiState) {
                is QueuesUiState.Loading -> {}
                is QueuesUiState.Error -> {}
                is QueuesUiState.Success -> {

                    val linkedQueue =
                        queuesUiState.userQueues.find { it.attractionId == attraction.id }

                    Scaffold(
                        modifier = Modifier.pullRefresh(pullRefreshState),
                        topBar = {
                            QueueTopAppBar(
                                title = stringResource(id = R.string.attraction_detail_button),
                                canNavigateBack = canNavigateBack,
                                navigateUp = { onNavigateUp() }
                            )
                        },
                        bottomBar = {
                            QueueBottomAppBar(
                                listSelected = true,
                                mapSelected = false,
                                queuesSelected = false,
                                navigateToMap = { navigateToMap() },
                                navigateToQueues = { navigateToQueues() })
                        },

                        //TODO: disable if user already in queue for attraction & replace with Leave Queue button
                        floatingActionButton = {
                            //if processing queue Join & user is not in a queue
                            if (joinQueueUiState == JoinQueueUiState.Loading && linkedQueue == null) {
                                FloatingActionButton(
                                    onClick = {},
                                    containerColor = colorResource(id = R.color.dark_baby_blue),
                                    contentColor = colorResource(id = R.color.disabled_text_grey),
                                ) {

                                    Row() {
                                        //while request in progress, display spinner icon
                                        CircularProgressIndicator(
                                            color = colorResource(id = R.color.disabled_text_grey),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(24.dp)
                                        )

                                        Text(
                                            text = stringResource(id = R.string.join_queue_button),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                        )

                                    }
                                }
                            }
                            //else if user is in queue for attraction (display leave queue instead)
                            else if (joinQueueUiState != JoinQueueUiState.Loading && linkedQueue != null) {
                                FloatingActionButton(
                                    onClick = { leaveConfirmation = true },
                                    containerColor = colorResource(id = R.color.closed_red),
                                    contentColor = colorResource(id = R.color.white),
                                ) {
                                    Row() {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = stringResource(id = R.string.leave_queue_button),
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        Text(
                                            text = stringResource(id = R.string.leave_queue_button),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                        )

                                    }
                                }
                            }
                            //else if leave queue is in progress
                            else if (joinQueueUiState == JoinQueueUiState.Loading && linkedQueue != null) {
                                FloatingActionButton(
                                    onClick = {},
                                    containerColor = colorResource(id = R.color.disabled_red),
                                    contentColor = colorResource(id = R.color.disabled_text_grey),
                                ) {

                                    Row() {
                                        //while request in progress, display spinner icon
                                        CircularProgressIndicator(
                                            color = colorResource(id = R.color.disabled_text_grey),
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(24.dp)
                                        )

                                        Text(
                                            text = stringResource(id = R.string.leave_queue_button),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                        )

                                    }
                                }
                            }

                            //else user is not in queue and join/leave is not in progress
                            else {
                                FloatingActionButton(
                                    onClick = {
                                        mainViewModel.joinQueueUiState = JoinQueueUiState.Loading
                                        mainViewModel.postJoinAttractionQueue(attraction.id, 0)
                                        lastRequest = "Join"
                                    }, //TODO: hardcoded user ID
                                    containerColor = colorResource(id = R.color.baby_blue),
                                    contentColor = colorResource(id = R.color.white),
                                ) {

                                    Row() {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = stringResource(id = R.string.join_queue_button),
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        Text(
                                            text = stringResource(id = R.string.join_queue_button),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                                        )

                                    }
                                }
                            }

                        }

                    ) { innerPadding ->
                        Box(
                            //modifier = Modifier.pullRefresh(pullRefreshState)
                        ) {
                            AttractionDetails(
                                attraction = attraction,
                                linkedQueue = linkedQueue,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            PullRefreshIndicator(
                                refreshing = isRefreshing,
                                state = pullRefreshState
                            )
                        }
                    }

                    //alert for confirming queue leave
                    if(leaveConfirmation){
                        AlertDialog(
                            onDismissRequest = { leaveConfirmation = false },
                            title = { Text(text = stringResource(id = R.string.leave_queue_confirmation_alert_title)) },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.leave_queue_confirmation_alert_desc_1) + " ${attraction.name}? \n\n" + stringResource(id = R.string.leave_queue_confirmation_alert_desc_2),
                                    textAlign = TextAlign.Center)
                                   },
                            dismissButton = {
                                TextButton(onClick = { leaveConfirmation = false }) {
                                    Text(text = stringResource(R.string.alert_cancel))
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    leaveConfirmation = false
                                    mainViewModel.joinQueueUiState = JoinQueueUiState.Loading
                                    mainViewModel.postLeaveAttractionQueue(attraction.id, 0) //TODO: hardcoded user ID
                                    lastRequest = "Leave"
                                }
                                ) {
                                    Text(stringResource(id = R.string.alert_confirm))
                                }
                            },
                        )
                    }

                    //alert for joining queue
                    when (joinQueueUiState) {
                        is JoinQueueUiState.Result -> {
                            AlertDialog(
                                onDismissRequest = {
                                    mainViewModel.refreshData(0)        //TODO: hardcoded user ID
                                    mainViewModel.joinQueueUiState = JoinQueueUiState.Idle
                                },
                                title = {
                                    if (joinQueueUiState.statusCode == 200) {
                                        Log.d("AttractionScreen", "lastRequest: $lastRequest")
                                        if (lastRequest == "Join") {
                                            Text(text = stringResource(id = R.string.join_queue_success_alert_title))
                                        } else {
                                            Text(text = stringResource(id = R.string.leave_queue_success_alert_title))
                                        }
                                    } else {
                                        Text(text = stringResource(id = R.string.error_alert_title))
                                    }
                                },
                                text = {
                                    when (joinQueueUiState.statusCode) {
                                        //success
                                        200 -> {
                                            if (lastRequest == "Join") {
                                                Text(
                                                    text = stringResource(id = R.string.join_queue_success_alert_desc) + " ${attraction.name}.",
                                                    textAlign = TextAlign.Center
                                                )
                                            } else {
                                                Text(
                                                    text = stringResource(id = R.string.leave_queue_success_alert_desc) + " ${attraction.name}.",
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        //already in queue - shouldnt happen, button to join queue isnt pressable if in queue
                                        409 -> {
                                            Text(
                                                text = stringResource(id = R.string.join_queue_error_409_alert_desc),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        else -> {
                                            Text(
                                                text = stringResource(id = R.string.error_500_alert_desc),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        mainViewModel.refreshData(0)
                                        mainViewModel.joinQueueUiState =
                                            JoinQueueUiState.Idle
                                    }
                                    ) {
                                        Text(stringResource(id = R.string.alert_okay))
                                    }
                                },
                                )
                        }

                        else -> {}
                    }

                }

            }

        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AttractionDetails(
    attraction: Attraction,
    linkedQueue: QueueEntry?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                colorResource(id = R.color.light_baby_blue)
            ),
    ) {
        Column {

            //if the user is queued for this attraction:
            if (linkedQueue != null) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = colorResource(id = R.color.emerald_green))
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                //Name
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.attraction_name_label),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = attraction.name,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                //TODO: add status message for when attraction is closed or maintenance
                //Queue Time
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
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (linkedQueue != null) {
                                Text(
                                    text = stringResource(id = R.string.attraction_queue_time_remaining_label),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            } else {
                                Text(
                                    text = stringResource(id = R.string.attraction_queue_time_label),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                text = "$queueTime " + stringResource(id = R.string.attraction_queue_time_unit),
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = queueTimeColour(time = queueTime)
                            )
                        }
                    }
                }

                //Type
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.attraction_type_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = attraction.type,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                //Status
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.attraction_status_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = attraction.status,
                        style = MaterialTheme.typography.titleLarge,
                        color = statusColor(staus = attraction.status)
                    )
                }

                //Attraction length
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.attraction_length_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${attraction.length} ${stringResource(id = R.string.attraction_length_unit)}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                //Attraction length
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.attraction_max_cap_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${attraction.max_capacity}",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                //Cost
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.attraction_cost_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = attractionCostText(attraction.cost),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                val cameraPositionState = rememberCameraPositionState {
                    position =
                        CameraPosition.fromLatLngZoom(LatLng(attraction.lat, attraction.lng), 17f)
                }
                val context = LocalContext.current
                val mapStyle: MapStyleOptions? = loadMapStyle(context)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.attraction_location_label),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GoogleMap(
                        cameraPositionState = cameraPositionState,
                        modifier = Modifier.height(225.dp),
                        properties = MapProperties(mapStyleOptions = mapStyle)
                    ) {
                        Marker(state = MarkerState(LatLng(attraction.lat, attraction.lng))) {

                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 64.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.attraction_description_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = attraction.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                //https://medium.com/make-apps-simple/text-scrolling-in-jetpack-compose-deabc7f67156

            }
        }
    }
}

fun attractionCostText(cost: Float): String {
    return when (cost) {
        0f -> "Free"
        else -> "€ " + String.format("%.2f", cost)
    }
}

@Composable
fun queueTimeColour(time: Int): Color {
    return when {
        time <= 20 -> colorResource(id = R.color.emerald_green)
        (time in 21..44) -> colorResource(id = R.color.maintenance_yellow)
        else -> colorResource(id = R.color.closed_red)      //else must be greater than 45
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
fun AttractionDetailsPreview() {
    val queue = QueueEntry(0,0, 150)
    QueueTheme {
        AttractionDetails(
            attraction =
            Attraction(
                id = 1,
                name = "Walton Building",
                description = "The Walton Building is located on the Institute’s main Cork Road campus close to the award-winning Institute library, Luke Wadding Library.  Named after Ernest TS Walton (the Co Waterford-born Nobel Physics Laureate) the 3,000 square metre Walton Building greatly enhances and expands the Institute’s world-class information and communications infrastructure. \n" +
                        "The 18 large computer laboratories in the building each feature an innovative passive air movement system that helps ensure comfortable learning conditions for users. A daylight-filled central atrium located alongside the entrance accommodates all circulation and social spaces.",
                type = "School",
                status = "Maintenance",
                cost = 2.50f,
                length = 180,
                lat = 52.2457368280431,
                lng = -7.137318108777412,
                avg_capacity = 15,
                max_capacity = 25,
                in_queue = 150
            ),
            linkedQueue = queue,
        )
    }
}