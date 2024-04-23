package com.example.qup.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qup.app.QueueApplicationContainer
import com.example.qup.ui.attraction.AttractionViewModel
import com.example.qup.ui.camera.CameraViewModel
import com.example.qup.ui.home.HomeViewModel
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.ticket.TicketViewModel

//Provides factory to instantiate ViewModels
object AppViewModelProvider{
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel( queueApplicationContainer().container.facilityRepository,)
        }
        initializer {
            MainViewModel(
                queueApplicationContainer().container.facilityRepository,
                queueApplicationContainer().applicationContext,  //passing context for notifications
            )
        }
        initializer {
            AttractionViewModel(
                this.createSavedStateHandle(),
            )
        }

        initializer {
            TicketViewModel(
                queueApplicationContainer().container.facilityRepository,
            )
        }
        initializer {
            CameraViewModel(
                queueApplicationContainer().container.facilityRepository
            )
        }
    }
}

//Extension function to return instance of QueueApplicationContainer
fun CreationExtras.queueApplicationContainer() : QueueApplicationContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as QueueApplicationContainer)