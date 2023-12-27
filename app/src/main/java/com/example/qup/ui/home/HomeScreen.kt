package com.example.qup.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
    navigateToMap: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    QueueTheme {
        Scaffold(
            //topBar = { QueueTopAppBar(title = "Home") }
        ) { innerPadding ->         //https://stackoverflow.com/questions/66573601/bottom-nav-bar-overlaps-screen-content-in-jetpack-compose
            Box(modifier = Modifier.padding(innerPadding)) {
                HomeBody()
            }
        }
    }

}

@Composable
fun HomeBody(){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_welcome),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(colorResource(R.color.setu_grey))
        ) {
            Text(text = stringResource(R.string.setu_button))
        }

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    QueueTheme {
        HomeBody()
    }
}