package com.example.skin_scanner_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraManager(private val context: Context) {

    private var currentPhotoPath: String? = null

    fun createImageFile(): File? {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (ex: IOException) {
            Log.e("CameraManager:", ex.localizedMessage)
            Toast.makeText(context, "An error occurred: ${ex.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        }
    }

    fun getCameraIntent(): Intent? {
        val photoFile: File? = createImageFile()
        return photoFile?.let {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "com.example.skin_scanner_app.fileprovider",
                it
            )
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        }
    }

    fun getCurrentPhotoPath(): String? = currentPhotoPath
}