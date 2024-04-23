package com.example.qup.ui.attraction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class AttractionViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel(){
    //TODO: copy how backstack arguments are used in TicketScreen
    val attractionId: Int =
        checkNotNull(savedStateHandle[AttractionDestination.attractionID])
}