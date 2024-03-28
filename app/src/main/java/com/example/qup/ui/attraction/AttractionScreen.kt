package com.example.qup.ui.attraction

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.main.MainUiState
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.navigation.NavigationDestination

object AttractionDestination: NavigationDestination {
    override val route = "attraction"
    override val titleRes = R.string.attraction_detail_button
    const val attractionID = "id"             //determines which attraction data to load
    val routeWithArgs = "$route/{$attractionID}"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AttractionScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: (String) -> Unit,
    navigateToMap: (String) -> Unit,
    mainViewModel: MainViewModel,
    attractionUiState: MainUiState,
    attractionViewModel: AttractionViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val attractionId = attractionViewModel.attractionId

    Scaffold(
        topBar = {
            QueueTopAppBar(
                title = attractionId.toString(),
                canNavigateBack = canNavigateBack,
                navigateUp = {onNavigateUp(mainViewModel.getFacilityName())}
            )
        },
        bottomBar = { QueueBottomAppBar(listSelected = true, mapSelected = false, navigateToMap= { navigateToMap(mainViewModel.getFacilityName()) }) }
    ) {innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when(attractionUiState){
                is MainUiState.Loading -> {
                }
                is MainUiState.Success -> {
                    Log.i("ViewModel", attractionUiState.attractions.toString())
                    AttractionBody(
                        attraction = attractionUiState.attractions[attractionId],
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    )
                }
                is MainUiState.Error -> {}
            }
        }
    }
}

@Composable
fun AttractionBody(
    attraction: Attraction,
    modifier: Modifier = Modifier,
){
    Text(text = attraction.name)
}