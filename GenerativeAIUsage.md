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

## Generative AI Usage 5.

#### Usage Overview:
In this instance, ChatGPT 3.5 was help with restoring the current App state when bringing the app to the foreground through a notification.

#### Chat log:
```
User:

This is my function to send a notification:
"fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
        .setSmallIcon(R.drawable.logo)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())     //permission to send notifications always granted by this point
    }
}"

How do I create an tap action so that the app is brought to focus when a notification is tapped?


ChatGPT:

To make the app come to focus or launch a specific activity when the user taps on a notification, you'll need to use a PendingIntent. This intent will specify the action to take when the notification is tapped. Usually, this action is to open a specific activity within your application.

Here’s how you can modify your sendNotification function to include a tap action that opens a specific activity when the notification is tapped:
Step 1: Create an Intent for the Activity

First, create an Intent that launches the activity you want to open when the notification is tapped.
Step 2: Wrap the Intent in a PendingIntent

This intent needs to be wrapped in a PendingIntent. A PendingIntent gives the Android system the permission and capability to execute the intent operation on behalf of your application, at a later time or in response to certain events (like a user tapping the notification).
Step 3: Set the PendingIntent to the Notification Builder

Attach this PendingIntent to the NotificationCompat.Builder instance using the setContentIntent method.

Here’s how you can integrate these steps into your existing sendNotification function:

kotlin

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.qup.R // Adjust the import based on your actual R class import

fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
    // Create an intent that will be fired when the user taps the notification.
    val intent = Intent(context, MainActivity::class.java) // Replace MainActivity with the activity you want to launch.
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    // Wrap the intent into a PendingIntent
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    // Build the notification as before, adding the PendingIntent
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
        .setSmallIcon(R.drawable.logo) // Make sure you have a valid drawable resource for the icon.
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent) // Set the PendingIntent to the builder
        .setAutoCancel(true) // Automatically remove the notification when tapped.

    // Notify the user
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}

Explanation:

    Intent and PendingIntent: The intent is configured to start your main activity (or any other activity you choose). The PendingIntent.getActivity method is used to create a PendingIntent that will start an activity. The flags FLAG_ACTIVITY_NEW_TASK and FLAG_ACTIVITY_CLEAR_TASK are set to ensure that when the activity starts, it clears any existing tasks and starts a new task for this activity, providing a clean state.
    Notification Builder: The setContentIntent method is used to set the PendingIntent on the notification builder. This means that when the notification is tapped, the intent wrapped by this PendingIntent will be executed, thus opening the activity.
    Auto-cancel: Setting setAutoCancel(true) on the builder makes sure that the notification dismisses itself when it is tapped.

Make sure to adjust the Intent to target the correct activity in your application and ensure that all activities you might launch via notifications are properly declared in your AndroidManifest.xml. Also, adjust the icon (setSmallIcon(R.drawable.logo)) to a suitable one for your application.


User:

That isnt quite doing what I want it to do. I want it to return to the state the App was in. Currently, this code returns the app to the state it is in when it is launched (Home screen)


ChatGPT:

If you want the notification tap to bring the app to its current state, rather than starting a new activity or resetting to the home screen, you need to adjust how the Intent is set up in the PendingIntent. The goal is to ensure that the Intent used respects the current task and back stack of the application rather than creating a new task or clearing the existing one.

Here’s how to modify the Intent and PendingIntent to restore the app to its current state:
Using Existing Task and Stack

If your app is already running and you want the notification to simply bring the app back to the front (whatever state it was left in), you can use the following approach:

kotlin

fun sendNotification(context: Context, title: String, content: String, notificationId: Int) {
    // Intent that mimics the launcher intent
    val intent = Intent(context, MainActivity::class.java).apply {
        // Set the action and category to mimic the launcher
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // Wrap the intent into a PendingIntent
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Build the notification as before, adding the PendingIntent
    val builder = NotificationCompat.Builder(context, "CHANNEL_ID")
        .setSmallIcon(R.drawable.logo) // Ensure you have a valid icon
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(NotificationCompat.BigTextStyle().bigText(content))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true) // Automatically remove the notification when tapped

    // Notify the user
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}

Key Changes:

    Intent Flags: The use of Intent.FLAG_ACTIVITY_NEW_TASK is important as it attempts to use an existing task that already has the specified activity on top. This flag ensures that if the activity is already running in the foreground, it will not create a new instance but bring the existing one to the front.

    Mimicking the Launcher: By setting ACTION_MAIN and CATEGORY_LAUNCHER, the intent mimics the launcher's intent, which is typically used to start your app from the home screen. This makes sure that the activity stack is preserved.

    PendingIntent Flags: PendingIntent.FLAG_UPDATE_CURRENT ensures that if there is a pending intent already created for the same Intent, it will be updated with the new Intent. The PendingIntent.FLAG_IMMUTABLE flag is used for security best practices, ensuring that the intent’s data cannot be changed after it's created.


```

