package com.example.qup.ui.main

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.data.Attraction
import com.example.qup.data.QueueEntry
import com.example.qup.helpers.calculateEstimatedQueueTime
import com.example.qup.ui.attraction.queueTimeColour
import com.example.qup.ui.navigation.NavigationDestination

object QueuesDestination: NavigationDestination {
    override val route = "queues"
    override val titleRes = R.string.queues_button
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, refreshThreshold = 120.dp, onRefresh = { mainViewModel.refreshData(0) })  //TODO: hardcoded user ID

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
        Box(modifier = Modifier
            .padding(innerPadding)
            .pullRefresh(pullRefreshState)
        ) {
            when (mainUiState) {
                is MainUiState.Loading -> {
                    ListLoading()}
                is MainUiState.Success -> {
                    when (queuesUiState) {
                        is QueuesUiState.Loading -> {
                            ListLoading()}
                        is QueuesUiState.Success -> {
                            QueuesListBody(
                                queues = queuesUiState.userQueues,
                                attractions = mainUiState.attractions,
                                navigateToAttraction = navigateToAttraction,
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                            )
                        }
                        is QueuesUiState.Error -> {
                            ListError()}
                    }
                }
                is MainUiState.Error -> {
                    ListError()}
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState )
            }
        }
    }

}

@Composable
fun QueuesListBody(
    queues: List<QueueEntry>,
    attractions: List<Attraction>,
    modifier: Modifier = Modifier,
    navigateToAttraction: (Int) -> Unit
){
    LazyColumn{
        items(queues){ queue ->
            val linkedAttraction = attractions.find{it.id == queue.attractionId}

            //if user somehow in queue for non-existent attractionId, skip it
            if (linkedAttraction != null) {
                val queueTime = calculateEstimatedQueueTime(queue.aheadInQueue, linkedAttraction.avg_capacity, linkedAttraction.length)

                QueueItemExpandable(
                    title = linkedAttraction.name,
                    queueTime = queueTime,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Row {
                        Spacer(modifier = Modifier.weight(0.3f))
                        Button(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.closed_red)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.leave_queue_button),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.3f))
                        Button(
                            onClick = { navigateToAttraction(linkedAttraction.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.baby_blue)),
                            modifier = Modifier.weight(1f)
                            ) {
                            Text(
                                text = stringResource(id = R.string.attraction_detail_button),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.weight(0.3f))
                    }
                }
            }
        }
    }
}

//Expandable item - https://proandroiddev.com/creating-expandable-sections-with-compose-c0e827fb6910
@Composable
fun QueueItemTitle(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    title: String,
    queueTime: Int
){
    val icon = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown

    Column {
        Row(
            modifier = modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(10f)
            )
            Spacer(Modifier.weight(0.1f))
            Image(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                //colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer),
                contentDescription = stringResource(id = R.string.expand_collapse_label)
            )
        }
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f),
                text = stringResource(id = R.string.attraction_queue_time_remaining_label),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$queueTime " + stringResource(id = R.string.attraction_queue_time_unit),
                style = MaterialTheme.typography.headlineSmall,
                color = queueTimeColour(time = queueTime)
            )
        }
    }
}

@Composable
fun QueueItemExpandable(
    modifier: Modifier = Modifier,
    queueTime: Int,
    title: String,
    content: @Composable () -> Unit
){
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(
                color = colorResource(id = R.color.light_baby_blue),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            QueueItemTitle(
                isExpanded = isExpanded,
                title = title,
                queueTime = queueTime,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
            )

            AnimatedVisibility(
                modifier = Modifier
                    .background(color = colorResource(id = R.color.light_baby_blue))
                    .fillMaxWidth(),
                visible = isExpanded
            ) {
                content()
            }
        }
    }
}