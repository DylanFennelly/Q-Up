package com.example.qup.ui.camera

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.qup.QueueTopAppBar
import com.example.qup.R
import com.example.qup.ui.navigation.NavigationDestination
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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
    navigateToMap: (String) -> Unit,
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier,
        topBar = {
            QueueTopAppBar(
                title = stringResource(id = R.string.entrance_ticket_title),
                canNavigateBack = canNavigateBack,
                navigateUp = { onNavigateUp() }
            )
        },
    ) {  innerPadding ->
        Box {
            QRScanner(
                navigateToMap =  navigateToMap,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            )
        }
    }
}


//https://blog.devgenius.io/qr-code-scanner-with-jetpack-compose-camerax-and-ml-kit-8e5a1d4a2fc9
@Composable
fun QRScanner(
    modifier: Modifier = Modifier,
    navigateToMap: (String) -> Unit,
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
                                        "QR Code detected: $qrCodeContent",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navigateToMap("SETU")
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


