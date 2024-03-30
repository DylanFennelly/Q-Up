package com.example.qup.ui.attraction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.qup.data.FacilityRepository

class AttractionViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel(){
    val attractionId: Int =
        checkNotNull(savedStateHandle[AttractionDestination.attractionID])
}