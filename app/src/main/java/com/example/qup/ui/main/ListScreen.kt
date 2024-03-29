package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.ui.navigation.NavigationDestination
import com.example.qup.ui.theme.QueueTheme

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                is MainUiState.Success -> {
                    ListBody(
                        attractions = listUiState.attractions,
                        onItemClick = {},    //TODO Add click function
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    )
                }
                is MainUiState.Error -> ListError()
            }
        }
    }
}

@Composable
fun ListBody(
    attractions: List<Attraction>,
    modifier: Modifier = Modifier,
    onItemClick: (Attraction) -> Unit
){
    LazyColumn{
        items(attractions) {attraction ->
            AttractionItem(attraction, Modifier
                .padding(8.dp)
                .clickable { onItemClick(attraction) }
            )
        }
    }

}

@Composable
fun AttractionItem(attraction: Attraction, modifier: Modifier = Modifier){
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.dark_baby_blue), contentColor = colorResource(id = R.color.white))
    ) {
        Column(

        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = attraction.name,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.Bottom),
                    text = attraction.type,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = attraction.status,
                    style = MaterialTheme.typography.headlineSmall,
                    color = statusColor(staus = attraction.status)
                )
            }
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

@Composable
fun statusColor(staus: String): Color{
    return when (staus.lowercase()){
        "open" -> colorResource(id = R.color.open_green)
        "closed" -> colorResource(id = R.color.closed_red)
        "maintenance" -> colorResource(id = R.color.maintenance_yellow)
        else -> Color.White
    }

}

@Preview(showBackground = true)
@Composable
fun AttractionItemPreview(){
    QueueTheme {
        AttractionItem(
            attraction =
                Attraction(
                    id = 1,
                    name = "Walton Building",
                    description = "The Walton Building is located on the Institute’s main Cork Road campus close to the award-winning Institute library, Luke Wadding Library.  Named after Ernest TS Walton (the Co Waterford-born Nobel Physics Laureate) the 3,000 square metre Walton Building greatly enhances and expands the Institute’s world-class information and communications infrastructure. \n" +
                            "The 18 large computer laboratories in the building each feature an innovative passive air movement system that helps ensure comfortable learning conditions for users. A daylight-filled central atrium located alongside the entrance accommodates all circulation and social spaces.",
                    type = "School",
                    status = "Maintenance",
                    cost = 0f,
                    length = 6f,
                    lat = 52.2457368280431,
                    lng = -7.137318108777412
                )
        )
    }
}