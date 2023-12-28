package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Facility
import com.example.qup.ui.AppViewModelProvider
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
    const val facility = "facility"             //determines which attraction data to load
    val routeWithArgs = "$route/{$facility}"

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController = rememberNavController(),
    facilityName: String,
    mapLatLng: LatLng,
    mapZoom: Float
){
    var mapLocation: LatLng = LatLng(0.0,0.0)

    //https://medium.com/@sujathamudadla1213/what-is-launchedeffect-coroutine-api-android-jetpack-compose-76d568b79e63
    LaunchedEffect(facilityName) {
        mapViewModel.retrieveFacility(facilityName)

    }

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MapBody(
                facility = mapViewModel.facility.value,
                latLng =  mapLatLng,
                zoom = mapZoom
            )
        }
    }
}
@Composable
fun MapBody(
    facility: Facility,
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
            for (attraction in facility.Attractions) {
                //https://www.boltuix.com/2022/11/custom-info-window-on-map-marker-clicks.html
                MarkerInfoWindow(
                    state = MarkerState(attraction.latlng),
                    onInfoWindowClick = {
                        Toast.makeText(
                            context,
                            "You have queued for ${attraction.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) { marker ->
                    Box(
                        modifier = Modifier.background(
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = attraction.name,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(onClick = { }) {    //button is not clickable, whole window is rendered as image -> https://stackoverflow.com/questions/15924045/how-to-make-the-content-in-the-marker-info-window-clickable-in-android
                                Text(text = stringResource(id = R.string.join_queue_button))
                            }

                        }
                    }
                }
            }
        }
    }
}