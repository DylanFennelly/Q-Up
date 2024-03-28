package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.ui.navigation.NavigationDestination
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
    navigateToList: (String) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navController: NavController = rememberNavController(),
    facilityName: String,
    mapLatLng: LatLng,
    mapZoom: Float,
    mainUiState: MainUiState
){
//    LaunchedEffect(facilityName){
//        mainViewModel.getFacilityAttractions()
//    }
    //TODO: Add API refresh button
    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = false, mapSelected = true, navigateToList= {navigateToList(mainViewModel.getFacilityName())})}
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when(mainUiState) {
                is MainUiState.Loading -> MapLoading()
                is MainUiState.Success -> MapBody(
                    attractions = mainUiState.attractions,
                    latLng = mapLatLng,
                    zoom = mapZoom
                )
                is MainUiState.Error -> MapError()
            }
        }
    }
}
@Composable
fun MapBody(
    attractions: List<Attraction>,
    latLng: LatLng,
    zoom: Float
){
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, zoom)
    }

    val context = LocalContext.current

    Column {
        GoogleMap(
            modifier = Modifier,
            cameraPositionState = cameraPositionState
        ) {
            for (attraction in attractions) {
                //https://www.boltuix.com/2022/11/custom-info-window-on-map-marker-clicks.html
                val attractionLatLng = LatLng(attraction.lat, attraction.lng)
                MarkerInfoWindow(
                    state = MarkerState(attractionLatLng),
                    onInfoWindowClick = {
                        Toast.makeText(
                            context,
                            "You have queued for ${attraction.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) { marker ->
                    Card(
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.dark_baby_blue), contentColor = colorResource(id = R.color.white))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = attraction.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = attraction.status,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = statusColor(staus = attraction.status)
                            )
                            Button(
                                onClick = { }, 
                                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.baby_blue))
                            ) {    //button is not clickable, whole window is rendered as image -> https://stackoverflow.com/questions/15924045/how-to-make-the-content-in-the-marker-info-window-clickable-in-android
                                Text(text = stringResource(id = R.string.join_queue_button))
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