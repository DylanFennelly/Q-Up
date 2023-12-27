package com.example.qup

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.qup.ui.home.HomeScreen


@Composable
fun QueueApp(){
    HomeScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTopAppBar(
    title: String
){
    CenterAlignedTopAppBar(
        title = {Text(title)}
    )
}