package com.example.qup.ui.camera

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.ApiException
import com.example.qup.data.FacilityRepository
import com.example.qup.data.UserIdApiResponse
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface CameraUiState {
    data class Success(val userIdResult: UserIdApiResponse): CameraUiState
    data class Error(val message: String?, val code: Int?) : CameraUiState
    object Loading : CameraUiState
    object Idle : CameraUiState
}

class CameraViewModel(
    private val facilityRepository: FacilityRepository,
): ViewModel(){
    var cameraUiState: CameraUiState by  mutableStateOf(CameraUiState.Idle)

    init {
        Log.i("CameraViewModel", "CameraViewModel init")
    }

    //uses QR code scan to get user ID
    fun getUserId(url: String){
        Log.i("CameraViewModel", "Starting getUserId")
        cameraUiState = CameraUiState.Loading
        viewModelScope.launch {
            cameraUiState = try {
                Log.i("CameraViewModel", "Starting coroutine")
                val userIdResult = facilityRepository.getUserId(url)
                Log.i("CameraViewModel", "userIdResult: $userIdResult")
                CameraUiState.Success(userIdResult)
            }catch (e: ApiException){
                CameraUiState.Error(e.message, e.code)

            }catch (e: Exception){
                CameraUiState.Error("Unknown error occured", 0)
            }

        }
    }
}