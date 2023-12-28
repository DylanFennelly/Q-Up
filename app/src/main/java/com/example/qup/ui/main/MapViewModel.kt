package com.example.qup.ui.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.qup.data.AttractionRepository

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val attractionRepository: AttractionRepository
): ViewModel() {

}