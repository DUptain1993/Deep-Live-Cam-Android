package com.deeplivecam.android

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.camera.view.PreviewView
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.deeplivecam.android.camera.CameraManager
import com.deeplivecam.android.ml.FaceSwapProcessor
import com.deeplivecam.android.ui.theme.DeepLiveCamTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Handles ACTION_IMAGE_CAPTURE / ACTION_VIDEO_CAPTURE intents from other apps.
 *
 * When a browser or any other app requests the camera, Android shows
 * Deep Live Cam in the chooser.  This activity presents a dialog:
 *   "Use normal camera" vs "Use Deep Live Cam (face swap)"
 * so the user always has a choice.
 */
class CameraCaptureActivity : ComponentActivity() {

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { sourceImageUri = it }
    }

    private var sourceImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DeepLiveCamTheme {
                CaptureScreen(
                    sourceImageUri = sourceImageUri,
                    onPickSource = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onUseNormalCamera = { forwardToStockCamera() },
                    onCaptured = { bitmap -> returnResult(bitmap) },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }

    private fun forwardToStockCamera() {
        try {
            val forward = Intent(intent?.action ?: MediaStore.ACTION_IMAGE_CAPTURE).apply {
                intent?.extras?.let { putExtras(it) }
                setPackage(null)
            }

            val candidates = packageManager.queryIntentActivities(forward, 0)
                .filter { it.activityInfo.packageName != packageName }

            if (candidates.isNotEmpty()) {
                forward.setClassName(
                    candidates[0].activityInfo.packageName,
                    candidates[0].activityInfo.name
                )
                stockCameraLauncher.launch(forward)
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch stock camera", e)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private val stockCameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        setResult(result.resultCode, result.data)
        finish()
    }

    @Suppress("DEPRECATION")
    private fun returnResult(bitmap: Bitmap) {
        try {
            val outputUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.extras?.getParcelable(MediaStore.EXTRA_OUTPUT, Uri::class.java)
            } else {
                intent?.extras?.getParcelable(MediaStore.EXTRA_OUTPUT)
            }

            if (outputUri != null) {
                contentResolver.openOutputStream(outputUri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                setResult(Activity.RESULT_OK)
            } else {
                val file = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                val data = Intent().apply { this.data = uri }
                setResult(Activity.RESULT_OK, data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error returning capture result", e)
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    companion object {
        private const val TAG = "CameraCaptureActivity"
    }
}

@Composable
private fun CaptureScreen(
    sourceImageUri: Uri?,
    onPickSource: () -> Unit,
    onUseNormalCamera: () -> Unit,
    onCaptured: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    var showChooser by remember { mutableStateOf(true) }
    var useFaceSwap by remember { mutableStateOf(false) }

    if (showChooser) {
        ChoiceDialog(
            onFaceSwap = {
                showChooser = false
                useFaceSwap = true
            },
            onNormalCamera = onUseNormalCamera,
            onCancel = onCancel
        )
        return
    }

    if (useFaceSwap) {
        FaceSwapCaptureView(
            sourceImageUri = sourceImageUri,
            onPickSource = onPickSource,
            onCaptured = onCaptured,
            onCancel = onCancel
        )
    }
}

@Composable
private fun ChoiceDialog(
    onFaceSwap: () -> Unit,
    onNormalCamera: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        AlertDialog(
            onDismissRequest = onCancel,
            icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) },
            title = { Text("Open camera with\u2026") },
            text = { Text("Another app wants to use the camera. Choose how to proceed.") },
            confirmButton = {
                Button(onClick = onFaceSwap) {
                    Icon(Icons.Default.Face, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Deep Live Cam")
                }
            },
            dismissButton = {
                TextButton(onClick = onNormalCamera) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Normal camera")
                }
            }
        )
    }
}

@Composable
private fun FaceSwapCaptureView(
    sourceImageUri: Uri?,
    onPickSource: () -> Unit,
    onCaptured: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraManager = remember { CameraManager(context) }
    val faceSwapProcessor = remember { FaceSwapProcessor.getInstance(context) }

    var processedFrame by remember { mutableStateOf<Bitmap?>(null) }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val isProcessing = remember { AtomicBoolean(false) }

    // Load source face when URI arrives
    LaunchedEffect(sourceImageUri) {
        if (sourceImageUri != null) {
            val bmp = loadBitmapFromUri(context, sourceImageUri)
            if (bmp != null) {
                sourceBitmap = bmp
                faceSwapProcessor.setSourceFace(bmp)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraManager.release() }
    }

    Box(Modifier.fillMaxSize()) {
        // Raw camera preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { preview ->
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

        // Overlay: processed face-swap frame
        processedFrame?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Face-swapped preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Source face thumbnail
        sourceBitmap?.let { bmp ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(64.dp),
                shape = CircleShape
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Source face",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Bottom controls
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickSource,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.AddAPhoto, "Pick source face")
                }

                Button(
                    onClick = {
                        val frame = processedFrame
                        if (frame != null) {
                            onCaptured(frame)
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PhotoCamera, "Capture")
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                ) {
                    Icon(Icons.Default.Close, "Cancel")
                }
            }
        }
    }
}

private suspend fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val request = ImageRequest.Builder(context).data(uri).build()
        context.imageLoader.execute(request).drawable?.toBitmap()
    } catch (e: Exception) {
        null
    }
}
