package com.example.skin_scanner_app

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skin_scanner_app.ui.theme.Skin_Scanner_AppTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    lateinit var permissionManager: PermissionManager
    lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraManager: CameraManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = CameraManager(this)
        permissionManager = PermissionManager(this)

        // Camera Permission Launcher
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_needed), Toast.LENGTH_SHORT).show()
            }
        }

        // Camera Activity Result Launcher
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Placeholder for action after successfully opening the camera
                Log.d("Camera", "OK!!")
            }
        }

        enableEdgeToEdge()
        setContent {
            Skin_Scanner_AppTheme {
                MainApp()
            }
        }
    }

    fun openCamera() {
        val cameraIntent = cameraManager.getCameraIntent()
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraActivityResultLauncher.launch(cameraIntent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("Home") }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .width(screenWidth * 0.6f)  // Adjust width to 60% of the screen
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Skin Scanner",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 30.dp) // replace with logo later?
                )
                Spacer(modifier = Modifier.height(24.dp))
                DrawerItem(
                    icon = Icons.Filled.Home,
                    text = "Home",
                    onClick = {
                        selectedScreen = "Home"
                        coroutineScope.launch { drawerState.close() }
                    }
                )
                DrawerItem(
                    icon = Icons.Filled.Star,
                    text = "Recommendations",
                    onClick = {
                        selectedScreen = "Recommendations"
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedScreen) },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (selectedScreen) {
                    "Home" -> Content()
                    "Recommendations" -> Recommendations()
                }
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun Content() {
    val context = LocalContext.current
    val activity = context as? MainActivity ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Skin Scanner",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Skin Scanner scans your skin using LLM to help detect possible melanoma.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
             onClick = {
                 Log.d("Camera", "Button was clicked")
                // Check if the permission is already granted
                if (activity.permissionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
                    // Open the camera directly
                    Log.d("Camera", "Should open camera...")
                    activity.openCamera()
                } else {
                    // Request the camera permission
                    Log.d("Camera","Will request camera permissions...")
                    activity.permissionManager.requestPermission(
                        activity.cameraPermissionLauncher,
                        Manifest.permission.CAMERA
                    )
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.size(100.dp)
        ) {
            Text(
                text = "Start Scan",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun Recommendations() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Here are some recommendations...",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    Skin_Scanner_AppTheme {
        MainApp()
    }
}