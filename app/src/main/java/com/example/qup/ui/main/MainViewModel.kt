package com.example.qup.ui.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.FacilityRepository
import com.example.qup.data.Attraction
import com.example.qup.data.JoinLeaveQueueBody
import com.example.qup.data.QueueEntry
import com.example.qup.helpers.calculateEstimatedQueueTime
import com.example.qup.helpers.sendNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface MainUiState {
    data class Success(val attractions: List<Attraction>) : MainUiState
    object Error : MainUiState
    object Loading : MainUiState
}

sealed interface QueuesUiState {
    data class Success(val userQueues: List<QueueEntry>) : QueuesUiState
    object Error : QueuesUiState
    object Loading : QueuesUiState
}

sealed interface JoinQueueUiState {
    data class Result(val statusCode: Int) : JoinQueueUiState
    object Loading : JoinQueueUiState   //Join Queue API request underway
    object Idle : JoinQueueUiState      //Request not underway
    object Error : JoinQueueUiState      //Error in request
}

class MainViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val facilityRepository: FacilityRepository,
    private val appContext: Context
): ViewModel() {

    var mainUiState: MainUiState by mutableStateOf(MainUiState.Loading)
        private set

    var queuesUiState: QueuesUiState by mutableStateOf(QueuesUiState.Loading)
        private set

    var joinQueueUiState: JoinQueueUiState by mutableStateOf(JoinQueueUiState.Idle)


    //Pull down to refresh data:
    // https://canlioya.medium.com/customise-pull-to-refresh-on-android-with-jetpack-compose-24a7119a4b94
    // https://medium.com/google-developer-experts/effortlessly-add-pull-to-refresh-to-your-android-app-with-jetpack-compose-4c8b218a9beb
    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    init{
        Log.i("ViewModel","MainViewModel Init")
    }

    fun getFacilityName(): String{
        // get value from savedStateHandle
        return checkNotNull(savedStateHandle["facilityName"])
    }

    fun setFacilityName(facilityName : String){
        savedStateHandle["facilityName"] = facilityName
    }

    fun refreshData(userId: Int){
        getFacilityAttractions()
        getUserQueues(userId)
        checkQueueTimes()
    }

    //Check user queues for each attraction and send notifications for ones close to queue
    fun checkQueueTimes(){
        when(mainUiState){
            is MainUiState.Success -> {
                when(queuesUiState){
                    is QueuesUiState.Success -> {
                        for (queue in (queuesUiState as QueuesUiState.Success).userQueues){
                            val linkedAttraction = (mainUiState as MainUiState.Success).attractions.find { it.id == queue.attractionId }

                            //if user somehow in queue for non-existent attractionId, skip it
                            if (linkedAttraction != null){
                                val queueTime = calculateEstimatedQueueTime(
                                    queue.aheadInQueue,
                                    linkedAttraction.avg_capacity,
                                    linkedAttraction.length
                                )

                                //if less then 5 minutes left in queue
                                //TODO: check user location and take into account when to send notification
                                //TODO: track what queues have had notifications sent for them
                                if (queueTime < 5){
                                    sendNotification(appContext,
                                        "Time to get going!",
                                        "Its almost time to enter ${linkedAttraction.name}, start heading towards the attraction now.",
                                        queue.attractionId)
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
            else -> {}
        }
    }


    //### REQUESTS ###

    //TODO: Add URL String input to function and facilityRepo function
    fun getFacilityAttractions(){
        Log.i("ViewModel","Starting API request")
        viewModelScope.launch {
            mainUiState = try {
                Log.i("ViewModel", "Starting coroutine")
                val listResult = facilityRepository.getAttractions()
                Log.i("ViewModel", "API result: $listResult.attractionList")
                MainUiState.Success(listResult.body)
            }catch (e: IOException){
                Log.e("ViewModel", "Error on API Call: $e")
                MainUiState.Error
            }
        }
    }

    fun getUserQueues(userId: Int){
        Log.i("ViewModel","Starting getUserQueues API request")
        viewModelScope.launch {
            queuesUiState = try {
                Log.i("ViewModel", "Starting getUserQueues coroutine")
                val queuesResult = facilityRepository.getUserQueues(userId)
                Log.i("ViewModel", "API result: ${queuesResult}")
                QueuesUiState.Success(queuesResult)
            }catch (e: IOException){
                Log.e("ViewModel", "Error on API Call: $e")
                QueuesUiState.Error
            }
        }
    }

    fun postJoinAttractionQueue(attractionId: Int, userId: Int){
        Log.i("ViewModel","Starting Join Queue API request")
        viewModelScope.launch {
            joinQueueUiState = try {
                Log.i("ViewModel","Starting coroutine")
                val joinResult = facilityRepository.joinQueue(body = JoinLeaveQueueBody(attractionId, userId))
                Log.i("ViewModel", "API result: $joinResult")
                JoinQueueUiState.Result(joinResult.statusCode)
            }catch (e: IOException) {
                Log.e("ViewModel", "Error on API Call: $e")
                JoinQueueUiState.Error
            }
        }
    }

    fun postLeaveAttractionQueue(attractionId: Int, userId: Int){
        Log.i("ViewModel","Starting Join Queue API request")
        viewModelScope.launch {
            joinQueueUiState = try {
                Log.i("ViewModel","Starting coroutine")
                val leaveResult = facilityRepository.leaveQueue(body = JoinLeaveQueueBody(attractionId, userId))
                Log.i("ViewModel", "API result: $leaveResult")
                JoinQueueUiState.Result(leaveResult.statusCode)
            }catch (e: IOException) {
                Log.e("ViewModel", "Error on API Call: $e")
                JoinQueueUiState.Error
            }
        }
    }

}