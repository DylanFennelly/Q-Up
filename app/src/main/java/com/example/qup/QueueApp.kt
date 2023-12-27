package com.example.qup

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.qup.ui.home.HomeScreen
import com.example.qup.ui.navigation.AppNavGraph


@Composable
fun QueueApp(navController: NavHostController = rememberNavController()){
    AppNavGraph(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTopAppBar(
    title: String
){
    CenterAlignedTopAppBar(
        title = {Text(title)}
    )
}