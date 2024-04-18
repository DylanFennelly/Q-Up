package com.example.qup.ui.attraction

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.FacilityRepository
import com.example.qup.data.JoinQueueBody
import kotlinx.coroutines.launch
import java.io.IOException

//UI State of join queue request and button
sealed interface JoinQueueUiState {
    data class Result(val statusCode: Int) : JoinQueueUiState
    object Loading : JoinQueueUiState   //Join Queue API request underway
    object Idle : JoinQueueUiState      //Request not underway
    object Error : JoinQueueUiState      //Error in request
}

class AttractionViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel(){
    val attractionId: Int =
        checkNotNull(savedStateHandle[AttractionDestination.attractionID])

    var joinQueueUiState: JoinQueueUiState by mutableStateOf(JoinQueueUiState.Idle)


    fun postJoinAttractionQueue(attractionId: Int, userId: Int){
        Log.i("AttractionViewModel","Starting Join Queue API request")
        viewModelScope.launch {
            joinQueueUiState = try {
                Log.i("AttractionViewModel","Starting coroutine")
                val joinResult = facilityRepository.joinQueue(body = JoinQueueBody(attractionId, userId))
                Log.i("AttractionViewModel", "API result: $joinResult")
                JoinQueueUiState.Result(joinResult.statusCode)
            }catch (e: IOException) {
                Log.e("AttractionViewModel", "Error on API Call: $e")
                JoinQueueUiState.Error
            }
        }
    }
}