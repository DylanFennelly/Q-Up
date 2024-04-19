package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination

object QueuesDestination: NavigationDestination {
    override val route = "queues"
    override val titleRes = R.string.queues_button
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun QueuesScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToList: () -> Unit,
    navigateToAttraction: (Int) -> Unit,
    mainViewModel: MainViewModel,
    facilityName: String,
    mainUiState: MainUiState
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.queues_button),
                canNavigateBack = canNavigateBack,
                navigateUp = {onNavigateUp()}
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = false, mapSelected = false, queuesSelected = true, navigateToMap= { navigateToMap() }, navigateToList = {navigateToList()}) }
    ) { innerPadding ->


    }
}