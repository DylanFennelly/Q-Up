package com.example.qup.ui.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.home.HomeDestination
import com.example.qup.ui.home.HomeScreen
import com.example.qup.ui.attraction.AttractionDestination
import com.example.qup.ui.attraction.AttractionScreen
import com.example.qup.ui.camera.CameraDestination
import com.example.qup.ui.camera.CameraScreen
import com.example.qup.ui.camera.RequestLoading
import com.example.qup.ui.main.ListDestination
import com.example.qup.ui.main.ListScreen
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.main.MapDestination
import com.example.qup.ui.main.MapScreen
import com.example.qup.ui.main.PermissionsDestination
import com.example.qup.ui.main.PermissionsScreen
import com.example.qup.ui.main.QueuesDestination
import com.example.qup.ui.main.QueuesScreen
import com.example.qup.ui.ticket.TicketDestination
import com.example.qup.ui.ticket.TicketScreen
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

//Defines navigation destinations for app views
//NavGraph and NavigationDestination code reference: John Rellis, Lab-Room InventoryApp, Mobile App Development 1, South East Technological University
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    //same view model for multiple screens -> initialise once, pass into screens
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToMap = {
                    mainViewModel.refreshData()
                    navController.navigate(MapDestination.route)

                },
                navigateToCamera = {
                    navController.navigate(CameraDestination.route)
                },
                navigateToPermissions = {navController.navigate(PermissionsDestination.route)},
                mainViewModel = mainViewModel
            )
        }

        composable(route = CameraDestination.route) {
            CameraScreen(
                onNavigateUp = {
                    navController.navigate(HomeDestination.route)
                },
                navigateToMap = {
                    mainViewModel.refreshData()
                    navController.navigate(MapDestination.route)
                },
                mainViewModel = mainViewModel
            )
        }

        composable(route = PermissionsDestination.route) {
            PermissionsScreen(
                navigateToHome = {
                    navController.navigate(HomeDestination.route)
                },
                onNavigateUp = { navController.popBackStack() })
        }

        composable(
            route = MapDestination.route,
        ) {
            val lat = remember { mutableDoubleStateOf(0.0) }
            val lng = remember { mutableDoubleStateOf(0.0) }

            LaunchedEffect(true) {
                lat.doubleValue = mainViewModel.mapLat.first()
                lng.doubleValue = mainViewModel.mapLng.first()
            }
            val mapLocation = LatLng(lat.doubleValue, lng.doubleValue)
            val mapZoom = 16f

            //only create map if values have been updated
            if (lat.doubleValue != 0.0 && lng.doubleValue != 0.0) {
                MapScreen(
                    onNavigateUp = {
                        navController.navigate(HomeDestination.route)
                    },
                    mapLatLng = mapLocation,
                    mapZoom = mapZoom,
                    navigateToList = {
                        navController.navigate(ListDestination.route)
                    },
                    navigateToQueues = {
                        navController.navigate(QueuesDestination.route)
                    },
                    mainViewModel = mainViewModel,
                    mainUiState = mainViewModel.mainUiState,
                    queuesUiState = mainViewModel.queuesUiState,
                    navigateToAttraction = {
                        navController.navigate("${AttractionDestination.route}/${it}")
                    }
                )
            }else{
                RequestLoading()
            }




        }

        composable(
            route = ListDestination.route,
        ) {
            ListScreen(
                onNavigateUp = {
                    navController.navigate(MapDestination.route)
                },
                navigateToMap = {
                    navController.navigate(MapDestination.route)
                },
                navigateToQueues = {
                    navController.navigate(QueuesDestination.route)
                },
                navigateToAttraction = { navController.navigate("${AttractionDestination.route}/${it}") },
                mainViewModel = mainViewModel,
                listUiState = mainViewModel.mainUiState,
                queuesUiState = mainViewModel.queuesUiState
            )

        }

        composable(
            route = QueuesDestination.route,
        ) {
            QueuesScreen(
                onNavigateUp = {
                    navController.navigate(MapDestination.route)
                },
                navigateToMap = {
                    navController.navigate(MapDestination.route)
                },
                navigateToList = {
                    navController.navigate(ListDestination.route)
                },
                navigateToAttraction = { navController.navigate("${AttractionDestination.route}/${it}") },
                navigateToTicket = { attractionId, userId -> navController.navigate("${TicketDestination.route}/$attractionId/$userId")},
                mainViewModel = mainViewModel,
                mainUiState = mainViewModel.mainUiState,
                queuesUiState = mainViewModel.queuesUiState
            )

        }

        composable(
            route = AttractionDestination.routeWithArgs,
            arguments = listOf(navArgument(AttractionDestination.attractionID) {
                type = NavType.IntType
            })
        ) {backStackEntry -> //Generative AI Usage 1.
            //val attractionId = backStackEntry.arguments?.getString(AttractionDestination.attractionID)
            //Log.i("ViewModel", "attractionId: ${attractionId}")
            //if (attractionId != null) {
                AttractionScreen(
                    onNavigateUp = { navController.navigate(ListDestination.route) },
                    navigateToMap = { navController.navigate(MapDestination.route) },
                    navigateToQueues = {
                        navController.navigate(QueuesDestination.route)
                    },
                    mainViewModel = mainViewModel,
                    attractionUiState = mainViewModel.mainUiState,
                    queuesUiState = mainViewModel.queuesUiState,
                   // attractionIdString = attractionId
                )
           //}
        }

        composable(
            route = TicketDestination.routeWithArgs,
            arguments = listOf(
                navArgument(TicketDestination.attractionId) {
                    type = NavType.IntType
                },
                navArgument(TicketDestination.userId) {
                    type = NavType.IntType
                },
            )
        ) {backStackEntry ->
            TicketScreen(
                backStackEntry = backStackEntry,
                onNavigateUp = {
                    navController.navigate(QueuesDestination.route)
                },
                navigateToMap = {
                    navController.navigate(MapDestination.route)
                },
                navigateToList = {
                    navController.navigate(ListDestination.route)
                },
                mainViewModel = mainViewModel
            )
        }
    }
}