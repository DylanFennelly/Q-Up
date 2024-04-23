package com.example.qup.ui.home

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.ApiException
import com.example.qup.data.FacilityRepository
import com.example.qup.data.UserIdApiResponse
import com.example.qup.ui.camera.CameraUiState
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface HomeUiState {
    data class Success(val isValid: Boolean): HomeUiState
    data class Error(val isValid: Boolean) : HomeUiState
    object InternetError : HomeUiState
    object Loading : HomeUiState
    object Idle : HomeUiState
}

class HomeViewModel(
    private val facilityRepository: FacilityRepository,
): ViewModel(){
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Idle)

    fun checkUserIdValidity(url: String, userId: Int){
        Log.i("HomeViewModel", "Starting checkUserIdValidity")
        homeUiState = HomeUiState.Loading
        viewModelScope.launch {
            if (url != "https://failed.com/") {     //if default URL
                homeUiState = try {
                    val validityCheck =
                        facilityRepository.checkUserIdValidity(url + "user-id", userId)
                    Log.i("HomeViewModel", "validity check: $validityCheck")
                    HomeUiState.Success(validityCheck.body.valid)
                } catch (e: ApiException) {       //if errors, assume invalid (id not in table)
                    HomeUiState.Error(false)
                } catch (e: IOException) {        //catch cases when internet not available
                    Log.i("HomeViewModel", "Internet error")
                    HomeUiState.InternetError
                }
            }else{
                Log.i("HomeViewModel", "Default URL -> no user ID to check")
                homeUiState = HomeUiState.Error(false)
            }
        }

    }

}