## Generative AI Usage 6.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to help with requesting multiple permissions at once.

#### Chat log:
```
User:

I am developing an Android app using Kotlin and Jetpack Compose. The app requires permissions for both Notifications and User Location. Both of these permissions are in the Manifest file. I have a Composable Screen view that is used to explain to the user what each permission is being used for, along with then asking for the permissions upon button click. Currently, it is only checking for one permission - Notifications. I want to add an additional permission to this check, such that when the button is pressed (and it MUST occur on a button press), it asks for both the Notification and Location permissions before proceeding to the next app view, MapView. Below is snippets from my code relevant to the permission check:

PermissionsScreen:
"@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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


    //Generate AI Usage 4.
    //Observes for changes in the settings for permission updates
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            //when app is resumed, check permissions
            if (event == Lifecycle.Event.ON_RESUME) {
                if (
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    //&& other permissions
                    ) {
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
        topBar = { QueueTopAppBar(title = stringResource(id = R.string.permissions_title), navigateUp = onNavigateUp, canNavigateBack = canNavigateBack)}
    ) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            PermissionsBody(context = context, requestPermissionLauncher = requestPermissionLauncher)
        }
        ShowPermissionDeniedDialog(showDeniedDialogState, context)
    }
}"

checkAllPermissions:
"@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkAllPermissions(context: Context, requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>){
    checkNotificationPermission(context, requestPermissionLauncher)
}"

checkNotificationPermission:
"@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
}"

ShowPermissionDeniedDialog:
"@Composable
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
}"


ChatGPT:

To modify your existing Jetpack Compose app to request both Notifications and Location permissions simultaneously upon a button click, you will need to update your permissions handling code to support checking and requesting multiple permissions at once. This involves using the ActivityResultContracts.RequestMultiplePermissions instead of ActivityResultContracts.RequestPermission, which is used for a single permission.

Here is how you can adjust your existing Kotlin code:

    Update the Permissions Screen: Change the launcher to handle multiple permissions.
    Modify the permission check functions: You need to adapt the check and request logic to handle multiple permissions.

Step 1: Update Permissions Screen

Update the PermissionsScreen composable to use ActivityResultContracts.RequestMultiplePermissions:

kotlin

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = true,
    navigateToMap: (String) -> Unit,
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current
    val showDeniedDialogState = remember { mutableStateOf(false) }

    // New launcher for requesting multiple permissions
    val requestMultiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Log.d("Permissions", "All permissions granted")
            navigateToMap("map_screen_route")
        } else {
            showDeniedDialogState.value = true
        }
    }

    // You may reuse other parts of your existing composable here...

    // Now use requestMultiplePermissionsLauncher in your button click
    Button(onClick = { 
        checkAllPermissions(context, requestMultiplePermissionsLauncher)
    }) {
        Text("Request Permissions")
    }
}

Step 2: Modify the Permission Check Functions

Change the checkAllPermissions function to handle multiple permissions:

kotlin

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkAllPermissions(context: Context, requestMultiplePermissionsLauncher: ManagedActivityResultLauncher<Map<String, Boolean>>) {
    val requiredPermissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION // Add other permissions if needed
    )

    val allPermissionsGranted = requiredPermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    if (!allPermissionsGranted) {
        requestMultiplePermissionsLauncher.launch(requiredPermissions)
    } else {
        Log.d("Permissions", "All permissions are already granted")
        // Navigate or perform next steps directly
    }
}

Handling Permission Explanation

If your app requires a rationale for permissions, you should check each permission individually before calling launch on requestMultiplePermissionsLauncher. This can be done by showing custom UI elements or dialogs explaining why these permissions are needed before the permission request if the shouldShowRequestPermissionRationale condition is met for any permissions.

Finally, ensure your AndroidManifest.xml includes the necessary permissions, especially for location, which may require both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION depending on the accuracy you need.

This setup will prompt the user for all necessary permissions at once when the button is clicked, and you will be able to handle the result in the callback of the requestMultiplePermissionsLauncher.
```

