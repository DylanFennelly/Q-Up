package com.example.qup.ui

import android.text.Spannable.Factory
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qup.app.QueueApplicationContainer
import com.example.qup.ui.home.HomeViewModel

//Provides factory to instantiate ViewModels
object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel()
        }
    }
}

//Extension function to return instance of QueueApplicationContainer
fun CreationExtras.queueApplicationContainer() : QueueApplicationContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as QueueApplicationContainer)