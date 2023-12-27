package com.example.qup.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.qup.QueueTopAppBar
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.theme.QueueTheme

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    Scaffold(
        topBar = { QueueTopAppBar(title = "Home") }
    ) { innerPadding ->         //https://stackoverflow.com/questions/66573601/bottom-nav-bar-overlaps-screen-content-in-jetpack-compose
        Box(modifier = Modifier.padding(innerPadding)) {
            Greeting("Android")
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
        Greeting("Android")
    }
}