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
        ){
            MapScreen()
        }
    }
}