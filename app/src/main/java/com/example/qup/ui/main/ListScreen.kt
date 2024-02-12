package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.navigation.NavigationDestination

object ListDestination: NavigationDestination {
    override val route = "list"
    override val titleRes = R.string.attraction_list_button
    const val facility = "facility"             //determines which attraction data to load
    val routeWithArgs = "$route/{$facility}"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToMap: (String) -> Unit,
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory),
    facilityName: String
){
    LaunchedEffect(facilityName) {
        mainViewModel.retrieveFacility(facilityName)
    }

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.attraction_list_button),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = true, mapSelected = false, navigateToMap= { navigateToMap(mainViewModel.facility.value.name) }) }
    ) {


    }
}