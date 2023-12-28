package com.example.qup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.qup.ui.home.HomeDestination
import com.example.qup.ui.home.HomeScreen
import com.example.qup.ui.main.MapDestination
import com.example.qup.ui.main.MapScreen
import com.google.android.gms.maps.model.LatLng

//Defines navigation destinations for app views
//NavGraph and NavigationDestination code reference: John Rellis, Lab-Room InventoryApp, Mobile App Development 1, South East Technological University
@Composable
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
        ){ backStackEntry ->            //Generative AI Usage 1.
            val facilityName = backStackEntry.arguments?.getString(MapDestination.facility)

            //hardcoded; for some reason, it just will not read in the data directly from the data object.
            val mapLocation = when(facilityName){
                "SETU" -> LatLng(52.245866910002846, -7.138898812594175)
                else -> LatLng(0.0,0.0)
            }

            val mapZoom = when(facilityName){
                "SETU" -> 16f
                else -> 0f
            }

            if (facilityName != null) {
                MapScreen(
                    onNavigateUp = { navController.navigateUp() },
                    facilityName = facilityName,
                    mapLatLng = mapLocation,
                    mapZoom = mapZoom
                )
            }
        }
    }
}