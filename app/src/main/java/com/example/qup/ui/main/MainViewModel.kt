package com.example.qup.ui.main

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.RefreshService
import com.example.qup.data.FacilityRepository
import com.example.qup.data.Attraction
import com.example.qup.data.JoinLeaveQueueBody
import com.example.qup.data.QueueEntry
import com.example.qup.data.RequestsRepository
import com.example.qup.data.UpdateCallNumBody
import com.example.qup.data.UserRepository
import com.example.qup.helpers.calculateEstimatedQueueTime
import com.example.qup.helpers.sendNotification
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

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
    private val appContext: Context,
    private val requestsRepository: RequestsRepository,
): ViewModel() {

    val userRepository = UserRepository(appContext, viewModelScope)

    val userId: Flow<Int> = userRepository.userId.catch { emit(-1) }    //default value
    val facilityName: Flow<String> = userRepository.facilityName.catch { emit("") }
    val baseUrl: Flow<String> = userRepository.baseUrl.catch { emit("https://failed.com/") }
    val mapLat: Flow<Double> = userRepository.mapLat.catch { emit(0.0) }
    val mapLng: Flow<Double> = userRepository.mapLng.catch { emit(0.0) }

    private val userDataUpdated = MutableStateFlow(false)
    val isDataUpdated: StateFlow<Boolean> = userDataUpdated.asStateFlow()

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

    private var isServiceStarted = false

    init{
        Log.i("ViewModel","MainViewModel Init")
        startAutoRefresh()     //TODO: harcoded id
    }

    suspend fun saveUserData(userId:Int, facilityName:String, baseUrl:String, mapLat:Double, mapLng:Double){
        userRepository.saveExampleData(userId, facilityName, baseUrl, mapLat, mapLng)
        userDataUpdated.value = true
    }

    fun resetUpdateFlag() {
        userDataUpdated.value = false
    }



    // function that manages how long its been since auto refresh
    // https://kotlinlang.org/docs/flow.html#flows-are-cold
    private fun timerFlow(interval: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit) // Emit an item to trigger the action
            delay(interval) // Wait for the next interval
        }
    }

    fun startAutoRefresh() {
        viewModelScope.launch {
            // Emitting an event every 60 seconds
            timerFlow(60_000).collectLatest {

                val userId = userId.firstOrNull() ?: -1
                Log.d("ViewModel", "timer flow 60 seconds")
                if (userId > 0) {
                    Log.d("ViewModel", "auto refresh")
                    refreshData(userId)
                }
            }
        }
    }


    fun startServiceIfNotStarted(context: Context, userId: Int) {
        if (!isServiceStarted) {
            val serviceIntent = Intent(context, RefreshService::class.java).apply {
                putExtra("userId", userId)
            }
            context.startService(serviceIntent)
            isServiceStarted = true
        }
    }

    fun getFacilityName(): String{
        // get value from savedStateHandle
        return checkNotNull(savedStateHandle["facilityName"])
    }

    fun setFacilityName(facilityName : String){
        savedStateHandle["facilityName"] = facilityName
    }

    fun refreshData(userId: Int){
        Log.i("ViewModel","Refresh Data Called")
        viewModelScope.launch {
            //waiting for both API requests to finish before checking queue times - https://stackoverflow.com/questions/58568592/how-to-wait-for-all-the-async-to-finish
            val attractionsDeferred = async{ getFacilityAttractions() }
            val queuesDeferred = async{ getUserQueues(userId) }

            attractionsDeferred.await()
            queuesDeferred.await()

            checkQueueTimes(userId)
        }
    }

    //Check user queues for each attraction and send notifications for ones close to queue
    fun checkQueueTimes(userId: Int){
        Log.d("ViewModel", "Starting queue time check")
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

                                getUserLocation { location ->
                                    if (location != null){
                                        Log.d("Location", "Queue: ${linkedAttraction.name}")
                                        Log.d("Location", "Call num: ${queue.callNum}")
                                        Log.d("Location", "User latitiude: ${location.latitude}")
                                        Log.d("Location", "User longitude: ${location.longitude}")

                                        //https://developer.android.com/reference/android/location/Location.html#distanceBetween(double,%20double,%20double,%20double,%20float[])
                                        val results = FloatArray(3)
                                        Location.distanceBetween(linkedAttraction.lat, linkedAttraction.lng, location.latitude, location.longitude, results)
                                        val distance = results[0]       //results in meters
                                        Log.d("Location", "Distance between: ${results[0]}")

                                        //Average human walks 100m in about 70 secs -> https://online-calculator.org/how-long-does-it-take-to-walk-100-meters
                                        //distanceBetween draws straight line between points -> users will most likely not be able to walk straight between points
                                        //round up to 2 mins -> every 100m means 1st notification will be sent 2 mins early
                                        //distance/100 * 2 -> 100m = 2 mins added, 350m -> 5mins added

                                        //modifier to add to check queue time on based on distance from attraciton
                                        val queueTimeModifier = ((distance/100) * 2).roundToInt()
                                        val firstCallQueueTime = 5 + queueTimeModifier
                                        Log.d("Location", "queueTimeMod: ${queueTimeModifier}")
                                        Log.d("Location", "firstCallQueueTime: ${firstCallQueueTime}")


                                        val timeNow = Instant.now()
                                        val lastUpdatedTime = Instant.parse(queue.lastUpdated)
                                        val minsBetween = Duration.between(lastUpdatedTime, Instant.now()).toMinutes()

                                        //How long until each reminder/queue removal occurs
                                        val reminderOne = 10
                                        val reminderTwo = 5
                                        val reminderRemove = 5

                                        Log.d("Location", "Time now: $timeNow")
                                        Log.d("Location", "Last updated: $lastUpdatedTime")
                                        Log.d("Location", "Duration between times: $minsBetween")


                                        //if less than 5 mins in queue & user hasnt been called/has had initial call
                                        if(queueTime <= 5 && queue.callNum < 2){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entrance Ticket Available!",
                                                "Your entrance ticket for ${linkedAttraction.name} is now available.\n\nGo to Your Queues to view the ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 2)       //2 = entrance ticket created
                                        }

                                        //if less then firstCallQueueTime (5mins + distance) & user has not been called for this queue yet
                                        else if (queueTime <= firstCallQueueTime && queue.callNum == 0){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Time to get going!",
                                                "Its almost time to enter ${linkedAttraction.name}, start heading towards the attraction now to make it in time for entry.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 1)       //1 = called to start heading toward attraction
                                        }

                                        //if it has been 10 minutes since entrance ticket generated
                                        else if (
                                            minsBetween >= reminderOne && queue.callNum == 2){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entry Reminder",
                                                "Your entry ticket for ${linkedAttraction.name} is available.\n\nBe sure to enter the attraction in the next ${reminderOne} minutes or you will lose your ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 3)       //3 = reminder to go to attraction
                                        }

                                        //if it has been 5 minutes since reminder
                                        else if (
                                            minsBetween >= reminderTwo && queue.callNum == 3
                                            ){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entry Reminder",
                                                "Your entry ticket for ${linkedAttraction.name} is available.\n\nEnter the attraction within the next ${reminderRemove} minutes or you will lose your ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 4)       //4 = final reminder
                                        }

                                        //if it has been 5 minutes since final reminder
                                        else if (minsBetween >= reminderRemove && queue.callNum == 4){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Ticket Expired",
                                                "Your entry ticket for ${linkedAttraction.name} has expired. You have been removed from the queue for this attraction.",
                                                queue.attractionId)

                                            updateQueueCallNum(queue.attractionId, userId, 5)       //to prevent notification from being sent twice
                                            refreshData(userId) //refresh data afterwards to remove queue from view     TODO: doesnt seem to be working, must refresh again to remove from view
                                        }

                                        else if (queue.callNum == 5){       //if call num == 5, remove
                                            postLeaveAttractionQueue(queue.attractionId, userId)
                                        }


                                    }else{
                                        //Calculate without queuetime mod
                                        val firstCallQueueTime = 10         //If no location, user longer estimation
                                        val reminderOne = 10
                                        val reminderTwo = 5
                                        val reminderRemove = 5

                                        val timeNow = Instant.now()
                                        val lastUpdatedTime = Instant.parse(queue.lastUpdated)
                                        val minsBetween = Duration.between(lastUpdatedTime, Instant.now()).toMinutes()

                                        if(queueTime <= 5 && queue.callNum < 2){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entrance Ticket Available!",
                                                "Your entrance ticket for ${linkedAttraction.name} is now available.\n\nGo to Your Queues to view the ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 2)       //2 = entrance ticket created
                                        }

                                        //if less then firstCallQueueTime (5mins + distance) & user has not been called for this queue yet
                                        else if (queueTime <= firstCallQueueTime && queue.callNum == 0){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Time to get going!",
                                                "Its almost time to enter ${linkedAttraction.name}, start heading towards the attraction now to make it in time for entry.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 1)       //1 = called to start heading toward attraction
                                        }

                                        //if it has been 10 minutes since entrance ticket generated
                                        else if (
                                            minsBetween >= reminderOne && queue.callNum == 2){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entry Reminder",
                                                "Your entry ticket for ${linkedAttraction.name} is available.\n\nBe sure to enter the attraction in the next ${reminderOne} minutes or you will lose your ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 3)       //3 = reminder to go to attraction
                                        }

                                        //if it has been 5 minutes since reminder
                                        else if (
                                            minsBetween >= reminderTwo && queue.callNum == 3
                                        ){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Entry Reminder",
                                                "Your entry ticket for ${linkedAttraction.name} is available.\n\nEnter the attraction within the next ${reminderRemove} minutes or you will lose your ticket.",
                                                queue.attractionId)
                                            updateQueueCallNum(queue.attractionId, userId, 4)       //4 = final reminder
                                        }

                                        //if it has been 5 minutes since final reminder
                                        else if (minsBetween >= reminderRemove && queue.callNum == 4){
                                            Log.d("CheckQueueTime", "Sending notificaiton for Attraction ${linkedAttraction.name} and Call Num ${queue.callNum}")
                                            sendNotification(appContext,
                                                "Ticket Expired",
                                                "Your entry ticket for ${linkedAttraction.name} has expired. You have been removed from the queue for this attraction.",
                                                queue.attractionId)

                                            updateQueueCallNum(queue.attractionId, userId, 5)       //to prevent notification from being sent twice
                                            postLeaveAttractionQueue(queue.attractionId, userId)
                                            refreshData(userId) //refresh data afterwards to remove queue from view     TODO: doesnt seem to be working, must refresh again to remove from view
                                        }

                                    }
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
    suspend fun getFacilityAttractions(){
        Log.i("ViewModel","Starting getFacilityAttractions API request")

        val currentBaseUrl = baseUrl.firstOrNull() ?: "https://failed.com/"

        mainUiState = try {
            Log.i("ViewModel", "Starting coroutine")
            val listResult = facilityRepository.getAttractions(currentBaseUrl + "test-data")
            Log.i("ViewModel", "API result: $listResult.attractionList")
            MainUiState.Success(listResult.body)
        }catch (e: IOException){
            Log.e("ViewModel", "Error on API Call: $e")
            MainUiState.Error
        }

    }

    suspend fun getUserQueues(userId: Int){
        Log.i("ViewModel","Starting getUserQueues API request")

        val currentBaseUrl = baseUrl.firstOrNull() ?: "https://failed.com/"

        queuesUiState = try {

            Log.i("ViewModel", "Starting getUserQueues coroutine")
            val queuesResult = facilityRepository.getUserQueues(currentBaseUrl + "user-queues", userId)
            Log.i("ViewModel", "API result: ${queuesResult}")
            QueuesUiState.Success(queuesResult)
        }catch (e: IOException){
            Log.e("ViewModel", "Error on API Call: $e")
            QueuesUiState.Error
        }
    }

    fun postJoinAttractionQueue(attractionId: Int, userId: Int){
        Log.i("ViewModel","Starting Join Queue API request")

        viewModelScope.launch {
            val currentBaseUrl = baseUrl.firstOrNull() ?: "https://failed.com/"

            joinQueueUiState = try {
                Log.i("ViewModel","Starting coroutine")
                val joinResult = facilityRepository.joinQueue(url = currentBaseUrl + "join-queue" ,body = JoinLeaveQueueBody(attractionId, userId))
                Log.i("ViewModel", "API result: $joinResult")
                JoinQueueUiState.Result(joinResult.statusCode)
            }catch (e: IOException) {
                Log.e("ViewModel", "Error on API Call: $e")
                JoinQueueUiState.Error
            }
        }
    }

    fun postLeaveAttractionQueue(attractionId: Int, userId: Int){
        Log.i("ViewModel","Starting Leave Queue API request")
        viewModelScope.launch {
            joinQueueUiState = try {
                val currentBaseUrl = baseUrl.firstOrNull() ?: "https://failed.com/"

                Log.i("ViewModel","Starting coroutine")
                val leaveResult = facilityRepository.leaveQueue(url = currentBaseUrl + "leave-queue", body = JoinLeaveQueueBody(attractionId, userId))
                Log.i("ViewModel", "API result: $leaveResult")
                JoinQueueUiState.Result(leaveResult.statusCode)
            }catch (e: IOException) {
                Log.e("ViewModel", "Error on API Call: $e")
                JoinQueueUiState.Error
            }
        }
    }

    fun updateQueueCallNum(attractionId: Int, userId: Int, callNum: Int){
        Log.i("ViewModel","Update Queue Call Num API request")
        viewModelScope.launch {
            val currentBaseUrl = baseUrl.firstOrNull() ?: "https://failed.com/"

            try {
                Log.i("ViewModel","Starting coroutine")
                val updateResult = facilityRepository.updateQueueCallNum(url = currentBaseUrl + "update-queue", body = UpdateCallNumBody(attractionId, userId, callNum))
                Log.i("ViewModel", "API result: $updateResult")
            }catch (e: IOException) {
                Log.e("ViewModel", "Error on API Call: $e")
            }
        }
    }


    //### MISC ###
    //https://developer.android.com/develop/sensors-and-location/location/request-updates
    //https://medium.com/@grudransh1/best-way-to-get-users-location-in-android-app-using-location-listener-from-java-in-android-studio-77882f8b87fd
    fun getUserLocation(onLocationReceived: (Location?) -> Unit) {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocationReceived(location)
                // After receiving the first location update, remove the listener to avoid further updates
                locationManager.removeUpdates(this)
            }

            override fun onProviderDisabled(provider: String) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }

        try {
            // Requesting location updates
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null)
        } catch (ex: SecurityException) {
            // Handle case where location permissions are not granted
            onLocationReceived(null)
        }
    }

}