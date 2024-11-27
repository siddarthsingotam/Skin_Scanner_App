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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skin_scanner_app.ui.theme.Skin_Scanner_AppTheme
import kotlinx.coroutines.launch
import coil3.compose.AsyncImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity : ComponentActivity() {

    lateinit var permissionManager: PermissionManager
    lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraManager: CameraManager

    var photoPath by mutableStateOf<String?>(null)

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
                Log.d("Camera", "Camera permission denied")
            }
        }

        // Camera Activity Result Launcher
        cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                cameraManager.getCurrentPhotoPath()?.let { path ->
                    Log.d("Camera", "Photo saved at: $path")
                    photoPath = path // Update composable state with the image path
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            Skin_Scanner_AppTheme {
                MainApp(photoPath)
            }
        }
    }

    fun openCamera() {
        val cameraIntent = cameraManager.getCameraIntent()
        if (cameraIntent != null) {
            if (cameraIntent.resolveActivity(packageManager) != null) {
                cameraActivityResultLauncher.launch(cameraIntent)
            }
        }
    }

    fun analyzeImage(filePath: String) {
        val file = File(filePath)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        Log.d("MainActivity", "Sending image to server: ${file.name}")

        RetrofitClient.instance.uploadImage(body).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val result = response.body()?.string()
                    Toast.makeText(this@MainActivity, "Result: $result", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Result: $result")
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get result", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Failed to get result: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(photoPath: String?) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf("Home") }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .width(screenWidth * 0.68f)  // Adjust width to 68% of the screen
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
                    "Home" -> Content(photoPath)
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
fun Content(photoPath: String?) {
    val context = LocalContext.current
    val activity = context as? MainActivity ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface),
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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scan your skin to detect possible melanoma using advanced AI.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(60.dp))
        Button(
            onClick = {
                Log.d("Camera", "Button was clicked")
                if (activity.permissionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
                    activity.openCamera()
                } else {
                    activity.permissionManager.requestPermission(
                        activity.cameraPermissionLauncher,
                        Manifest.permission.CAMERA
                    )
                }
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .size(120.dp)
                .shadow(4.dp, CircleShape)
        ) {
            Text(
                text = "Start Scan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Display the captured image
        photoPath?.let { path ->
            AsyncImage(
                model = path,
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(8.dp)
                    .shadow(4.dp, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { activity.photoPath = null },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.size(100.dp)
                ) {
                    Text(
                        text = "Clear",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Analyzing...", Toast.LENGTH_SHORT).show()
                        photoPath.let { path ->
                            activity.analyzeImage(path)
                        }
                        Log.d("Camera", "Analyze button clicked")
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(100.dp)
                ) {
                    Text(
                        text = "Analyze",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


