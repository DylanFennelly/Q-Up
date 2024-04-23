package com.example.qup.ui.ticket

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.example.qup.QueueBottomAppBar
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.navigation.NavigationDestination
import kotlinx.coroutines.flow.first

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
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    ticketViewModel: TicketViewModel = viewModel(factory = AppViewModelProvider.Factory),
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val attractionId = backStackEntry.arguments?.getInt(TicketDestination.attractionId)
    val userId = backStackEntry.arguments?.getInt(TicketDestination.userId)
    val scope = rememberCoroutineScope()

    //running the checkForQueue function once upon composition
    LaunchedEffect(key1 = true){
        val baseUrl = mainViewModel.baseUrl.first()
        if (attractionId != null && userId != null) {
            ticketViewModel.checkForQueue(attractionId, userId, baseUrl)
        }
    }

    BackHandler {
        onBack()
    }

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
        Box{
            when(ticketViewModel.ticketUiState){
                is TicketUiState.Loading ->{
                    TicketLoading()
                }
                is TicketUiState.Success ->{
                    (ticketViewModel.ticketUiState as TicketUiState.Success).qrBitmap?.let{
                        val qrBitmap = it.asImageBitmap()
                        TicketBody(
                            qrBitmap = qrBitmap,
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                        )
                    }
                }
                is TicketUiState.Error ->{
                    Text(text = "Error")
                }
            }
        }
    }
}

@Composable
fun TicketBody(
    modifier: Modifier = Modifier,
    qrBitmap: ImageBitmap,

    ){
    Column(
        modifier = modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "This is your entrance ticket",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom =  16.dp)
        )
        Text(
            text = "Present this ticket at the attraction entrance to gain entry.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom =  16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1.0f)
                .background(
                    color = colorResource(id = R.color.light_baby_blue),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = qrBitmap,
                contentDescription = stringResource(id = R.string.entrance_ticket_title)
            )
        }
        Text(
            text = "Important Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = "Do not scan this ticket yourself. Doing so may cause you to lose your entrance ticket.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Brightness",
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = "Ensure your screen brightness is sufficient for the QR code to be visible.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }
}

@Composable
fun TicketLoading(
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = colorResource(id = R.color.baby_blue),
            trackColor = colorResource(id = R.color.light_baby_blue),
            )
    }
}