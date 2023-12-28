package com.example.qup.ui.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.Facility
import com.example.qup.data.FacilityRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(
    savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository
): ViewModel() {
    //state of facility obtained from request
    val facility = mutableStateOf<Facility>(Facility("", LatLng(0.0,0.0), 0f, emptyList()))
    //private val facilityName: String = checkNotNull(savedStateHandle[MapDestination.facility])

    fun retrieveFacility(facilityName: String){
        viewModelScope.launch {
            val retrievedFacilities = facilityRepository.getFacilities()

            val selectedFacility = retrievedFacilities.find { it.name == facilityName }     //find facility with same name (assumes all facility names are unique)

            selectedFacility?.let {             //if selected facility is not null (finds a match), set facility.value to it
                facility.value = it
            }
        }
    }

}