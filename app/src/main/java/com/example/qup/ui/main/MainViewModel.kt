package com.example.qup.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.FacilityRepository
import com.example.qup.data.testAttraction
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface MainUiState {
    data class Success(val attractions: List<testAttraction>) : MainUiState
    object Error : MainUiState
    object Loading : MainUiState
}

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel() {
    var mainUiState: MainUiState by mutableStateOf(MainUiState.Loading)
        private set

    init{
        Log.i("ViewModel","MainViewModel Init")
        //getFacilityAttractions()
    }

    fun getFacilityName(): String{
        // get value from savedStateHandle
        return checkNotNull(savedStateHandle["facilityName"])
    }

    fun setFacilityName(facilityName : String){
        savedStateHandle["facilityName"] = facilityName
    }

    //TODO: Add URL String input to function and facilityRepo function
    fun getFacilityAttractions(){
        Log.i("ViewModel","Starting API request")
        viewModelScope.launch {
            mainUiState = try {
                Log.i("ViewModel", "Starting coroutine")
                val listResult = facilityRepository.getAttractions()
                Log.i("ViewModel", "API result: $listResult.attractionList")
                MainUiState.Success(listResult.list)
            }catch (e: IOException){
                Log.e("ViewModel", "Error on API Call: $e")
                MainUiState.Error
            }
        }
    }

}