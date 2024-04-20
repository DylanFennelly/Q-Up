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

## Generative AI Usage 2.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to help add custom styling to the GoogleMaps composable.

#### Chat log:
```
User

Using the GoogleMap composable, how can I hide street names on the map?


ChatGPT

To hide street names on a map using the Google Maps Composable in Jetpack Compose, you can utilize custom styling for the map. Google Maps allows you to customize the appearance of the map using a JSON style string, which can specify various features and elements to hide, show, or style differently.

Here’s how you can achieve hiding street names on the map:

    Create a JSON style string: You need to define a JSON style string that specifies hiding the street labels. Here’s an example of such a JSON string:

json

[
  {
    "featureType": "road",
    "elementType": "labels",
    "stylers": [
      {"visibility": "off"}
    ]
  }
]

This JSON string targets all road labels and sets their visibility to "off".

    Apply the style in your Composable: You can apply this style to your map using the GoogleMap composable from the Google Maps library for Android. Here's how you can integrate the style:

kotlin

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapStyleOptions
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun StyledMap() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 12f) // Example coordinates
    }

    val jsonStyle = """
        [
          {
            "featureType": "road",
            "elementType": "labels",
            "stylers": [
              {"visibility": "off"}
            ]
          }
        ]
    """.trimIndent()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = MapStyleOptions(jsonStyle))
    )
    // You can add more composables or features to your map here
}

This code snippet demonstrates setting up a GoogleMap composable with a camera position and a custom map style that hides street names.

Make sure to adjust the coordinates in CameraPosition.fromLatLngZoom to center the map according to your requirements. This approach provides a lot of flexibility, allowing you to customize different aspects of the map's appearance as desired.


User

Can I have the MapStyle as a .json file in the res directory?


ChatGPT

Yes, you can store your custom map styles in a .json file within the res directory of your Android project and then load this style into your Google Maps Composable. This method helps keep your code clean and makes it easier to manage or change the map style without modifying your Kotlin code directly.

Here's how you can set this up:
1. Create a JSON file with your style

Create a JSON file in your project’s res/raw directory. For example, create a file named map_style.json inside res/raw and put your JSON style configuration there:

json

[
  {
    "featureType": "road",
    "elementType": "labels",
    "stylers": [
      {"visibility": "off"}
    ]
  }
]

2. Load the JSON file in your Composable

You can read this file in your Jetpack Compose function and apply it to the map. Here’s how to do it:

kotlin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapStyleOptions
import com.google.maps.android.compose.rememberCameraPositionState
import java.io.InputStream

@Composable
fun StyledMap() {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 12f) // Example coordinates
    }

    val mapStyleOptions: MapStyleOptions? = loadMapStyle(context)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = mapStyleOptions)
    )
}

fun loadMapStyle(context: Context): MapStyleOptions? {
    return try {
        val rawResourceStream: InputStream = context.resources.openRawResource(R.raw.map_style)
        val bytes = rawResourceStream.readBytes()
        val jsonStyle = String(bytes)
        MapStyleOptions(jsonStyle)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

In this example:

    The JSON file is loaded from the res/raw directory using openRawResource(R.raw.map_style).
    The loadMapStyle function reads the content of the JSON file into a string and constructs a MapStyleOptions object from it.
    This MapStyleOptions is then applied to the GoogleMap composable.

This approach not only makes the code cleaner but also simplifies modifications to the map's appearance, as changes to the style can be done directly in the JSON file without any adjustments to the Kotlin code.
```
## Generative AI Usage 3.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to help with an issue regarding a type mismatch.

#### Chat log:
```
User:

I trying to ask for permissions in a Composable screen, PermissionsScreen, but I am getting the following error for checkNotificationPermission:
"shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
            // Explain to user reason for permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

-> Type mismatch.
Required:
Activity
Found:
String
No value passed for parameter 'permission'"

This code was working in the apps MainActivity. This is the entire PermissionsScreen code:
"package com.example.qup.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination

//Screen to explain and ask for permissions
object PermissionsDestination: NavigationDestination {
    override val route = "permissions"
    override val titleRes = R.string.permissions_title
}

fun showPermissionDeniedDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("Permission Denied")
        .setMessage("Notification permission was denied. Queue notifications will not be available. Re-open to app to allow permissions.")
        .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        .create()
        .show()
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkNotificationPermission(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
    when {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {}

        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
            // Explain to user reason for permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        else -> {
            // directly ask for the permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
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

    //Requesting permissions: https://developer.android.com/training/permissions/requesting

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // permission granted -> continue
        } else {
            // permission denied -> explain consequence of denying permissions
            showPermissionDeniedDialog(context)
        }
    }

    checkNotificationPermission(context, requestPermissionLauncher)

    Scaffold(
        topBar = { QueueTopAppBar(title = stringResource(id = R.string.permissions_title), navigateUp = onNavigateUp, canNavigateBack = canNavigateBack)}
    ) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            
        }
    }




}

@Composable
fun PermissionsBody(
    modifier: Modifier = Modifier,
){
    Column {
        Row {
            Text(text = stringResource(id = R.string.permissions_top))
        }
        Row {
            Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.permissions_notification_title))
            Column {
                Text(text = stringResource(id = R.string.permissions_notification_title))
                Text(text = stringResource(id = R.string.permissions_notification_desc))
            }
        }
        Text(text = stringResource(id = R.string.permissions_notification_desc))
        Button(onClick = { /*TODO*/ }) {
            
        }
    }
}


ChatGPT:

The error you are encountering in the checkNotificationPermission function when trying to use shouldShowRequestPermissionRationale indicates a type mismatch issue. Specifically, shouldShowRequestPermissionRationale is being used incorrectly because it expects two parameters: an Activity and a String for the permission.

In Android development, shouldShowRequestPermissionRationale is a method that belongs to Activity, not Context, and it checks whether you should show UI with rationale for requesting a permission. The method is used within an activity context to provide the user with more information about why a permission is needed after they have denied the request once but before they select "Don't ask again."
Correcting the Issue

To fix this issue, you need to ensure that the context object passed to shouldShowRequestPermissionRationale is cast to an Activity. In Compose, you can do this using LocalContext.current and then casting it to Activity when necessary. Here's how to correct your function:

kotlin

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkNotificationPermission(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
    when {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
            // Permission already granted, handle as needed
        }

        context is Activity && context.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
            // Show rationale dialog before making the request again
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        else -> {
            // No rationale needed; directly ask for the permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

"
```

