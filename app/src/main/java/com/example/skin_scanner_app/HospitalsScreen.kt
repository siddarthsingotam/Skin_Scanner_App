package com.example.skin_scanner_app

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

@Composable
fun Hospitals(
    permissionManager: PermissionManager,
    locationPermissionLauncher: ActivityResultLauncher<String>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Find Nearby Hospitals",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                permissionManager.requestPermission(
                    launcher = locationPermissionLauncher,
                    permission = Manifest.permission.ACCESS_FINE_LOCATION
                )

                if (permissionManager.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val uri = Uri.parse("geo:0,0?q=health+center") // "geo:0,0?q=health+center&radius=5000"
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    context.startActivity(intent)
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Search Nearby")
        }
    }
}