## Generative AI Usage 7.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to help error handle Retrofit request.

#### Chat log:
```
User:

How can I make it so retrofit doesnt crash when it receives a bad status code (e.g. 404)?

ChatGPT:
For the simplest approach to handling HTTP errors like 404 in Retrofit without your application crashing, you can use the basic Retrofit error handling mechanism provided within the onResponse callback. Here's how you can do it in a straightforward way:

    Create your Retrofit instance and your service interface. (I assume you already have this set up, but just for completeness):

    java

Retrofit retrofit = new Retrofit.Builder()
    .baseUrl("https://your.api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build();

YourApiService service = retrofit.create(YourApiService.class);

Make an API call and handle responses in onResponse. Check if the response is successful or not, and handle accordingly:

java

    Call<YourResponseClass> call = service.getYourData();
    call.enqueue(new Callback<YourResponseClass>() {
        @Override
        public void onResponse(Call<YourResponseClass> call, Response<YourResponseClass> response) {
            if (response.isSuccessful()) {
                // Handle successful response
                YourResponseClass data = response.body();  // Your successful response
                // Process your data
            } else {
                // Handle all other responses (e.g., 404, 500)
                // This prevents the app from crashing due to non-2XX responses
                int statusCode = response.code();
                // You can log or display an error message based on statusCode
                Log.e("API Error", "Status Code: " + statusCode);
            }
        }

        @Override
        public void onFailure(Call<YourResponseClass> call, Throwable t) {
            // Handle failure like network error, parsing error etc.
            Log.e("API Call Failure", t.getMessage());
        }
    });

This method is the easiest and most direct way to handle different HTTP status codes using Retrofit. It doesn't crash your app and logs the error, helping in the debugging process or simply informing the user that something went wrong.


User:

I am using Kotlin, not Java


ChatGPT:

Alright, if you're using Kotlin, you can handle API errors gracefully with Retrofit in a very similar way. Here's a simplified example tailored to Kotlin syntax:
1. Set Up Your Retrofit Instance and API Service Interface

Ensure you have Retrofit set up with an interface for your API endpoints:

kotlin

val retrofit = Retrofit.Builder()
    .baseUrl("https://your.api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface ApiService {
    @GET("endpoint")
    fun fetchData(): Call<YourDataType>
}

2. Make an API Call and Handle Different HTTP Responses

Here’s how you would make an API call and manage responses, including handling errors like 404, in Kotlin:

kotlin

val service = retrofit.create(ApiService::class.java)
val call = service.fetchData()

call.enqueue(object : Callback<YourDataType> {
    override fun onResponse(call: Call<YourDataType>, response: Response<YourDataType>) {
        if (response.isSuccessful) {
            // This block handles the successful HTTP response (2XX)
            val data = response.body()  // Your successful response body
            // Process your data
        } else {
            // This block handles all other HTTP responses (e.g., 404, 500)
            // Use response.code() to handle different HTTP status codes
            val statusCode = response.code()
            when(statusCode) {
                404 -> Log.e("API Error", "Not Found")
                else -> Log.e("API Error", "Unhandled status code: $statusCode")
            }
        }
    }

    override fun onFailure(call: Call<YourDataType>, t: Throwable) {
        // This block handles failures like network errors, serialization errors, etc.
        Log.e("API Call Failure", t.message ?: "Unknown error")
    }
})

Key Points:

    Error Handling in onResponse: The onResponse function checks if the response is successful using response.isSuccessful. If not, it handles different status codes appropriately.
    Use of Callbacks: Kotlin's use of an anonymous object for the Callback interface provides a clean way to handle both success and error cases.
    Logging Errors: The example uses Log.e to output error information, which you can replace with user-facing error messages or other handling logic as needed.

This Kotlin example provides a straightforward way to handle various HTTP responses using Retrofit without crashing your application. It keeps your API interactions robust and user-friendly.


User:

The way the API calls are setup in my app, I dont have direct access to the service.

When I make the API call i want to error handle, its called through a FacilityRepository, defined as such:
"package com.example.qup.data

import com.example.qup.network.FacilityApiService

//middle-man between data and application
interface FacilityRepository {
    suspend fun getAttractions(url: String): GetAttractionsApiResponse
    suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>
    suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse
    suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse
    suspend fun getUserId(url: String): UserIdApiResponse
}

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository{
    override suspend fun getAttractions(url: String): GetAttractionsApiResponse = facilityApiService.getAttractions(url)
    override suspend fun joinQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse = facilityApiService.joinQueue(url, body)
    override suspend fun getUserQueues(url: String, userId: Int): List<QueueEntry>  = facilityApiService.getUserQueues(url, userId)
    override suspend fun leaveQueue(url: String, body: JoinLeaveQueueBody): JoinLeaveQueueApiResponse  = facilityApiService.leaveQueue(url, body)
    override suspend fun updateQueueCallNum(url: String, body: UpdateCallNumBody): UpdateCallNumApiResponse = facilityApiService.updateQueueCallNum(url, body)
    override suspend fun getUserId(url: String): UserIdApiResponse = facilityApiService.getUserId(url)
}"

The retrofit service is included as a value when the NetworkFacilityRespository is initialised


ChatGPT:

Given the structure of your FacilityRepository and NetworkFacilityRepository, where API interactions are abstracted away, the error handling for different HTTP responses can be efficiently managed within the repository layer. This way, you encapsulate all network-related error handling, making the rest of your application code cleaner and more focused on business logic.
Step-by-Step Guide for Error Handling in Repository

    Modify the API Service to Return Response<T>:
    Modify your Retrofit interface to return Response<T> instead of directly returning the data object. This allows you to handle HTTP response codes directly within your repository.

    kotlin

interface FacilityApiService {
    @GET
    suspend fun getAttractions(@Url url: String): Response<GetAttractionsApiResponse>
    // Define other methods similarly to return Response<>
}

Handle Success and Errors in the Repository Layer:
In your NetworkFacilityRepository, you can now check the response and handle errors accordingly.

kotlin

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService): FacilityRepository {
    override suspend fun getAttractions(url: String): GetAttractionsApiResponse {
        val response = facilityApiService.getAttractions(url)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("No data received")
        } else {
            throw Exception("Error fetching data: ${response.code()} ${response.message()}")
        }
    }
    // Apply similar logic to other methods
}

Using Try-Catch for Error Propagation:
When you call these repository methods from your view models or use cases, use try-catch blocks to handle exceptions, allowing you to react to errors (e.g., display error messages, log errors).

kotlin

viewModelScope.launch {
    try {
        val attractions = facilityRepository.getAttractions("your_url")
        // Use attractions in your UI
    } catch (e: Exception) {
        // Handle error, update UI accordingly
        _state.value = UIState.Error(e.message ?: "Unknown error")
    }
}

Improving Error Handling with Custom Exceptions:
To provide more granular control over error handling, consider defining custom exceptions for different types of errors. This helps differentiate between network issues, data parsing errors, and API-specific problems.

kotlin

    class DataFetchException(message: String): Exception(message)
    class NetworkException(message: String): Exception(message)
    class ApiException(message: String): Exception(message)

    // In your repository
    throw ApiException("Error ${response.code()}: ${response.message()}")

Benefits of This Approach

    Centralized Error Handling: By handling errors at the repository level, you ensure that all network-related errors are managed in one place. This reduces redundancy and makes maintenance easier.
    Cleaner Architecture: Your view models remain clean and focused on handling UI logic and state management. They only need to handle exceptions thrown by the repository, not the specifics of HTTP error codes.
    Scalability and Maintenance: With custom exceptions and clear error handling logic, it's easier to update and maintain the code as your application scales.

By structuring your error handling in this manner, you ensure that your Kotlin application can gracefully handle various network errors without crashing, improving the robustness and user experience of your app.


User:

I want to, after the request fails, display a different message in a Compose view based on the response code. How can I throw the Exception in a way that the reponseCode can be access by the UiState.Error that is recieving it can have the responseCode?


ChatGPT:

To display different messages in your Compose UI based on the response code, you can customize how you throw exceptions in your repository and how these are handled in your ViewModel to update the UI accordingly.

Here's a step-by-step approach to implement this:
1. Define a Custom Exception Class

Create a custom exception class that includes the response code so that it can be used in the UI to determine the error message.

kotlin

class ApiException(val code: Int, message: String): Exception(message)

2. Modify the Repository to Use the Custom Exception

When you handle errors in your repository, throw your custom ApiException, including the status code and a relevant message.

kotlin

class NetworkFacilityRepository(private val facilityApiService: FacilityApiService) : FacilityRepository {
    override suspend fun getAttractions(url: String): GetAttractionsApiResponse {
        val response = facilityApiService.getAttractions(url)
        if (response.isSuccessful) {
            return response.body() ?: throw IllegalStateException("No response body")
        } else {
            throw ApiException(response.code(), "Error fetching attractions: ${response.message()}")
        }
    }
}

3. Update ViewModel to Handle Custom Exception

In your ViewModel, handle this exception in your coroutine block. When catching the ApiException, update your UI state accordingly.

kotlin

class MyViewModel : ViewModel() {
    private val _uiState = mutableStateOf<UIState>(UIState.Success)
    val uiState: State<UIState> = _uiState

    fun fetchAttractions(url: String) {
        viewModelScope.launch {
            try {
                val data = facilityRepository.getAttractions(url)
                _uiState.value = UIState.Success(data)
            } catch (e: ApiException) {
                _uiState.value = UIState.Error(e.message, e.code)
            } catch (e: Exception) {
                _uiState.value = UIState.Error("Unknown error occurred", null)
            }
        }
    }
}

sealed class UIState {
    data class Success(val data: Any): UIState()
    data class Error(val message: String, val code: Int?): UIState()
}

4. Use the Error Code in Compose UI

In your Compose UI, you can now use the error code to display different messages or perform different actions based on the code.

kotlin

@Composable
fun AttractionsScreen(viewModel: MyViewModel) {
    val state = viewModel.uiState.value
    when (state) {
        is UIState.Success -> {
            // Display attractions
        }
        is UIState.Error -> {
            Text("Error: ${state.message}")
            if (state.code != null) {
                when(state.code) {
                    404 -> Text("Attractions not found.")
                    500 -> Text("Server error, please try again later.")
                    else -> Text("An unexpected error occurred.")
                }
            }
        }
    }
}

Conclusion

By using a custom exception that includes the response code, your ViewModel can pass detailed error information to your Compose UI, allowing it to react appropriately to different scenarios. This approach helps maintain a clean separation of concerns, with your UI logic depending solely on the state provided by the ViewModel.
```

