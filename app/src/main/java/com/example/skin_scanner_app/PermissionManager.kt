package com.example.skin_scanner_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher

class PermissionManager(private val context: Context) {

    fun checkAndRequestLocationPermission(
        launcher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit
    ) {
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            onPermissionGranted()
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(launcher: ActivityResultLauncher<String>, permission: String) {
        if (!isPermissionGranted(permission)) {
            launcher.launch(permission)
        }
    }
}

