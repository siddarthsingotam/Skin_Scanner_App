package com.example.skin_scanner_app.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun CameraReadyScreen(onStartCamera: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Rectangle overlay
        Box(
            modifier = Modifier
                .size(200.dp) // Size of the rectangle
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)) // Styled border
        )

        // "Start Camera" button
        Button(
            onClick = onStartCamera,
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp)
        ) {
            Text("Start Camera")
        }
    }
}

//@Composable
//fun CameraOverlay(modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier.fillMaxSize()
//    ) {
//        // Centered rectangle overlay
//        Box(
//            modifier = Modifier
//                .size(200.dp) // Size of the rectangle
//                .align(Alignment.Center)
//                .border(2.dp, Color.Red) // Red border for the rectangle
//        )
//    }
//}
