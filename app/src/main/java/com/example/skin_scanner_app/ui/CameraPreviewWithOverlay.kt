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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.skin_scanner_app.R
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Overlay: Green-bordered rectangle
            Box(
                modifier = Modifier
                    .size(200.dp) // Size of the rectangle
                    .border(4.dp, Color.Green, RoundedCornerShape(8.dp)) // Green border
            )

            Text(
                text = stringResource(R.string.point_camera_text),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp) // Adjust the padding to position the text below the rectangle
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
                                    onError(Exception(context.getString(R.string.failed_to_crop_image)))
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError(exception)
                            }
                        }
                    )
                },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(120.dp)
                    .shadow(8.dp, RoundedCornerShape(50.dp))
            ) {
                Text(
                    text = stringResource(R.string.capture_text),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Cropping logic
fun cropImageToRectangle(imageFile: File): File? {
    try {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath) ?: return null

        val matrix = android.graphics.Matrix().apply {
            postRotate(90f) // Rotate 90 degrees clockwise
        }

        val rotatedBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
        )

        // Define cropping rectangle (adjust coordinates and dimensions as needed)
        val rectX = (rotatedBitmap.width - 950) / 2 // Centered X
        val rectY = (rotatedBitmap.height - 950) / 2 // Centered Y
        val rectWidth = 950
        val rectHeight = 950

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(
            rotatedBitmap,
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
