package com.example.skin_scanner_app

import android.content.Context
import android.content.Intent
import android.provider.MediaStore

class CameraManager(private val context: Context) {

    fun getCameraIntent(): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    }
}