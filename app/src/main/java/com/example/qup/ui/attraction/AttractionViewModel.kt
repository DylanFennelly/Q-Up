package com.example.qup.ui.attraction

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.FacilityRepository
import com.example.qup.data.JoinLeaveQueueBody
import kotlinx.coroutines.launch
import java.io.IOException

//UI State of join queue request and button


class AttractionViewModel(
    savedStateHandle: SavedStateHandle,
): ViewModel(){
    val attractionId: Int =
        checkNotNull(savedStateHandle[AttractionDestination.attractionID])
}