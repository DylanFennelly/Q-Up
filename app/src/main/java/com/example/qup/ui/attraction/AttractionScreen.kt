package com.example.qup.ui.attraction

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.main.MainUiState
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.main.statusColor
import com.example.qup.ui.navigation.NavigationDestination
import com.example.qup.ui.theme.QueueTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object AttractionDestination: NavigationDestination {
    override val route = "attraction"
    override val titleRes = R.string.attraction_detail_button
    const val attractionID = "id"             //determines which attraction data to load
    val routeWithArgs = "$route/{$attractionID}"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AttractionScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: (String) -> Unit,
    navigateToMap: (String) -> Unit,
    mainViewModel: MainViewModel,
    attractionUiState: MainUiState,
    attractionViewModel: AttractionViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val attractionId = attractionViewModel.attractionId

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(id = R.string.attraction_detail_button),
                canNavigateBack = canNavigateBack,
                navigateUp = {onNavigateUp(mainViewModel.getFacilityName())}
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = true, mapSelected = false, navigateToMap= { navigateToMap(mainViewModel.getFacilityName()) }) },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
                containerColor = colorResource(id = R.color.baby_blue),
                contentColor = colorResource(id = R.color.white)
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
    ) {innerPadding ->
        when(attractionUiState){
            is MainUiState.Loading -> {
            }
            is MainUiState.Success -> {
                Log.i("ViewModel", attractionUiState.attractions.toString())
                AttractionDetails(
                    attraction = attractionUiState.attractions[attractionId],
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                )
            }
            is MainUiState.Error -> {}
        }
    }
}

@Composable
fun AttractionDetails(
    attraction: Attraction,
    modifier: Modifier = Modifier,
){
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.light_baby_blue), contentColor = colorResource(id = R.color.black))
    ){
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
                ){
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

            //Queue Time
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ){
                    Text(
                        text = stringResource(id = R.string.attraction_queue_time_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${attraction.in_queue}",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = queueTimeColour(time = 15)      //TODO: Hardcoded, take actual estiamed queue time
                    )
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
                position = CameraPosition.fromLatLngZoom(LatLng(attraction.lat, attraction.lng), 17f)
            }

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
                    modifier = Modifier.height(225.dp)
                ) {
                    Marker(state = MarkerState(LatLng(attraction.lat, attraction.lng))) {

                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)
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

fun attractionCostText(cost: Float) : String{
    return when (cost){
        0f -> "Free"
        else -> "€ " + String.format("%.2f", cost)
    }
}

@Composable
fun queueTimeColour(time: Int) : Color{
    return when {
        time <= 20 -> colorResource(id = R.color.emerald_green)
        (time in 21..44) -> colorResource(id = R.color.maintenance_yellow)
        else -> colorResource(id = R.color.closed_red)      //else must be greater than 45
    }
}

@Preview(showBackground = true)
@Composable
fun AttractionDetailsPreview(){
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
            )
        )
    }
}