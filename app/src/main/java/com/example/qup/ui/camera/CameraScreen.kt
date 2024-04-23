package com.example.qup.ui.camera

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.AppViewModelProvider
import com.example.qup.ui.main.MainViewModel
import com.example.qup.ui.main.ShowInfoDialog
import com.example.qup.ui.navigation.NavigationDestination
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


object CameraDestination: NavigationDestination {
    override val route = "camera"
    override val titleRes = R.string.camera_title
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CameraScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    navigateToMap: () -> Unit,
    onBack: () -> Unit,
    mainViewModel: MainViewModel,
    cameraViewModel: CameraViewModel = viewModel(factory = AppViewModelProvider.Factory)
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scope = rememberCoroutineScope()
    val showInfoDialogState = remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            QueueTopAppBar(
                title = stringResource(id = R.string.camera_title),
                canNavigateBack = canNavigateBack,
                navigateUp = { onNavigateUp() },
                showInfo = true,
                onInfoClick = {showInfoDialogState.value = true}
            )
        },
    ) {  innerPadding ->
        Box {
            if (showInfoDialogState.value) {
                ShowInfoDialog(
                    showInfoDialog = showInfoDialogState,
                    title = stringResource(id = R.string.camera_title),
                    description = stringResource(id = R.string.camera_info)
                )
            }
            when(cameraViewModel.cameraUiState) {
                is CameraUiState.Idle -> {
                    QRScanner(
                        cameraViewModel = cameraViewModel,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    )
                }
                is CameraUiState.Success -> {
                    Log.i("CameraViewModel", "UIState Success")
                    val userIdResult = (cameraViewModel.cameraUiState as CameraUiState.Success).userIdResult
                    Log.i("CameraViewModel", "userIdResult: $userIdResult")

                    RequestLoading()

                    LaunchedEffect(userIdResult) {
                        scope.launch {
                            mainViewModel.resetUpdateFlag()
                            mainViewModel.saveUserData(
                                userId = userIdResult.body.userId,
                                facilityName = userIdResult.body.facilityName,
                                baseUrl = userIdResult.body.baseUrl,
                                mapLat = userIdResult.body.mapLat,
                                mapLng = userIdResult.body.mapLng
                            )

                            var attempts = 0
                            while (attempts < 5) {
                                val facilityName = mainViewModel.facilityName.first()
                                val userId = mainViewModel.userId.first()
                                val baseUrl = mainViewModel.baseUrl.first()
                                val mapLat = mainViewModel.mapLat.first()
                                val mapLng = mainViewModel.mapLng.first()
                                Log.i("CameraViewModel", "UserId: ${facilityName}")
                                Log.i("CameraViewModel", "Facility Name: ${userId}")
                                Log.i("CameraViewModel", "Base URL: ${baseUrl}")
                                Log.i("CameraViewModel", "MapLat: ${mapLat}")
                                Log.i("CameraViewModel", "MapLng: ${mapLng}")


                                //Datastore can be slow to update - ensuring data has been updated and looping until it has

                                if (userId == userIdResult.body.userId &&
                                    facilityName == userIdResult.body.facilityName &&
                                    baseUrl == userIdResult.body.baseUrl &&
                                    mapLat == userIdResult.body.mapLat &&
                                    mapLng == userIdResult.body.mapLng
                                ) {
                                    Log.i(
                                        "CameraViewModel",
                                        "UserId: ${mainViewModel.facilityName.first()}"
                                    )
                                    Log.i(
                                        "CameraViewModel",
                                        "Facility Name: ${mainViewModel.userId.first()}"
                                    )
                                    Log.i(
                                        "CameraViewModel",
                                        "Base URL: ${mainViewModel.baseUrl.first()}"
                                    )
                                    Log.i(
                                        "CameraViewModel",
                                        "MapLat: ${mainViewModel.mapLat.first()}"
                                    )
                                    Log.i(
                                        "CameraViewModel",
                                        "MapLng: ${mainViewModel.mapLng.first()}"
                                    )

                                    Log.i("CameraViewModel", "Beginning Nav")
                                    navigateToMap()
                                    attempts = 5        //break the loop
                                } else {
                                    Log.i("CameraViewModel", "Data not updated in time.")
                                    attempts++
                                    delay(1000L)    //wait 1 sec before trying again
                                }
                            }

                        }
                    }



                }
                is CameraUiState.Loading -> {
                    Log.i("CameraViewModel", "UIState Loading")
                    Column(modifier = Modifier.padding(innerPadding)) {
                        RequestLoading()
                    }
                }
                is CameraUiState.Error -> {
                    Log.i("CameraViewModel", "UIState Error")
                    Column(modifier = Modifier.padding(innerPadding)) {
                        (cameraViewModel.cameraUiState as CameraUiState.Error).code?.let {
                            RequestError(errorCode = it)
                        }
                    }
                }
            }
        }
    }
}


