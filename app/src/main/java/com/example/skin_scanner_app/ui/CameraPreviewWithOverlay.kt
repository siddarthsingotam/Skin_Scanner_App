package com.example.skin_scanner_app.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.io.FileOutputStream

@Composable
fun CameraPreviewWithOverlay(
    modifier: Modifier = Modifier,
    onImageCaptured: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = ContextCompat.getMainExecutor(context)

    Box(modifier = modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        ) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (exc: Exception) {
                    onError(exc)
                }
            }, executor)
        }

        // Overlay: Green-bordered rectangle
        Box(
            modifier = Modifier
                .size(200.dp) // Size of the rectangle
                .align(Alignment.Center) // Center the rectangle
                .border(4.dp, Color.Green, RoundedCornerShape(8.dp)) // Green border
        )

        // Capture Button
        Button(
            onClick = {
                val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val photoFile = File(
                    outputDirectory,
                    "IMG_${System.currentTimeMillis()}.jpg"
                )
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    executor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            // Crop the image after it's saved
                            val croppedFile = cropImageToRectangle(photoFile)
                            if (croppedFile != null) {
                                onImageCaptured(croppedFile.absolutePath)
                            } else {
                                onError(Exception("Failed to crop image"))
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onError(exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(80.dp)
        ) {
            Text("Capture")
        }
    }
}

// Cropping logic
fun cropImageToRectangle(imageFile: File): File? {
    try {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return null

        // Define cropping rectangle (adjust coordinates and dimensions as needed)
        val rectX = (originalBitmap.width - 950) / 2 // Centered X
        val rectY = (originalBitmap.height - 950) / 2 // Centered Y
        val rectWidth = 950
        val rectHeight = 950

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            rectX, rectY, rectWidth, rectHeight
        )

        // Save the cropped image to a new file
        val croppedFile = File(imageFile.parent, "CROPPED_${imageFile.name}")
        FileOutputStream(croppedFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        return croppedFile
    } catch (e: Exception) {
        Log.e("Camera", "Error cropping image: ${e.localizedMessage}")
        return null
    }
}
