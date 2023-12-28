# Generative AI Usage Declaration

Generative AI was used to assist with the development of this project. The following is a list of times AI was used, the overview of what the usage achieved, and the exact conversation logs.

_____

## Generative AI Usage 1.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to help with an issue where the route arguments were not being passed to a function being run upon view load.

#### Chat log:
```
User
I am developing an Android app in Kotlin using Jetpack Compose with an Google Maps widget. I have a dataclass, Facility, which contains a list of coordinates to place markers on the map. The MapViewModel interacts with the facilityRepository with an input string and retrieves the specified facility (There are multiple facilities in the list, I wish to only retrieve the data for one. How can I load the data in such a way so that the markers are loaded onto the map upon navigation to the map screen without the need for the user to press a specific button to invoke a method?

MapScreen:
"package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.home.HomeViewModel
import com.example.qup.ui.navigation.NavigationDestination
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
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
    mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val context = LocalContext.current

    Scaffold(
        topBar = { QueueTopAppBar(
            title = stringResource(R.string.map_title),
            canNavigateBack = canNavigateBack,
            navigateUp = onNavigateUp
        )}
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MapBody()
        }

    }
}
@Composable
fun MapBody(){
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0,0.0), 0f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {

    }
}"

MapViewModel:
"package com.example.qup.ui.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.Facility
import com.example.qup.data.FacilityRepository
import kotlinx.coroutines.launch

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel() {
    //state of facility obtained from request
    val facility = mutableStateOf<Facility>(Facility("", emptyList()))
    //private val facilityName: String = checkNotNull(savedStateHandle[MapDestination.facility])

    fun retrieveFacility(facilityName: String){
        viewModelScope.launch {
            val retrievedFacility = facilityRepository.getFacilities()

            when(facilityName){         //Hard coded values for purpose of demonstration; real app will handle data from API request
                "SETU" -> facility.value = retrievedFacility[0]
            }
        }
    }

}"

AppNavGraph, where the navigation to the MapScreen is done from HomeScreen using a routeWithArgs (the facility name):

"@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
        ){
            composable(route = HomeDestination.route){
                HomeScreen(
                    navigateToMap = {navController.navigate("${MapDestination.route}/${it}")}
                )
            }
        composable(
            route = MapDestination.routeWithArgs,
            arguments = listOf(navArgument(MapDestination.facility){
                type = NavType.StringType
            })
        ){
            MapScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}"

FacilityRepository:
"package com.example.qup.data

//middle-man between data and application
interface FacilityRepository {
    suspend fun getFacilities(): List<Facility>
}

class SetuFacilityRepository(private val facilityData: FacilityData): FacilityRepository{
    override suspend fun getFacilities() = facilityData.getFacilities()
}"

FacilityData:
"package com.example.qup.data

import com.google.android.gms.maps.model.LatLng

//temporary attraction data testing purposes
class FacilityData {
    //list of attractions for setu
    private val setuAttractionList = listOf(
        Attraction("Attraction 1", LatLng(0.0, 0.0)),
        Attraction("Attraction 2", LatLng(1.0, 0.0)),
        Attraction("Attraction 3", LatLng(2.0, 0.0)),
    )

    private val facilitiesList = listOf(
        Facility("SETU", setuAttractionList)
    )

    fun getFacilities(): List<Facility> = facilitiesList.toMutableList()

}"
ChatGPT

To achieve the automatic loading of markers on the map upon navigation to the MapScreen without the need for the user to press a specific button, you can call the retrieveFacility method in the MapViewModel when the MapScreen is created. You can do this by observing the facility name argument in the MapScreen and triggering the data retrieval.

Here's an updated version of your MapScreen and MapViewModel:
MapScreen:

kotlin

@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
    facilityName: String? = null // Facility name passed as argument
) {
    val context = LocalContext.current

    // Observe the facility name and trigger data retrieval
    LaunchedEffect(facilityName) {
        if (!facilityName.isNullOrBlank()) {
            mapViewModel.retrieveFacility(facilityName)
        }
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
            MapBody()
        }
    }
}

MapViewModel:

kotlin

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
) : ViewModel() {
    val facility = mutableStateOf<Facility>(Facility("", emptyList()))

    // Retrieve facility data based on the facility name
    fun retrieveFacility(facilityName: String) {
        viewModelScope.launch {
            val retrievedFacility = facilityRepository.getFacilities()

            val selectedFacility = retrievedFacility.find { it.name == facilityName }
            selectedFacility?.let {
                facility.value = it
            }
        }
    }
}

Now, when you navigate to the MapScreen using the routeWithArgs and provide the facility name as an argument, the LaunchedEffect in the MapScreen will automatically trigger the retrieveFacility method in the MapViewModel, and the markers will be loaded onto the map. Ensure that you pass the facility name when navigating to the MapScreen from the HomeScreen.
User
The code you provided does not account for the facility name placed into the arguments. When the button on the HomeScreen is pressed, it triggers the function "navController.navigate("${MapDestination.route}/${it}", where the function call is given the arguement "SETU" (the facility I wish to receive). Currently, the code always returns the default facility name placed in MapDestination ("facility"). How can it be updated to accommodate for input values such as the one described?

" MapScreen(
                onNavigateUp = { navController.navigateUp() },
                facilityName = MapDestination.facility
            )"
ChatGPT

My apologies for the oversight. To address this, you need to pass the actual facility name as an argument when navigating to the MapScreen. Update the call to navController.navigate in your HomeScreen as follows:

kotlin

@Composable
fun HomeScreen(navigateToMap: (String) -> Unit) {
    // ...
    Button(
        onClick = {
            navigateToMap("SETU") // Pass the desired facility name here
        }
    ) {
        Text("Navigate to Map")
    }
    // ...
}

Now, when the button is pressed, it will navigate to the MapScreen with the specified facility name. The HomeScreen composable function should accept a lambda that takes a String (facility name) and uses it in the navigateToMap function. This ensures that the correct facility name is passed when navigating to the MapScreen.

In your AppNavGraph, you can modify the composable block for HomeScreen to capture the facility name and pass it to the MapScreen:

kotlin

composable(route = HomeDestination.route) { backStackEntry ->
    val navController = rememberNavController()
    HomeScreen { facilityName ->
        navController.navigate("${MapDestination.route}/$facilityName")
    }
}

This way, when the HomeScreen composable is created, it receives the facilityName as an argument and uses it in the navigateToMap lambda, ensuring that the correct facility name is passed to the MapScreen.
User
Yes, my application already does that. I want the facility name input into the navigateToMap function (in this case, SETU), to be received by the MapScreen/MapViewModel
ChatGPT

I see, thanks for clarifying. In that case, you can use the NavBackStackEntry in the MapScreen composable to retrieve the facility name from the arguments. Here's how you can modify your MapScreen:

kotlin

@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController = rememberNavController(),
) {
    val context = LocalContext.current

    // Retrieve facility name from arguments using NavBackStackEntry
    val facilityName: String? = navController
        .currentBackStackEntry
        ?.arguments
        ?.getString(MapDestination.facility)

    // Observe the facility name and trigger data retrieval
    LaunchedEffect(facilityName) {
        facilityName?.let { name ->
            mapViewModel.retrieveFacility(name)
        }
    }

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = {
                    navController.navigateUp()
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MapBody()
        }
    }
}

With this modification, the MapScreen now retrieves the facility name from the navigation arguments using navController.currentBackStackEntry.arguments.getString(MapDestination.facility). It then uses this facility name to trigger the data retrieval in the MapViewModel.
User
This does not work, as facilityName is null when the LaunchedEffect occurs
ChatGPT

I apologize for the confusion. To handle the scenario where facilityName is null when LaunchedEffect occurs, you can use the LaunchedEffect to check for nullability of facilityName and then retrieve it from the NavBackStackEntry. Here's an updated version:

kotlin

@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController = rememberNavController(),
) {
    val context = LocalContext.current

    // Use LaunchedEffect to retrieve facilityName when available
    LaunchedEffect(navController.currentBackStackEntry) {
        val facilityName = navController.currentBackStackEntry?.arguments?.getString(MapDestination.facility)
        facilityName?.let { name ->
            mapViewModel.retrieveFacility(name)
        }
    }

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.map_title),
                canNavigateBack = canNavigateBack,
                navigateUp = {
                    navController.navigateUp()
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            MapBody()
        }
    }
}

This ensures that the LaunchedEffect will only attempt to retrieve and use facilityName when it is non-null, preventing potential issues when the effect runs before the value is set. If facilityName is null, the effect will not attempt to call retrieveFacility.
User
This also does not work, as "val facilityName = navController.currentBackStackEntry?.arguments?.getString(MapDestination.facility)" returns null
ChatGPT

I apologize for the confusion. If the facilityName is returning null, it indicates that the navigation arguments may not be set correctly. Let's ensure that the facility name is passed correctly when navigating to the MapScreen.

In your HomeScreen, when calling navigateToMap, make sure you pass the facility name as an argument:

kotlin

@Composable
fun HomeScreen(navigateToMap: (String) -> Unit) {
    // ...

    Button(
        onClick = {
            navigateToMap("SETU")
        }
    ) {
        Text("Navigate to Map")
    }

    // ...
}

Ensure that you are passing the facility name as an argument when invoking the navigateToMap function.

In your AppNavGraph, make sure that the facility name is correctly set in the navigation arguments:

kotlin

composable(
    route = MapDestination.routeWithArgs,
    arguments = listOf(
        navArgument(MapDestination.facility) {
            type = NavType.StringType
        }
    )
) { backStackEntry ->
    val facilityName = backStackEntry.arguments?.getString(MapDestination.facility)
    MapScreen(
        onNavigateUp = { navController.navigateUp() },
        facilityName = facilityName
    )
}

Make sure that the navArgument is correctly set for MapDestination.facility. This should allow the facilityName to be retrieved correctly in the MapScreen. If the issue persists, double-check the navigation setup and ensure that the arguments are being passed as expected.
```
