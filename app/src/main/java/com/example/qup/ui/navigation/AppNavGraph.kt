package com.example.qup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.qup.ui.home.HomeDestination
import com.example.qup.ui.home.HomeScreen

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
                    navigateToMap = {}
                )
            }
    }
}