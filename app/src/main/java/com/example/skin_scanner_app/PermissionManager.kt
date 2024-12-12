package com.example.skin_scanner_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

class PermissionManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun isPermissionGranted(permission: String): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(launcher: ActivityResultLauncher<String>, permission: String) {
        if (!isPermissionGranted(permission)) {
            launcher.launch(permission)
        }
    }

    fun requestLocationPermissionAndFetch(
        launcher: ActivityResultLauncher<String>,
        onLocationReceived: (Location?) -> Unit
    ) {
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fetchLocation(onLocationReceived)
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchLocation(onLocationReceived: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, return without fetching the location
            Toast.makeText(context, "Location permission is not granted", Toast.LENGTH_SHORT).show()
            onLocationReceived(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful && task.result != null) {
                    onLocationReceived(task.result)
                } else {
                    Log.e("PermissionManager", "Failed to fetch location")
                    onLocationReceived(null)
                }
            }
    }
}