//https://blog.devgenius.io/qr-code-scanner-with-jetpack-compose-camerax-and-ml-kit-8e5a1d4a2fc9
@Composable
fun QRScanner(
    modifier: Modifier = Modifier,
    cameraViewModel: CameraViewModel
){
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.camera_top),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom= 80.dp)
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AndroidView(
                { context ->
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val previewView = PreviewView(context).also {
                        it.scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        val imageCapture = ImageCapture.Builder().build()

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, QRAnalyser { qrCodeContent ->
                                    Log.d("CameraScreen", "QR Code Scanned: $qrCodeContent")
                                    Toast.makeText(
                                        context,
                                        "QR Code detected",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    cameraViewModel.getUserId(qrCodeContent)
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            // Unbind use cases before rebinding
                            cameraProvider.unbindAll()

                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                context as ComponentActivity,
                                cameraSelector,
                                preview,
                                imageCapture,
                                imageAnalyzer
                            )

                        } catch (exc: Exception) {
                            Log.e("CameraScreen", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(context))
                    previewView
                },
                modifier = Modifier
                    .fillMaxWidth()

            )
        }
    }


}

@Composable
fun RequestError(
    errorCode: Int,
    modifier: Modifier = Modifier
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (errorCode) {
            460 -> {        //Ticket is not active yet (starttime not reached)
                Icon(imageVector = Icons.Default.Warning, contentDescription = stringResource(id = R.string.generic_error), modifier = Modifier.size(128.dp))
                Text(
                    text = stringResource(id = R.string.ticket_error_460),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            461 -> {        //Ticket expired
                Icon(imageVector = Icons.Default.Warning, contentDescription = stringResource(id = R.string.generic_error), modifier = Modifier.size(128.dp))
                Text(
                    text = stringResource(id = R.string.ticket_error_461),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            462 -> {        //Already activate
                Icon(imageVector = Icons.Default.Warning, contentDescription = stringResource(id = R.string.generic_error), modifier = Modifier.size(128.dp))
                Text(
                    text = stringResource(id = R.string.ticket_error_462),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> { //misc errors
                Icon(imageVector = Icons.Default.Warning, contentDescription = stringResource(id = R.string.generic_error), modifier = Modifier.size(128.dp))
                Text(
                    text = stringResource(id = R.string.ticket_error_misc),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun RequestLoading(
    modifier: Modifier = Modifier
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.width(64.dp),
            color = colorResource(id = R.color.baby_blue),
            trackColor = colorResource(id = R.color.light_baby_blue),
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
class QRAnalyser(
    val callback: (String) -> Unit
) : ImageAnalysis.Analyzer {
     override fun analyze(imageProxy: ImageProxy) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.size > 0) {
                        for (barcode in barcodes) {
                            val rawValue = barcode.rawValue
                            rawValue?.let {
                                callback(rawValue)
                                return@addOnSuccessListener // Assuming you want to handle only the first QR code found
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }
        }
        imageProxy.close()
    }
}


