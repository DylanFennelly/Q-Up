package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.QueueEntry
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
    mainUiState: MainUiState,
    queuesUiState: QueuesUiState
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
        Box(modifier = Modifier.padding(innerPadding)) {
            when(queuesUiState){
                is QueuesUiState.Loading -> {}
                is QueuesUiState.Success -> {
                    QueuesListBody(
                        queues = queuesUiState.userQueues,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                        )
                }
                is QueuesUiState.Error -> {}
            }
        }
    }

}

@Composable
fun QueuesListBody(
    queues: List<QueueEntry>,
    modifier: Modifier = Modifier,
    //onItemClick: (Int) -> Unit
){
    LazyColumn{
        items(queues){ queue ->
            QueueItem(queue = queue, Modifier
                .padding(8.dp)
                .clickable { }
            )
        }
    }
}

@Composable
fun QueueItem(
    queue: QueueEntry,
    modifier: Modifier = Modifier,
){
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.light_baby_blue), contentColor = colorResource(id = R.color.black))
    ) {
        Column(
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "${queue.attractionId}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "${queue.aheadInQueue}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}