package com.example.luontopeli.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(viewModel: CameraViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember { ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Kameran lupa tarvitaan", modifier = Modifier.padding(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Myönnä lupa")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedImagePath == null) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { viewModel.takePhoto(context, imageCapture) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Camera, "Ota kuva", tint = Color.White)
                    }
                }
            }
        } else {
            CapturedImageView(
                imagePath = capturedImagePath!!,
                result = classificationResult,
                onRetake = { viewModel.clearCapturedImage() },
                onSave = { viewModel.saveCurrentSpot() }
            )
        }
    }
}

@Composable
fun CapturedImageView(
    imagePath: String,
    result: ClassificationResult?,
    onRetake: () -> Unit,
    onSave: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = "Otettu kuva",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().background(Color.Black)
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            result?.let {
                ClassificationResultCard(it)
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onRetake,
                    modifier = Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, "Ota uudelleen", tint = Color.White)
                }
                
                if (result is ClassificationResult.Success) {
                    IconButton(
                        onClick = onSave,
                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Check, "Tallenna", tint = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is ClassificationResult.Success ->
                    if (result.confidence > 0.8f)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tunnistettu:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = when {
                                result.confidence > 0.8f -> Color(0xFF2E7D32)
                                result.confidence > 0.6f -> Color(0xFFF57C00)
                                else -> Color(0xFFD32F2F)
                            }
                        ) {
                            Text("${"%.0f".format(result.confidence * 100)}%")
                        }
                    }

                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LinearProgressIndicator(
                        progress = { result.confidence },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = when {
                            result.confidence > 0.8f -> Color(0xFF2E7D32)
                            result.confidence > 0.6f -> Color(0xFFF57C00)
                            else -> Color(0xFFD32F2F)
                        }
                    )
                }

                is ClassificationResult.NotNature -> {
                    Text("Ei luontokohde", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Kuvassa tunnistettiin: ${result.allLabels.joinToString { it.text }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                is ClassificationResult.Error -> {
                    Text("Tunnistus epäonnistui: ${result.message}",
                        style = MaterialTheme.typography.bodyMedium)
                }
                ClassificationResult.Loading -> {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
