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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
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
import coil3.compose.AsyncImage
import com.example.skin_scanner_app.ui.CameraPreviewWithOverlay
import com.example.skin_scanner_app.ui.theme.Skin_Scanner_AppTheme
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
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
    var resultText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraManager = CameraManager(this)
        permissionManager = PermissionManager(this)

        // Initialize resultText with the string resource
        resultText = getString(R.string.tap_analyze_text)

        // Camera Permission Launcher
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    openCamera()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.camera_permission_needed),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("Camera", "Camera permission denied")
                }
            }

        // Camera Activity Result Launcher
        cameraActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    cameraManager.getCurrentPhotoPath()?.let { path ->
                        Log.d("Camera", "Photo saved at: $path")
                        photoPath = path // Update composable state with the image path
                        resultText = getString(R.string.tap_analyze_text) // Reset result text when a new picture is taken
                    }
                }
            }

        enableEdgeToEdge()
        setContent {
            Skin_Scanner_AppTheme {
                MainApp(photoPath, resultText)
            }
        }
    }

    fun openCamera() {
        val cameraIntent = cameraManager.getCameraIntent()
        if (cameraIntent?.resolveActivity(packageManager) != null) {
            cameraActivityResultLauncher.launch(cameraIntent)
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
                    val responseString = response.body()?.string().orEmpty()
                    val jsonObject = JSONObject(responseString)
                    val result = jsonObject.getString("result")
                    Toast.makeText(this@MainActivity, "Result: $result", Toast.LENGTH_LONG).show()
                    Log.d("MainActivity", "Result: $result")
                    resultText = result // Update result text state
                } else {
                    Toast.makeText(this@MainActivity, "Failed to get result", Toast.LENGTH_LONG)
                        .show()
                    Log.d("MainActivity", "Failed to get result: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                Log.e("MainActivity", "Error: ${t.message}")
            }
        })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainApp(photoPath: String?, resultText: String) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        var selectedScreen by remember { mutableStateOf("Home") }
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column(
                    modifier = Modifier
                        .width(screenWidth * 0.68f) // Adjust width to 68% of the screen
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Skin Scanner",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 30.dp)
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
                        "Home" -> Content(photoPath, resultText)
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
    fun Content(photoPath: String?, initialResultText: String) {
        val context = LocalContext.current
        val activity = context as? MainActivity ?: return

        var resultText by remember { mutableStateOf(initialResultText) }

        LaunchedEffect(activity.resultText) {
            resultText = activity.resultText
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Start Scan Button
            Button(
                onClick = {
                    if (activity.permissionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
                        Log.d("Camera", "Button was clicked")
                        activity.setContent {
                            CameraPreviewWithOverlay(
                                onImageCaptured = { imagePath ->
                                    Log.d("Camera", "Image captured at $imagePath")
                                    // Return to MainApp and display the captured photo
                                    activity.setContent {
                                        Skin_Scanner_AppTheme {
                                            MainApp(
                                                photoPath = imagePath,
                                                resultText = getString(R.string.tap_analyze_text)
                                            )
                                        }
                                    }
                                },
                                onError = { exception ->
                                    Log.e("Camera", "Error: ${exception.localizedMessage}")
                                    Toast.makeText(
                                        activity,
                                        "Error: ${exception.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    } else {
                        activity.cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .size(width = 150.dp, height = 60.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "Start Scan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Display the captured image
            photoPath?.let { path ->
                Spacer(modifier = Modifier.height(20.dp))
                AsyncImage(
                    model = path,
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .size(200.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                        .shadow(4.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            Log.d("ClearButton", "Clear button clicked")
                            activity.photoPath = null
                            activity.resultText = getString(R.string.tap_analyze_text)
                            activity.setContent {
                                Skin_Scanner_AppTheme {
                                    MainApp(
                                        photoPath = null,
                                        resultText = getString(R.string.tap_analyze_text)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.size(width = 150.dp, height = 60.dp)
                    ) {
                        Text(
                            text = "Clear",
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "Analyzing...", Toast.LENGTH_SHORT).show()
                            Log.d("Camera", "Analyze button clicked")
                            path.let { activity.analyzeImage(it) }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(width = 150.dp, height = 60.dp)
                    ) {
                        Text(
                            text = "Analyze",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Display result text
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (resultText == getString(R.string.tap_analyze_text)) resultText else "Result: $resultText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}