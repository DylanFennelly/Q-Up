package com.example.qup.ui.navigation


//Defines vals for NavGraph navigation
interface NavigationDestination {
    //unique view path name
    val route: String

    //view title to display om TopAppBar
    val titleRes: Int
}