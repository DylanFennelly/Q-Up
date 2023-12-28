package com.example.qup.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.navigation.NavigationDestination
import com.example.qup.ui.theme.QueueTheme

object HomeDestination: NavigationDestination{
    override val route = "home"
    override val titleRes = R.string.home_title

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navigateToMap: (String) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    Scaffold(
        //topBar = { QueueTopAppBar(title = "Home") }
    ) { innerPadding ->         //https://stackoverflow.com/questions/66573601/bottom-nav-bar-overlaps-screen-content-in-jetpack-compose
        Box(modifier = Modifier.padding(innerPadding)) {
            HomeBody(
                navigateToMap
            )
        }
    }
}

@Composable
fun HomeBody(
    onButtonClick: (String) -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter =  painterResource(id = R.drawable.logo),
            contentDescription = "App logo",
            modifier = Modifier.size(200.dp).padding(bottom = 32.dp)
        )
        Text(
            text = stringResource(R.string.app_welcome),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 64.dp)
        )
        Button(
            onClick = { onButtonClick("SETU") },        //TODO: Change to use actual data, not raw string
            colors = ButtonDefaults.buttonColors(colorResource(R.color.setu_grey))
        ) {
            Text(text = stringResource(R.string.setu_button))
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QueueTheme {
        HomeBody({})
    }
}