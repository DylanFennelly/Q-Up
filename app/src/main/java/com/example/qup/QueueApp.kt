package com.example.qup

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.qup.ui.navigation.AppNavGraph


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun QueueApp(navController: NavHostController = rememberNavController()){
    AppNavGraph(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
){
    CenterAlignedTopAppBar(
        title = {Text(title)},
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorResource(R.color.baby_blue),
            titleContentColor = Color.White
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomAppBar(
    listSelected: Boolean,
    mapSelected: Boolean,
    queuesSelected: Boolean,
    navigateToList: () -> Unit = {},
    navigateToMap: () -> Unit = {},
    navigateToQueues: () -> Unit = {}
){
    val listBG: Color =  if (listSelected) colorResource(id = R.color.dark_baby_blue) else colorResource(R.color.baby_blue)
    val mapBG: Color = if (mapSelected) colorResource(id = R.color.dark_baby_blue) else colorResource(R.color.baby_blue)
    val queuesBG: Color = if (queuesSelected) colorResource(id = R.color.dark_baby_blue) else colorResource(R.color.baby_blue)

    BottomAppBar(
        containerColor = colorResource(R.color.baby_blue),
        contentColor = Color.White,
        modifier = Modifier.height(64.dp)
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                modifier = Modifier
                    .weight(1F)
                    .background(listBG)
                    .fillMaxHeight(),
                onClick = navigateToList,
                enabled = !listSelected
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Filled.List,
                    contentDescription = stringResource(id = R.string.attraction_list_button)
                )
            }
            IconButton(modifier = Modifier
                .weight(1F)
                .background(mapBG)
                .fillMaxHeight(),
                onClick = navigateToMap,
                enabled = !mapSelected
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = R.drawable.map_fill0_wght400_grad0_opsz24),
                    contentDescription = stringResource(id = R.string.map_button)
                )
            }

            IconButton(modifier = Modifier
                .weight(1F)
                .background(queuesBG)
                .fillMaxHeight(),
                onClick = navigateToQueues,
                enabled = !queuesSelected
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(id = R.drawable.schedule_24px),
                    contentDescription = stringResource(id = R.string.queues_button)
                )
            }
        }
    }
}