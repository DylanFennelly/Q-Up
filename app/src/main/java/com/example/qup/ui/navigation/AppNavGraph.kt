package com.example.qup.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
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
import com.example.qup.ui.main.ListDestination
import com.example.qup.ui.main.ListScreen
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.main.MapDestination
import com.example.qup.ui.main.MapScreen
import com.google.android.gms.maps.model.LatLng

//Defines navigation destinations for app views
//NavGraph and NavigationDestination code reference: John Rellis, Lab-Room InventoryApp, Mobile App Development 1, South East Technological University
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    //same view model for multiple screens -> initialise once, pass into screens
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)        //TODO: possibly move? idk how many standards im violating by initialising here
){
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
        ){
            composable(route = HomeDestination.route){
                HomeScreen(
                    navigateToMap = {
                        mainViewModel.setFacilityName(it)
                        navController.navigate(MapDestination.route)
                    }
                )
            }

            composable(
                route = MapDestination.route,
            ){
                //hardcoded; for some reason, it just will not read in the data directly from the data object.
                val mapLocation = LatLng(52.245866910002846, -7.138898812594175)
                val mapZoom = 16f

                MapScreen(
                    onNavigateUp = {
                        navController.navigate(HomeDestination.route)
                                   },
                    facilityName = mainViewModel.getFacilityName(),
                    mapLatLng = mapLocation,
                    mapZoom = mapZoom,
                    navigateToList = {
                        navController.navigate(ListDestination.route)
                                     },
                    mainViewModel = mainViewModel,
                    mainUiState = mainViewModel.mainUiState
                )

            }

        composable(
            route = ListDestination.route,
        ){
                ListScreen(
                    onNavigateUp = {
                        navController.navigate(MapDestination.route)
                                   },
                    facilityName = mainViewModel.getFacilityName(),
                    navigateToMap = {
                        navController.navigate(MapDestination.route)
                                    },
                    mainViewModel = mainViewModel,
                    listUiState = mainViewModel.mainUiState
                )
            }
        }

        //keeping as example of route with args
//        composable(
//            route = ListDestination.routeWithArgs,
//            arguments = listOf(navArgument(ListDestination.facility){
//                type = NavType.StringType
//            })
//        ){backStackEntry ->            //Generative AI Usage 1.
//            val facilityName = backStackEntry.arguments?.getString(ListDestination.facility)
//
//            if (facilityName != null) {
//                ListScreen(
//                    onNavigateUp = {
//                        navController.navigate("${MapDestination.route}/${it}")
//                    },
//                    facilityName = facilityName,
//                    navigateToMap = {
//                        navController.navigate("${MapDestination.route}/${it}")
//                    },
//                    mainViewModel = mainViewModel,
//                    listUiState = mainViewModel.mainUiState
//                )
//            }
//        }
}
