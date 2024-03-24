package com.example.qup.ui.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.testAttraction
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.navigation.NavigationDestination

object ListDestination: NavigationDestination {
    override val route = "list"
    override val titleRes = R.string.attraction_list_button
//    const val facility = "facility"             //determines which attraction data to load
//    val routeWithArgs = "$route/{$facility}"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ListScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: (String) -> Unit,
    navigateToMap: (String) -> Unit,
    mainViewModel: MainViewModel,
    facilityName: String,
    listUiState: MainUiState
){
    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = stringResource(R.string.attraction_list_button),
                canNavigateBack = canNavigateBack,
                navigateUp = {onNavigateUp(mainViewModel.getFacilityName())}
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = true, mapSelected = false, navigateToMap= { navigateToMap(mainViewModel.getFacilityName()) }) }
    ) {innerPadding ->

        Box(modifier = Modifier.padding(innerPadding)) {
            when(listUiState){
                is MainUiState.Loading -> ListLoading()
                is MainUiState.Success -> ListBody(listUiState.attractions)
                is MainUiState.Error -> ListError()
            }
        }
    }
}

@Composable
fun ListBody(
    attractions: List<testAttraction>,
    modifier: Modifier = Modifier
){
    Column {
        for (attraction in attractions) {
            Text(text = attraction.name)
            Text(text = "${attraction.lat}")
            Text(text = "${attraction.lng}")
        }
    }
}

@Composable
fun ListLoading(modifier: Modifier = Modifier){
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.loading_img),
        contentDescription = stringResource(R.string.loading)
    )
}

@Composable
fun ListError(modifier: Modifier = Modifier){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_connection_error),
            contentDescription = ""
        )
        Text(
            text = stringResource(R.string.loading_failed),
            modifier = Modifier.padding(16.dp)
        )
    }
}