package com.deeplivecam.android.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.deeplivecam.android.camera.CameraManager
import com.deeplivecam.android.ml.FaceSwapProcessor
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGallery: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraManager = remember { CameraManager(context) }
    val faceSwapProcessor = remember { FaceSwapProcessor.getInstance(context) }

    var isCameraStarted by remember { mutableStateOf(false) }
    var sourceFaceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedFrame by remember { mutableStateOf<Bitmap?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    val isProcessing = remember { AtomicBoolean(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val bmp = loadBitmap(context, it)
                    if (bmp != null) {
                        // Scale down if too large to prevent OOM
                        val scaledBmp = if (bmp.width > 2048 || bmp.height > 2048) {
                            val scale = minOf(2048f / bmp.width, 2048f / bmp.height)
                            val scaledWidth = (bmp.width * scale).toInt()
                            val scaledHeight = (bmp.height * scale).toInt()
                            val scaled = Bitmap.createScaledBitmap(bmp, scaledWidth, scaledHeight, true)
                            bmp.recycle()
                            scaled
                        } else {
                            bmp
                        }
                        
                        sourceFaceBitmap = scaledBmp
                        faceSwapProcessor.setSourceFace(scaledBmp)
                    }
                } catch (e: OutOfMemoryError) {
                    // Handle OOM gracefully
                    System.gc()
                } catch (e: Exception) {
                    // Ignore other errors gracefully
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
            faceSwapProcessor.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep Live Cam") },
                actions = {
                    IconButton(onClick = onNavigateToGallery) {
                        Icon(Icons.Default.PhotoLibrary, "Gallery")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isCameraStarted) {
                // Camera preview (raw feed underneath)
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { preview ->
                            previewViewRef = preview
                            cameraManager.startCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = preview
                            ) { frame ->
                                if (faceSwapProcessor.hasSourceFace() &&
                                    isProcessing.compareAndSet(false, true)
                                ) {
                                    scope.launch {
                                        try {
                                            processedFrame =
                                                faceSwapProcessor.processFaceSwap(frame)
                                        } finally {
                                            isProcessing.set(false)
                                        }
                                    }
                                }
                                frame
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay: show the face-swapped frame on top of the preview
                processedFrame?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Face-swapped preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                WelcomeScreen(
                    onStartCamera = { isCameraStarted = true },
                    onSelectSourceFace = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                )
            }

            // Source face thumbnail
            if (isCameraStarted && sourceFaceBitmap != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(80.dp),
                    shape = CircleShape
                ) {
                    Image(
                        bitmap = sourceFaceBitmap!!.asImageBitmap(),
                        contentDescription = "Source Face",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (isCameraStarted) {
                CameraControls(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    hasSourceFace = sourceFaceBitmap != null,
                    onSelectSourceFace = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onSwitchCamera = {
                        val pv = previewViewRef ?: return@CameraControls
                        processedFrame = null
                        cameraManager.switchCamera(lifecycleOwner, pv)
                    },
                    onStopCamera = {
                        cameraManager.stopCamera()
                        processedFrame = null
                        isCameraStarted = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(
    onStartCamera: () -> Unit,
    onSelectSourceFace: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Deep Live Cam",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Real-time face swap for Android",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSelectSourceFace,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, null)
                Spacer(Modifier.width(8.dp))
                Text("Select Source Face")
            }

            Button(
                onClick = onStartCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Videocam, null)
                Spacer(Modifier.width(8.dp))
                Text("Start Camera")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Use ethically. Obtain consent. Label as deepfake.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraControls(
    modifier: Modifier = Modifier,
    hasSourceFace: Boolean,
    onSelectSourceFace: () -> Unit,
    onSwitchCamera: () -> Unit,
    onStopCamera: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onSelectSourceFace,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    "Select Face",
                    tint = if (hasSourceFace)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(
                onClick = onSwitchCamera,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Icon(Icons.Default.Cameraswitch, "Switch Camera")
            }

            IconButton(
                onClick = onStopCamera,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    Icons.Default.Stop,
                    "Stop",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

private suspend fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .build()
        context.imageLoader.execute(request).drawable?.toBitmap()
    } catch (e: Exception) {
        null
    }
}
