package com.example.qup.ui.ticket

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination

object TicketDestination : NavigationDestination {
    override val route = "ticket"
    override val titleRes = R.string.entrance_ticket_title

    const val attractionId = "id"
    const val userId = "userId"

    val routeWithArgs = "$route/{$attractionId}/{$userId}"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(
    backStackEntry: NavBackStackEntry,
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToList: () -> Unit,
){
    val attractionId = backStackEntry.arguments?.getInt(TicketDestination.attractionId)
    val userId = backStackEntry.arguments?.getInt(TicketDestination.userId)

    Scaffold(
        modifier = Modifier,
        topBar = {
            QueueTopAppBar(
                title = stringResource(id = R.string.entrance_ticket_title),
                canNavigateBack = canNavigateBack,
                navigateUp = { onNavigateUp() }
            )
        },
        bottomBar = {
            QueueBottomAppBar(
                listSelected = false,
                mapSelected = false,
                queuesSelected = true,
                navigateToMap = { navigateToMap() },
                navigateToList = { navigateToList() })
        },
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)){
            Column {
                Text(text = "Attraction Id : $attractionId")
                Text(text = "User Id : $userId")
            }

        }

    }
}