## Generative AI Usage 4.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to fix an issue regarding the granting of app permissions through the settings application.

#### Chat log:
```
User:

I am trying to navigate to another screen (MapScreen) when the required permissions (only notification for now) are granted to the app. Currently, the code navigates the user to the if they accept the permissions, but if the user denies the permission, goes to the settings to enable the permission, and presses the button again, the alert dialog does not appear (as is expected), but the app does not navigate to the map. This is the current state of the code:
"package com.example.qup.ui.main

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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

    //Requesting permissions: https://developer.android.com/training/permissions/requesting
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions","All permissions granted")
            // all permissions granted -> continue to map
            navigateToMap("SETU")       //TODO: remove string
        } else {
            // permission denied -> show dialog
            showDeniedDialogState.value = true
        }
    }
    Scaffold(
        topBar = { QueueTopAppBar(title = stringResource(id = R.string.permissions_title), navigateUp = onNavigateUp, canNavigateBack = canNavigateBack)}
    ) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            PermissionsBody(context = context, requestPermissionLauncher = requestPermissionLauncher)
        }
        ShowPermissionDeniedDialog(showDeniedDialogState, context)
    }
}



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PermissionsBody(
    modifier: Modifier = Modifier,
    context: Context,
    requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>
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
fun checkAllPermissions(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>){
    checkNotificationPermission(context, requestPermissionLauncher)
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkNotificationPermission(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
    when {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                //&& Other permissions
        -> {
            Log.d("Permissions","Notification Permission granted")
            //Permission granted -> go to map
        }

        //Generative AI Usage 3.
        context is Activity && context.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
            // Explain to user reason for permission
            Log.d("Permissions","Notification Permission ask")
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        //Request other permissions here


        else -> {
            Log.d("Permissions","Notification Permission ask")
            // directly ask for the permission
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

"

Using logging, I can see that the "Log.d("Permissions","Notification Permission granted")" is triggered after the permission is granted in settings, but the navigation to the map doesnt occur.


ChatGPT:

To resolve the issue where the navigation to the map does not occur after permissions are granted from the app settings, you need to adjust the logic to actively check the permission status again when the user returns to the app and then perform the navigation if the permissions are now granted. This could happen because the ActivityResultLauncher won't trigger when the permission is manually granted through the settings.

Here’s a detailed approach to handle this:
Step 1: Use Lifecycle to Detect When the App Resumes

To detect when the user returns to the app from the settings, you can use lifecycle events in your Compose function. This is done by observing the Lifecycle of the activity and triggering a permission check when the app resumes.
Step 2: Add the Lifecycle Observer

Add a lifecycle observer in your PermissionsScreen to check permissions when the app comes into the foreground:

kotlin

@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    navigateToMap: (String) -> Unit,
    onNavigateUp: () -> Unit,
){
    val context = LocalContext.current as Activity
    val showDeniedDialogState = remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permissions", "All permissions granted")
            navigateToMap("map_screen_route")  // Assuming "map_screen_route" is the route for your MapScreen
        } else {
            showDeniedDialogState.value = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "Permission check on resume: GRANTED")
                    navigateToMap("map_screen_route")
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
        topBar = { QueueTopAppBar(title = stringResource(id = R.string.permissions_title), navigateUp = onNavigateUp, canNavigateBack = canNavigateBack) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            PermissionsBody(context = context, requestPermissionLauncher = requestPermissionLauncher)
        }
        ShowPermissionDeniedDialog(showDeniedDialogState, context)
    }
}

Explanation

    Lifecycle Observation: The DisposableEffect with LifecycleEventObserver is used to add and remove a lifecycle observer that checks the permission status when the app resumes (i.e., comes to the foreground). If the permissions are granted, it triggers the navigation.
    Permission Check on Resume: This ensures that if the user changes the permission from the settings (outside of your app's flow), the app correctly reacts when it resumes and navigates to the map if all necessary permissions are granted.

"
```

