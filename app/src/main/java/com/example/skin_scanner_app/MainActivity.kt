import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skin_scanner_app.ui.theme.Skin_Scanner_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Skin_Scanner_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Content(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Content(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
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
            onClick = { /* TODO */ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier
                .size(100.dp) // Size for a circular button
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

@Preview(showBackground = true)
@Composable
fun ContentPreview() {
    Skin_Scanner_AppTheme {
        Content()
    }
}