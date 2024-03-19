package com.example.qup.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.qup.app.QueueApplicationContainer
import com.example.qup.data.Facility
import com.example.qup.data.FacilityRepository
import com.example.qup.data.NetworkFacilityRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface MainUiState {
    data class Success(val attractions: String) : MainUiState
    object Error : MainUiState
    object Loading : MainUiState
}

class MainViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel() {
    //state of facility obtained from request
    //TODO - UN-HARDCODE SETU NAME
    val facility = mutableStateOf<Facility>(Facility("SETU", LatLng(0.0,0.0), 0f, emptyList()))
    //private val facilityName: String = checkNotNull(savedStateHandle[MapDestination.facility])
    var mainUiState: MainUiState by mutableStateOf(MainUiState.Loading)
        private set

    fun retrieveFacility(facilityName: String){
        viewModelScope.launch {
//            val retrievedFacilities = facilityRepository.getAttractions()
//
//            val selectedFacility = retrievedFacilities.find { it.name == facilityName }     //find facility with same name (assumes all facility names are unique)
//
//            selectedFacility?.let {             //if selected facility is not null (finds a match), set facility.value to it
//                facility.value = it
//            }
        }
    }

    init{
        Log.i("ViewModel","MainViewModel Init")
        getFacilityAttractions()
    }

    private fun getFacilityAttractions(){
        Log.i("ViewModel","Starting API request")
        viewModelScope.launch {
            mainUiState = try {
                Log.i("ViewModel", "Starting coroutine")
                val listResult = facilityRepository.getAttractions()
                Log.i("ViewModel", "API result: $listResult")
                MainUiState.Success("Status Code: ${listResult.statusCode}\n${listResult.body}")
            }catch (e: IOException){
                //TODO: Handle exception
                Log.e("ViewModel", "Error on API Call: $e")
                MainUiState.Error
            }
        }
    }

}