## Generative AI Usage 8.

#### Usage Overview:
In this instance, ChatGPT 3.5 was used to assist with resizing a custom map icon.

#### Chat log:
```
User:

How can I change the size of the custom GoogleMap marker icon?


ChatGPT:

To change the size of a map marker icon in Google Maps when using Android's Google Maps API (including with Jetpack Compose), you typically need to resize the bitmap before creating a BitmapDescriptor from it. This involves modifying the drawable or bitmap resource programmatically before assigning it to the marker.

Here’s a step-by-step guide on how to resize the icon:
Step 1: Create a Utility Function to Resize the Bitmap

First, you'll need a utility function that can resize a bitmap to the desired width and height. This function takes the context, the resource ID of the drawable, and the new size parameters:

kotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.core.graphics.drawable.DrawableCompat

fun resizeMapIcons(context: Context, iconId: Int, width: Int, height: Int): BitmapDescriptor {
    val imageBitmap = BitmapFactory.decodeResource(context.resources, iconId)
    val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}

Step 2: Use the Function in Your ViewModel or Component

Now, use this function when initializing your marker icon in the ViewModel or right before setting up the markers, adjusting the width and height to your needs.

kotlin

class MainViewModel : ViewModel() {
    fun loadMapIcon(context: Context): BitmapDescriptor {
        return resizeMapIcons(context, R.drawable.map_marker, 100, 100) // Set your desired size here
    }
}

Step 3: Apply the Resized Icon to Your Marker

When creating your markers, use the resized icon:

kotlin

val viewModel: MainViewModel = viewModel()
val context = LocalContext.current
val resizedIcon = viewModel.loadMapIcon(context)

GoogleMap(
    // Other parameters
) {
    Marker(
        position = LatLng(37.4221, -122.0841), // Example position
        icon = resizedIcon,
        title = "Marker in Sydney",
        snippet = "Description"
    )
}

Additional Tips

    Aspect Ratio: When resizing icons, consider maintaining the aspect ratio to prevent the icon from appearing stretched or compressed.
    Performance: Resizing images can be computationally expensive. If you're placing many markers, consider resizing the icons beforehand and storing them, or use different icon sizes strategically based on the map zoom level.
    Context Usage: Ensure that the context used for BitmapFactory.decodeResource is appropriate. It’s typically safe to use the application context for loading resources.

By following these steps, you can control the size of your map marker icons in Google Maps within your Android application, making them suitable for different display sizes and resolutions.
```





