package com.example.qup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
){
    CenterAlignedTopAppBar(
        title = {Text(title)},
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(R.color.baby_blue),
            titleContentColor = Color.White
        )
    )
}