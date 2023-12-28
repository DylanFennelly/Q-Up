package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination

object MapDestination: NavigationDestination {
    override val route = "map"
    override val titleRes = R.string.map_title
    const val facility = "facility"             //determines which attraction data to load
    val routeWithArgs = "$route/{$facility}"

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
){
    Scaffold(
        topBar = { QueueTopAppBar(
            title = stringResource(R.string.map_title),
            canNavigateBack = canNavigateBack,
            navigateUp = onNavigateUp
        )}
    ) {

    }
}