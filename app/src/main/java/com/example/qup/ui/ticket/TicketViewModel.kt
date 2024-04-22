package com.example.qup.ui.ticket

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qup.data.FacilityRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface TicketUiState {
    data class Success(val qrBitmap: Bitmap?) : TicketUiState
    object Error : TicketUiState
    object Loading : TicketUiState
}
class TicketViewModel(
    private val facilityRepository: FacilityRepository,
    val baseUrl: String
): ViewModel() {
    var ticketUiState: TicketUiState by mutableStateOf(TicketUiState.Loading)
        private set

    init {
        Log.i("TicketViewModel", "TicketViewModel init")
    }

    //Ensuring queue entry is still valid
    fun checkForQueue(attractionId: Int, userId: Int){
        Log.i("TicketViewModel", "Starting checkForQueue")
        viewModelScope.launch {
            ticketUiState = try {
                Log.i("TicketViewModel", "Starting coroutine")
                val queuesResult = facilityRepository.getUserQueues(baseUrl + "user-queues", userId)
                val validQueue = queuesResult.find { it.attractionId == attractionId && it.callNum != 5 }

                //if linked matching attraction ID is found and ticket is not invalidated
                //ensuring edge case where user accesses this screen after being removed does not occur
                Log.i("TicketViewModel", "ValidQueue: $validQueue")
                if (validQueue != null){
                    Log.i("TicketViewModel", "ValidQueue found")
                    val qrCodeBitmap = generateTicketQR(baseUrl + "complete-queue?userId=$userId&attractionId=$attractionId")
                    if (qrCodeBitmap != null) {
                        TicketUiState.Success(qrCodeBitmap)
                    }else{
                        TicketUiState.Error
                    }
                }else{
                    Log.i("TicketViewModel", "No ValidQueue found")
                    TicketUiState.Error
                }

            }catch (e: IOException){
                TicketUiState.Error
            }
        }
    }

    //https://dev.to/devniiaddy/qr-code-with-jetpack-compose-47e
    fun generateTicketQR(text: String): Bitmap? {
        // width & height in pixels
        val width = 700
        val height = 700
        val qrCodeWriter = QRCodeWriter()

        try {
            //create qr bitMatrix
            val bitMatrix: BitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)

            //Creating bitMap from QR bitmatrix
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }

}